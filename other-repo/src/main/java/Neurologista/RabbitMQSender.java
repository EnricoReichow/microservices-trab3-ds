package Neurologista;

import TriagemSender.Paciente;
import com.google.gson.Gson;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.GetResponse;

import java.nio.charset.StandardCharsets;

public class RabbitMQSender {

    private static final String EXCHANGE_NAME = "exchangePA";
    private static final String DLX_EXCHANGE = "dlExchange";
    private static final String NEURO_QUEUE = "neurologia";
    private static final String FALLBACK_NEURO_QUEUE = "fallbackNeuro";
    private static final String NEURO_KEY = "paciente.neurologia";
    private static final String FALLBACK_NEURO_KEY = "fallback.neuro";
    private static final String LOGGER_KEY = "paciente.log";

    private static final Gson gson = new Gson();

    public static void start() throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        factory.setUsername("user");
        factory.setPassword("password");

        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        channel.exchangeDeclare(EXCHANGE_NAME, "direct", true);
        channel.exchangeDeclare(DLX_EXCHANGE, "direct", true);

        channel.queueDeclare(NEURO_QUEUE, true, false, false, null);
        channel.queueDeclare(FALLBACK_NEURO_QUEUE, true, false, false, null);

        channel.queueBind(NEURO_QUEUE, EXCHANGE_NAME, NEURO_KEY);
        channel.queueBind(FALLBACK_NEURO_QUEUE, DLX_EXCHANGE, FALLBACK_NEURO_KEY);

        System.out.println("Neurologista service started. Waiting for patients...");

        while (true) {
            GetResponse response;

            response = channel.basicGet(NEURO_QUEUE, false);

            if (response == null) {
                response = channel.basicGet(FALLBACK_NEURO_QUEUE, false);
            }

            if (response == null) {
                Thread.sleep(500);
                continue;
            }

            processPaciente(channel, response);
        }
    }

    private static void processPaciente(Channel channel, GetResponse response) throws Exception {
        String message = new String(response.getBody(), StandardCharsets.UTF_8);
        Paciente paciente = gson.fromJson(message, Paciente.class);
        Thread.sleep(10000);
        System.out.println("Paciente " + paciente.getNome() + " com o sintoma " + paciente.getSintomas() + " foi consultado pelo Neurologista");
        try {
            paciente.setMedicoResponsavel("Atendido pelo Neurologista");
            sendPaciente(channel, paciente, LOGGER_KEY);
        } catch (Exception e) {
            System.out.println("Erro ao enviar atualização para o Logger: " + e.getMessage());
        }

        channel.basicAck(response.getEnvelope().getDeliveryTag(), false);
    }

    private static void sendPaciente(Channel channel, Paciente paciente, String routingKey) throws Exception {
        String updatedMessage = gson.toJson(paciente);
        channel.basicPublish(EXCHANGE_NAME, routingKey, null, updatedMessage.getBytes(StandardCharsets.UTF_8));
        System.out.println("...Paciente " + paciente.getNome() + " atualizado no logger (Status: " + paciente.getMedicoResponsavel() + ")");
    }
}
