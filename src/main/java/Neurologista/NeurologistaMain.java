package Neurologista;

import com.google.gson.Gson;
import com.rabbitmq.client.*;
import TriagemSender.Paciente;

public class NeurologistaMain {
    private static final String EXCHANGE_NAME = "exchangePA";
    private static final String DLX_EXCHANGE = "dlExchange";
    private static final String NEURO_QUEUE = "neurologia";
    private static final String FALLBACK_NEURO_QUEUE = "fallbackNeuro";
    private static final String NEURO_KEY = "paciente.neurologia";
    private static final String FALLBACK_NEURO_KEY = "fallback.neuro";
    private static final Gson gson = new Gson();

    public static void main(String[] args) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        factory.setUsername("user");
        factory.setPassword("password");
        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {

            // Declare exchanges and queues
            channel.exchangeDeclare(EXCHANGE_NAME, "direct", true);
            channel.exchangeDeclare(DLX_EXCHANGE, "direct", true);
            channel.queueDeclare(NEURO_QUEUE, true, false, false, null);
            channel.queueDeclare(FALLBACK_NEURO_QUEUE, true, false, false, null);
            channel.queueBind(NEURO_QUEUE, EXCHANGE_NAME, NEURO_KEY);
            channel.queueBind(FALLBACK_NEURO_QUEUE, DLX_EXCHANGE, FALLBACK_NEURO_KEY);

            System.out.println("Neurologista service started. Waiting for patients...");

            while (true) {
                GetResponse response = null;
                // Check both queues, process one message at a time
                response = channel.basicGet(NEURO_QUEUE, false);
                if (response == null) {
                    response = channel.basicGet(FALLBACK_NEURO_QUEUE, false);
                }
                if (response == null) {
                    Thread.sleep(500); // No messages, wait a bit
                    continue;
                }

                String message = new String(response.getBody(), "UTF-8");
                Paciente paciente = gson.fromJson(message, Paciente.class);

                Thread.sleep(10000); // Simulate consultation time

                System.out.println("Paciente " + paciente.getNome() +
                        " com o sintoma " + paciente.getSintomas() +
                        " foi consultado pelo Neurologista");

                channel.basicAck(response.getEnvelope().getDeliveryTag(), false);
            }
        }
    }
}
