package Cardiologista;

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
    private static final String CARDIO_QUEUE = "cardiologia";
    private static final String FALLBACK_CARDIO_QUEUE = "fallbackCardio";
    private static final String CARDIO_KEY = "paciente.cardiologia";
    private static final String FALLBACK_CARDIO_KEY = "fallback.cardio";

    // NOVO: Adicionada a routing key para o logger
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

        channel.queueDeclare(CARDIO_QUEUE, true, false, false, null);
        channel.queueDeclare(FALLBACK_CARDIO_QUEUE, true, false, false, null);

        channel.queueBind(CARDIO_QUEUE, EXCHANGE_NAME, CARDIO_KEY);
        channel.queueBind(FALLBACK_CARDIO_QUEUE, DLX_EXCHANGE, FALLBACK_CARDIO_KEY);

        System.out.println("Cardiologista service iniciado. Aguardando pacientes...");

        while (true) {
            GetResponse response = null;

            response = channel.basicGet(CARDIO_QUEUE, false);

            if (response == null) {
                response = channel.basicGet(FALLBACK_CARDIO_QUEUE, false);
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
        System.out.println("Paciente " + paciente.getNome() + " com o sintoma " + paciente.getSintomas() + " foi consultado pelo Cardiologista");
        try {
            paciente.setMedicoResponsavel("Atendido pelo Cardiologista");
            sendPaciente(channel, paciente, LOGGER_KEY);
        } catch (Exception e) {
            System.out.println("Erro ao enviar atualização para o Logger: " + e.getMessage());
        }

        channel.basicAck(response.getEnvelope().getDeliveryTag(), false);
    }

    // NOVO: Método helper para enviar a atualização ao logger
    private static void sendPaciente(Channel channel, Paciente paciente, String routingKey) throws Exception {
        String updatedMessage = gson.toJson(paciente);
        channel.basicPublish(EXCHANGE_NAME, routingKey, null, updatedMessage.getBytes(StandardCharsets.UTF_8));
        System.out.println("...Paciente " + paciente.getNome() + " atualizado no logger (Status: " + paciente.getMedicoResponsavel() + ")");
    }
}
