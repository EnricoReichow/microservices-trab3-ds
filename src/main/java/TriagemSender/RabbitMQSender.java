package TriagemSender;

import com.google.gson.Gson;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

import java.nio.charset.StandardCharsets;

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

    public static void start() throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        factory.setUsername("user");
        factory.setPassword("password");

        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        channel.exchangeDeclare(EXCHANGE_NAME, "direct", true);
        channel.queueDeclare(CARDIO_QUEUE, true, false, false, null);
        channel.queueDeclare(NEURO_QUEUE, true, false, false, null);
        channel.queueDeclare(GERAL_QUEUE, true, false, false, null);

        channel.queueBind(CARDIO_QUEUE, EXCHANGE_NAME, CARDIO_KEY);
        channel.queueBind(NEURO_QUEUE, EXCHANGE_NAME, NEURO_KEY);
        channel.queueBind(GERAL_QUEUE, EXCHANGE_NAME, GERAL_KEY);

        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
            Paciente paciente = gson.fromJson(message, Paciente.class);

            String sintoma = paciente.getSintomas();
            String destinoKey;

            if ("Infarto".equalsIgnoreCase(sintoma) || "Pressão alta".equalsIgnoreCase(sintoma)) {
                paciente.setMedicoResponsavel("Cardiologista");
                destinoKey = CARDIO_KEY;
            } else if ("Dor de cabeça".equalsIgnoreCase(sintoma) ||
                    "Perda dos movimentos de um lado (princípio AVC)".equalsIgnoreCase(sintoma)) {
                paciente.setMedicoResponsavel("Neurologista");
                destinoKey = NEURO_KEY;
            } else {
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

        channel.basicConsume(TRIAGEM_QUEUE, false, deliverCallback, consumerTag -> {});
        Thread.currentThread().join();
    }

    public static void sendPaciente(Channel channel, Paciente paciente, String routingKey) throws Exception {
        String updatedMessage = gson.toJson(paciente);
        channel.basicPublish(EXCHANGE_NAME, routingKey, null, updatedMessage.getBytes("UTF-8"));
        System.out.println("Paciente " + paciente.getNome() +
                " encaminhado para " + paciente.getMedicoResponsavel());
    }
}
