package Neurologista;

import TriagemSender.Paciente;
import com.google.gson.Gson;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.GetResponse;

import java.nio.charset.StandardCharsets;

/**
 * Classe responsável pela comunicação com RabbitMQ no serviço de Neurologia.
 * <p>
 * Esta classe gerencia o consumo de mensagens de pacientes das filas
 * de neurologia e fallback, processando consultas neurológicas especializadas.
 */
public class RabbitMQSender {

    private static final String EXCHANGE_NAME = "exchangePA";
    private static final String DLX_EXCHANGE = "dlExchange";
    private static final String NEURO_QUEUE = "neurologia";
    private static final String FALLBACK_NEURO_QUEUE = "fallbackNeuro";
    private static final String NEURO_KEY = "paciente.neurologia";
    private static final String FALLBACK_NEURO_KEY = "fallback.neuro";
    private static final Gson gson = new Gson();

    /**
     * Inicia o consumidor do serviço de Neurologia.
     * <p>
     * Este método estabelece a conexão com o RabbitMQ, configura as exchanges
     * e as filas (principal e de fallback) e entra em um loop infinito para
     * consumir e processar mensagens de pacientes. A lógica prioriza o consumo
     * da fila principal e, caso esteja vazia, tenta consumir da fila de fallback.
     *
     * @throws Exception se ocorrer um erro durante a conexão com o RabbitMQ ou o processamento da mensagem.
     */
    public static void start() throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        factory.setUsername("user");
        factory.setPassword("password");

        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        // Configura exchanges para roteamento normal e de fallback
        channel.exchangeDeclare(EXCHANGE_NAME, "direct", true);
        channel.exchangeDeclare(DLX_EXCHANGE, "direct", true);

        // Declara filas duráveis para persistência de mensagens
        channel.queueDeclare(NEURO_QUEUE, true, false, false, null);
        channel.queueDeclare(FALLBACK_NEURO_QUEUE, true, false, false, null);

        // Vincula as filas às exchanges com suas respectivas routing keys
        channel.queueBind(NEURO_QUEUE, EXCHANGE_NAME, NEURO_KEY);
        channel.queueBind(FALLBACK_NEURO_QUEUE, DLX_EXCHANGE, FALLBACK_NEURO_KEY);

        System.out.println("Neurologista service started. Waiting for patients...");

        while (true) {
            GetResponse response;

            // Prioriza fila principal de neurologia
            response = channel.basicGet(NEURO_QUEUE, false);

            // Se não há pacientes na fila principal, verifica fila de fallback
            // (casos redirecionados pelo Clínico Geral por serem críticos)
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

    /**
     * Processa a mensagem de um paciente recebida da fila.
     * <p>
     * O método deserializa a mensagem JSON para um objeto {@link Paciente},
     * simula o tempo de uma consulta neurológica, exibe uma mensagem de confirmação
     * no console e, por fim, envia um acknowledgement (ACK) para o RabbitMQ,
     * confirmando que a mensagem foi processada com sucesso e pode ser removida da fila.
     *
     * @param channel  O canal de comunicação com o RabbitMQ, usado para enviar o ACK.
     * @param response A resposta do basicGet contendo a mensagem e seus metadados.
     * @throws Exception se ocorrer um erro durante a deserialização ou confirmação da mensagem.
     */
    private static void processPaciente(Channel channel, GetResponse response) throws Exception {
        String message = new String(response.getBody(), StandardCharsets.UTF_8);
        Paciente paciente = gson.fromJson(message, Paciente.class);

        // Simula tempo de consulta neurológica especializada (10 segundos)
        Thread.sleep(10000);

        System.out.println("Paciente " + paciente.getNome() +
                " com o sintoma " + paciente.getSintomas() +
                " foi consultado pelo Neurologista");

        // Confirma processamento bem-sucedido da mensagem
        channel.basicAck(response.getEnvelope().getDeliveryTag(), false);
    }
}
