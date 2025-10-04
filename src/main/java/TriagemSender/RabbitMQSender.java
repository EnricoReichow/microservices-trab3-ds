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
    private static final String CARDIO_QUEUE = "cardiologia";
    private static final String NEURO_QUEUE = "neurologia";
    private static final String GERAL_QUEUE = "geral";
    private static final String CARDIO_KEY = "paciente.cardiologia";
    private static final String NEURO_KEY = "paciente.neurologia";
    private static final String GERAL_KEY = "paciente.geral";
    private static final Gson gson = new Gson();

    /**
     * Inicia o serviço de triagem de pacientes.
     * <p>
     * Este método estabelece conexão com o RabbitMQ, configura as exchanges e filas
     * necessárias e inicia o consumo de mensagens da fila de triagem. Para cada
     * paciente recebido, analisa os sintomas e determina o médico responsável:
     * - Cardiologista: para "Infarto" e "Pressão alta"
     * - Neurologista: para "Dor de cabeça" e "Perda dos movimentos (AVC)"
     * - Clínico Geral: para demais sintomas
     *
     * @throws Exception se ocorrer erro durante a conexão com RabbitMQ ou processamento
     */
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

        // Callback para processar mensagens recebidas da fila de triagem
        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
            Paciente paciente = gson.fromJson(message, Paciente.class);

            String sintoma = paciente.getSintomas();
            String destinoKey;

            // Lógica de triagem baseada nos sintomas apresentados
            if ("Infarto".equalsIgnoreCase(sintoma) || "Pressão alta".equalsIgnoreCase(sintoma)) {
                // Casos cardiológicos
                paciente.setMedicoResponsavel("Cardiologista");
                destinoKey = CARDIO_KEY;
            } else if ("Dor de cabeça".equalsIgnoreCase(sintoma) ||
                    "Perda dos movimentos de um lado (princípio AVC)".equalsIgnoreCase(sintoma)) {
                // Casos neurológicos
                paciente.setMedicoResponsavel("Neurologista");
                destinoKey = NEURO_KEY;
            } else {
                // Demais casos vão para clínica geral
                paciente.setMedicoResponsavel("Geral");
                destinoKey = GERAL_KEY;
            }

            try {
                sendPaciente(channel, paciente, destinoKey);
            } catch (Exception e) {
                System.out.println("Erro ao enviar Triagem para o RabbitMQ: " + e.getMessage());
            }

            channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
        };

        // Inicia consumo da fila de triagem com ACK manual
        channel.basicConsume(TRIAGEM_QUEUE, false, deliverCallback, consumerTag -> {});
        Thread.currentThread().join();
    }

    /**
     * Envia um paciente para a fila especializada apropriada.
     * <p>
     * Serializa o objeto Paciente (já com o médico responsável definido)
     * para JSON e o publica na exchange com a routing key especificada,
     * direcionando-o para a fila do especialista apropriado.
     *
     * @param channel    O canal de comunicação com o RabbitMQ
     * @param paciente   O objeto Paciente a ser enviado
     * @param routingKey A chave de roteamento que determina a fila de destino
     * @throws Exception se ocorrer erro durante a serialização ou envio
     */
    public static void sendPaciente(Channel channel, Paciente paciente, String routingKey) throws Exception {
        String updatedMessage = gson.toJson(paciente);
        // Publica na exchange com a routing key específica da especialidade
        channel.basicPublish(EXCHANGE_NAME, routingKey, null, updatedMessage.getBytes("UTF-8"));
        System.out.println("Paciente " + paciente.getNome() +
                " encaminhado para " + paciente.getMedicoResponsavel());
    }
}
