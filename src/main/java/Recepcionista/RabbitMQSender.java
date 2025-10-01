package Recepcionista;

import com.google.gson.Gson;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class RabbitMQSender {
    private static final String EXCHANGE_NAME = "exchangePA";
    private static final String QUEUE_NAME = "triagem";
    private static final String ROUTING_KEY = "paciente.triagem";
    private static Connection connection;
    private static Channel channel;
    private static final Gson gson = new Gson();

    static {
        try {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost("localhost");
            factory.setUsername("user");
            factory.setPassword("password");
            connection = factory.newConnection();
            channel = connection.createChannel();
            channel.exchangeDeclare(EXCHANGE_NAME, "direct", true);
            channel.queueDeclare(QUEUE_NAME, true, false, false, null);
            channel.queueBind(QUEUE_NAME, EXCHANGE_NAME, ROUTING_KEY);
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize RabbitMQ connection: " + e.getMessage(), e);
        }
    }

    public static void sendPaciente(Paciente paciente) throws Exception {
        String message = gson.toJson(paciente);
        channel.basicPublish(EXCHANGE_NAME, ROUTING_KEY, null, message.getBytes("UTF-8"));
    }
}
