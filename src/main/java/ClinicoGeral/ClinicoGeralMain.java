package ClinicoGeral;

import com.google.gson.Gson;
import com.rabbitmq.client.*;
import TriagemSender.Paciente;

public class ClinicoGeralMain {
    private static final String EXCHANGE_NAME = "exchangePA";
    private static final String DLX_EXCHANGE = "dlExchange";
    private static final String[] QUEUES = {"geral", "cardiologia", "neurologia"};
    private static final String[] ROUTING_KEYS = {"paciente.geral", "paciente.cardiologia", "paciente.neurologia"};
    private static final String FALLBACK_CARDIO_QUEUE = "fallbackCardio";
    private static final String FALLBACK_NEURO_QUEUE = "fallbackNeuro";
    private static final String FALLBACK_CARDIO_KEY = "fallback.cardio";
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
            channel.queueDeclare(FALLBACK_CARDIO_QUEUE, true, false, false, null);
            channel.queueDeclare(FALLBACK_NEURO_QUEUE, true, false, false, null);
            channel.queueBind(FALLBACK_CARDIO_QUEUE, DLX_EXCHANGE, FALLBACK_CARDIO_KEY);
            channel.queueBind(FALLBACK_NEURO_QUEUE, DLX_EXCHANGE, FALLBACK_NEURO_KEY);

            for (int i = 0; i < QUEUES.length; i++) {
                channel.queueDeclare(QUEUES[i], true, false, false, null);
                channel.queueBind(QUEUES[i], EXCHANGE_NAME, ROUTING_KEYS[i]);
            }

            System.out.println("ClinicoGeral service started. Waiting for patients...");

            while (true) {
                GetResponse response = null;
                // Priority: geral > cardiologia > neurologia
                for (String queue : QUEUES) {
                    response = channel.basicGet(queue, false);
                    if (response != null) break;
                }
                if (response == null) {
                    Thread.sleep(500); // No messages, wait a bit
                    continue;
                }

                String message = new String(response.getBody(), "UTF-8");
                Paciente paciente = gson.fromJson(message, Paciente.class);

                String sintoma = paciente.getSintomas();
                Thread.sleep(10000); // Simula o atendimento para que conseguimos mostrar filas com mais de 1 paciente

                if ("Infarto".equalsIgnoreCase(sintoma)) {
                    channel.basicPublish(DLX_EXCHANGE, FALLBACK_CARDIO_KEY, null, message.getBytes("UTF-8"));
                    System.out.println("Paciente " + paciente.getNome() + " com sintoma " + paciente.getSintomas() + " redirecionado para fallbackCardio (DLQ).");
                } else if ("Perda dos movimentos de um lado (princípio AVC)".equalsIgnoreCase(sintoma)) {
                    channel.basicPublish(DLX_EXCHANGE, FALLBACK_NEURO_KEY, null, message.getBytes("UTF-8"));
                    System.out.println("Paciente " + paciente.getNome() + " com sintoma " + paciente.getSintomas() + " redirecionado para fallbackNeuro (DLQ).");
                } else {
                    System.out.println("Paciente " + paciente.getNome() + " atendido pelo Clínico Geral. Sintoma: " + sintoma);
                    // Here you can add further processing logic
                }
                channel.basicAck(response.getEnvelope().getDeliveryTag(), false);
            }
        }
    }
}
