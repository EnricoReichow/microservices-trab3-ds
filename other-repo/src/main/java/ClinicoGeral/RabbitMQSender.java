package ClinicoGeral;

import com.google.gson.Gson;
import com.rabbitmq.client.*;
import TriagemSender.Paciente;

import java.nio.charset.StandardCharsets;

/**
 * Classe responsável pela comunicação com RabbitMQ no serviço de Clínico Geral.
 * <p>
 * Esta classe gerencia o consumo de mensagens de pacientes de múltiplas filas
 * (geral, cardiologia, neurologia) e implementa a lógica de triagem, redirecionando
 * casos específicos para filas de fallback ou atendendo diretamente.
 */
public class RabbitMQSender {
    private static final String EXCHANGE_NAME = "exchangePA";
    private static final String DLX_EXCHANGE = "dlExchange";

    private static final String[] QUEUES = {"geral", "cardiologia", "neurologia"};
    private static final String[] ROUTING_KEYS = {"paciente.geral", "paciente.cardiologia", "paciente.neurologia"};

    private static final String FALLBACK_CARDIO_QUEUE = "fallbackCardio";
    private static final String FALLBACK_NEURO_QUEUE = "fallbackNeuro";
    private static final String FALLBACK_CARDIO_KEY = "fallback.cardio";
    private static final String FALLBACK_NEURO_KEY = "fallback.neuro";

    private static final Gson gson = new Gson();

    /**
     * Inicia o consumidor do serviço de Clínico Geral.
     * <p>
     * Este método estabelece conexão com o RabbitMQ, configura as exchanges e filas
     * (incluindo filas de fallback) e entra em um loop infinito para consumir mensagens.
     * O serviço prioriza o consumo das filas na ordem: geral > cardiologia > neurologia.
     * Casos específicos como "Infarto" e "AVC" são redirecionados para filas de fallback.
     *
     * @throws Exception se ocorrer um erro durante a conexão com o RabbitMQ ou processamento.
     */
    public static void start() throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        factory.setUsername("user");
        factory.setPassword("password");

        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        channel.exchangeDeclare(EXCHANGE_NAME, "direct", true);
        channel.exchangeDeclare(DLX_EXCHANGE, "direct", true);

        // Declara filas de fallback para redirecionamento de casos críticos
        channel.queueDeclare(FALLBACK_CARDIO_QUEUE, true, false, false, null);
        channel.queueDeclare(FALLBACK_NEURO_QUEUE, true, false, false, null);

        channel.queueBind(FALLBACK_CARDIO_QUEUE, DLX_EXCHANGE, FALLBACK_CARDIO_KEY);
        channel.queueBind(FALLBACK_NEURO_QUEUE, DLX_EXCHANGE, FALLBACK_NEURO_KEY);

        // Declara e vincula múltiplas filas que o Clínico Geral pode atender
        for (int i = 0; i < QUEUES.length; i++) {
            channel.queueDeclare(QUEUES[i], true, false, false, null);
            channel.queueBind(QUEUES[i], EXCHANGE_NAME, ROUTING_KEYS[i]);
        }

        System.out.println("Clínico Geral service started. Waiting for patients...");

        while (true) {
            GetResponse response = null;

            // Estratégia de priorização: geral > cardiologia > neurologia
            // O Clínico Geral atende múltiplas especialidades quando os especialistas não estão disponíveis
            for (String queue : QUEUES) {
                response = channel.basicGet(queue, false);
                if (response != null) break; // Para no primeiro paciente encontrado
            }

            if (response == null) {
                Thread.sleep(500);
                continue;
            }

            processPaciente(channel, response);
        }
    }

    /**
     * Processa a mensagem de um paciente recebida da fila.
     * <p>
     * O método deserializa a mensagem JSON para um objeto {@link Paciente},
     * avalia o sintoma e decide se o paciente deve ser redirecionado para
     * uma fila de fallback (casos de "Infarto" ou "AVC") ou atendido pelo
     * Clínico Geral. Simula o tempo de consulta e confirma o processamento.
     *
     * @param channel  O canal de comunicação com o RabbitMQ
     * @param response A resposta do basicGet contendo a mensagem e metadados
     * @throws Exception se ocorrer erro durante deserialização ou confirmação
     */
    private static void processPaciente(Channel channel, GetResponse response) throws Exception {
        String message = new String(response.getBody(), StandardCharsets.UTF_8);
        Paciente paciente = gson.fromJson(message, Paciente.class);

        String sintoma = paciente.getSintomas();

        // Simula tempo de avaliação clínica
        Thread.sleep(10000);

        // Lógica de redirecionamento para casos críticos que precisam de especialista urgente
        if ("Infarto".equalsIgnoreCase(sintoma)) {
            // Redireciona para fila de fallback do cardiologista (caso crítico)
            channel.basicPublish(DLX_EXCHANGE, FALLBACK_CARDIO_KEY, null, message.getBytes(StandardCharsets.UTF_8));
            System.out.println("Paciente " + paciente.getNome() + " com sintoma " + sintoma + " redirecionado para fallbackCardio (DLQ).");
        } else if ("Perda dos movimentos de um lado (princípio AVC)".equalsIgnoreCase(sintoma)) {
            // Redireciona para fila de fallback do neurologista (caso crítico)
            channel.basicPublish(DLX_EXCHANGE, FALLBACK_NEURO_KEY, null, message.getBytes(StandardCharsets.UTF_8));
            System.out.println("Paciente " + paciente.getNome() + " com sintoma " + sintoma + " redirecionado para fallbackNeuro (DLQ).");
        } else {
            // Casos que podem ser tratados pelo próprio Clínico Geral
            System.out.println("Paciente " + paciente.getNome() + " atendido pelo Clínico Geral. Sintoma: " + sintoma);
        }

        channel.basicAck(response.getEnvelope().getDeliveryTag(), false);
    }
}
