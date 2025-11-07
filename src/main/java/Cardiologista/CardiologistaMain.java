package Cardiologista;

import com.google.gson.Gson;
import com.rabbitmq.client.*;
import TriagemSender.Paciente;

public class CardiologistaMain {
    private static final String EXCHANGE_NAME = "exchangePA";
    private static final String DLX_EXCHANGE = "dlExchange";
    private static final String CARDIO_QUEUE = "cardiologia";
    private static final String FALLBACK_CARDIO_QUEUE = "fallbackCardio";
    private static final String CARDIO_KEY = "paciente.cardiologia";
    private static final String FALLBACK_CARDIO_KEY = "fallback.cardio";
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
            channel.queueDeclare(CARDIO_QUEUE, true, false, false, null);
            channel.queueDeclare(FALLBACK_CARDIO_QUEUE, true, false, false, null);
            channel.queueBind(CARDIO_QUEUE, EXCHANGE_NAME, CARDIO_KEY);
            channel.queueBind(FALLBACK_CARDIO_QUEUE, DLX_EXCHANGE, FALLBACK_CARDIO_KEY);

            System.out.println("Cardiologista service started. Waiting for patients...");

            while (true) {
                GetResponse response = null;
                // Check both queues, process one message at a time
                response = channel.basicGet(CARDIO_QUEUE, false);
                if (response == null) {
                    response = channel.basicGet(FALLBACK_CARDIO_QUEUE, false);
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
                        " foi consultado pelo Cardiologista");

                channel.basicAck(response.getEnvelope().getDeliveryTag(), false);
            }
        }
    }
}
