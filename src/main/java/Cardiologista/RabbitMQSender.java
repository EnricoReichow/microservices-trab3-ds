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
    private static final Gson gson = new Gson();

    /**
     * Inicia o consumidor do serviço de Cardiologia.
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

        // Declara exchanges - o exchangePA é para roteamento normal, dlExchange para fallback
        channel.exchangeDeclare(EXCHANGE_NAME, "direct", true);
        channel.exchangeDeclare(DLX_EXCHANGE, "direct", true);

        // Declara filas duráveis (persistem após restart do RabbitMQ)
        channel.queueDeclare(CARDIO_QUEUE, true, false, false, null);
        channel.queueDeclare(FALLBACK_CARDIO_QUEUE, true, false, false, null);

        // Vincula filas às exchanges com routing keys específicas
        channel.queueBind(CARDIO_QUEUE, EXCHANGE_NAME, CARDIO_KEY);
        channel.queueBind(FALLBACK_CARDIO_QUEUE, DLX_EXCHANGE, FALLBACK_CARDIO_KEY);

        System.out.println("Cardiologista service iniciado. Aguardando pacientes...");

        while (true) {
            GetResponse response = null;

            // Estratégia de priorização: fila principal tem precedência sobre fallback
            response = channel.basicGet(CARDIO_QUEUE, false);

            // Se não houver mensagens na fila principal, verifica fallback
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

    /**
     * Processa a mensagem de um paciente recebida da fila.
     * <p>
     * O método deserializa a mensagem JSON para um objeto {@link Paciente},
     * simula o tempo de uma consulta médica, exibe uma mensagem de confirmação
     * no console e, por fim, envia um acknowledgement (ACK) para o RabbitMQ,
     * confirmando que a mensagem foi processada com sucesso e pode ser removida da fila.
     *
     * @param channel  O canal de comunicação com o RabbitMQ, usado para enviar o ACK.
     * @param response A resposta do `basicGet` contendo a mensagem e seus metadados.
     * @throws Exception se ocorrer um erro durante a deserialização ou confirmação da mensagem.
     */
    private static void processPaciente(Channel channel, GetResponse response) throws Exception {
        String message = new String(response.getBody(), "UTF-8");
        Paciente paciente = gson.fromJson(message, Paciente.class);

        // Simula tempo real de uma consulta cardiológica (10 segundos)
        Thread.sleep(10000);

        System.out.println("Paciente " + paciente.getNome() +
                " com o sintoma " + paciente.getSintomas() +
                " foi consultado pelo Cardiologista");

        // ACK manual - confirma que a mensagem foi processada com sucesso
        // O delivery tag identifica unicamente esta mensagem no canal
        channel.basicAck(response.getEnvelope().getDeliveryTag(), false);
    }
}
