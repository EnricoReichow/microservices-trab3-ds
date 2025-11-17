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

    public static void start() throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        factory.setUsername("user");
        factory.setPassword("password");

        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        // Configura exchange principal para roteamento de pacientes
        channel.exchangeDeclare(EXCHANGE_NAME, "direct", true);

        // Declara filas para cada especialidade médica
        channel.queueDeclare(CARDIO_QUEUE, true, false, false, null);
        channel.queueDeclare(NEURO_QUEUE, true, false, false, null);
        channel.queueDeclare(GERAL_QUEUE, true, false, false, null);

        // Vincula cada fila à sua routing key específica
        channel.queueBind(CARDIO_QUEUE, EXCHANGE_NAME, CARDIO_KEY);
        channel.queueBind(NEURO_QUEUE, EXCHANGE_NAME, NEURO_KEY);
        channel.queueBind(GERAL_QUEUE, EXCHANGE_NAME, GERAL_KEY);

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

        channel.basicConsume(TRIAGEM_QUEUE, false, deliverCallback, consumerTag -> {});
        System.out.println("Serviço de Triagem iniciado. Aguardando pacientes...");
        Thread.currentThread().join();
    }

    public static void sendPaciente(Channel channel, Paciente paciente, String routingKey) throws Exception {
        String updatedMessage = gson.toJson(paciente);
        channel.basicPublish(EXCHANGE_NAME, routingKey, null, updatedMessage.getBytes("UTF-8"));

        if (routingKey.equals(LOGGER_KEY)) {
            System.out.println("Paciente " + paciente.getNome() + " ATUALIZADO no logger (Médico: " + paciente.getMedicoResponsavel() + ")");
        } else {
            System.out.println("Paciente " + paciente.getNome() + " ENCAMINHADO para " + paciente.getMedicoResponsavel() + " (Key: " + routingKey + ")");
        }
    }
}
