package TriagemSender;

import com.google.gson.Gson;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

import java.nio.charset.StandardCharsets;

/**
 * Classe responsável pela triagem e roteamento de pacientes no sistema.
 * <p>
 * Esta classe consome mensagens da fila de triagem, analisa os sintomas
 * dos pacientes e os redireciona para as filas especializadas apropriadas
 * (cardiologia, neurologia ou clínica geral) baseado nos sintomas apresentados.
 * <p>
 * IMPORTANTE: Apenas o coordenador (eleito via Bully Algorithm) processa a fila de triagem.
 */
public class RabbitMQSender {
    private static final String EXCHANGE_NAME = "exchangePA";
    private static final String TRIAGEM_QUEUE = "triagem";

    // Filas e Keys dos Especialistas
    private static final String CARDIO_QUEUE = "cardiologia";
    private static final String NEURO_QUEUE = "neurologia";
    private static final String GERAL_QUEUE = "geral";
    private static final String CARDIO_KEY = "paciente.cardiologia";
    private static final String NEURO_KEY = "paciente.neurologia";
    private static final String GERAL_KEY = "paciente.geral";
    private static final String LOGGER_KEY = "paciente.log";

    private static final Gson gson = new Gson();

    private static Connection connection;
    private static Channel channel;
    private static String consumerTag;
    private static volatile boolean isConsuming = false;

    /**
     * Inicializa a conexão com RabbitMQ e configura filas/exchanges
     */
    public static void initialize() throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        factory.setUsername("triagem");
        factory.setPassword("password");

        connection = factory.newConnection();
        channel = connection.createChannel();

        // Configura exchange principal para roteamento de pacientes
        channel.exchangeDeclare(EXCHANGE_NAME, "direct", true);

        // Declara filas para cada especialidade médica
        channel.queueDeclare(CARDIO_QUEUE, true, false, false, null);
        channel.queueDeclare(NEURO_QUEUE, true, false, false, null);
        channel.queueDeclare(GERAL_QUEUE, true, false, false, null);
        channel.queueDeclare(TRIAGEM_QUEUE, true, false, false, null);

        // Vincula cada fila à sua routing key específica
        channel.queueBind(CARDIO_QUEUE, EXCHANGE_NAME, CARDIO_KEY);
        channel.queueBind(NEURO_QUEUE, EXCHANGE_NAME, NEURO_KEY);
        channel.queueBind(GERAL_QUEUE, EXCHANGE_NAME, GERAL_KEY);

        System.out.println("[TRIAGEM] Conexão RabbitMQ inicializada");
    }

    /**
     * Inicia o consumo da fila de triagem (apenas chamado quando este processo é o líder)
     */
    public static synchronized void startConsuming() throws Exception {
        if (isConsuming) {
            System.out.println("[TRIAGEM] Já está consumindo a fila");
            return;
        }

        if (channel == null || !channel.isOpen()) {
            throw new IllegalStateException("Canal RabbitMQ não está inicializado");
        }

        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
            Paciente paciente = gson.fromJson(message, Paciente.class);

            String sintoma = paciente.getSintomas();
            String destinoKeyEspecialista;

            if ("Infarto".equalsIgnoreCase(sintoma) || "Pressão alta".equalsIgnoreCase(sintoma)) {
                paciente.setMedicoResponsavel("Cardiologista");
                destinoKeyEspecialista = CARDIO_KEY;
            } else if ("Dor de cabeça".equalsIgnoreCase(sintoma) || "Perda dos movimentos de um lado (princípio AVC)".equalsIgnoreCase(sintoma)) {
                paciente.setMedicoResponsavel("Neurologista");
                destinoKeyEspecialista = NEURO_KEY;
            } else {
                paciente.setMedicoResponsavel("Geral");
                destinoKeyEspecialista = GERAL_KEY;
            }

            try {
                sendPaciente(channel, paciente, destinoKeyEspecialista);  // envia para a fila do especialista
                sendPaciente(channel, paciente, LOGGER_KEY); // envio para o logger
            } catch (Exception e) {
                System.out.println("Erro ao enviar Triagem para o RabbitMQ: " + e.getMessage());
            }

            channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
        };

        consumerTag = channel.basicConsume(TRIAGEM_QUEUE, false, deliverCallback,
            tag -> System.out.println("[TRIAGEM] Consumer cancelado: " + tag));

        isConsuming = true;
        System.out.println("[TRIAGEM] ✅ INICIOU consumo da fila de triagem (consumerTag: " + consumerTag + ")");
    }

    /**
     * Para o consumo da fila de triagem (chamado quando este processo perde a liderança)
     */
    public static synchronized void stopConsuming() throws Exception {
        if (!isConsuming || consumerTag == null) {
            System.out.println("[TRIAGEM] Não está consumindo");
            return;
        }

        if (channel != null && channel.isOpen()) {
            channel.basicCancel(consumerTag);
            isConsuming = false;
            System.out.println("[TRIAGEM] ❌ PAROU consumo da fila de triagem");
        }
    }

    /**
     * @deprecated Use initialize() + startConsuming() ao invés disso
     */
    @Deprecated
    public static void start() throws Exception {
        initialize();
        startConsuming();
        System.out.println("Serviço de Triagem iniciado. Aguardando pacientes...");
        Thread.currentThread().join();
    }

    public static void sendPaciente(Channel channel, Paciente paciente, String routingKey) throws Exception {
        String updatedMessage = gson.toJson(paciente);
        channel.basicPublish(EXCHANGE_NAME, routingKey, null, updatedMessage.getBytes(StandardCharsets.UTF_8));

        if (routingKey.equals(LOGGER_KEY)) {
            System.out.println("Paciente " + paciente.getNome() + " ATUALIZADO no logger (Médico: " + paciente.getMedicoResponsavel() + ")");
        } else {
            System.out.println("Paciente " + paciente.getNome() + " ENCAMINHADO para " + paciente.getMedicoResponsavel() + " (Key: " + routingKey + ")");
        }
    }

    public static void shutdown() {
        try {
            if (isConsuming) {
                stopConsuming();
            }
            if (channel != null && channel.isOpen()) {
                channel.close();
            }
            if (connection != null && connection.isOpen()) {
                connection.close();
            }
        } catch (Exception e) {
            System.err.println("[TRIAGEM] Erro ao desligar: " + e.getMessage());
        }
    }
}
