package Recepcionista;

import com.google.gson.Gson;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

/**
 * Classe responsável pelo envio de mensagens de pacientes para o RabbitMQ.
 * <p>
 * Esta classe gerencia a conexão com o RabbitMQ e fornece funcionalidades
 * para enviar objetos Paciente para a fila de triagem. A conexão é inicializada
 * estaticamente para reutilização durante toda a execução da aplicação.
 */
public class RabbitMQSender {
    private static final String EXCHANGE_NAME = "exchangePA";
    private static final String QUEUE_NAME = "triagem";
    private static final String ROUTING_KEY = "paciente.triagem";

    // Conexão e canal são mantidos como variáveis estáticas para reutilização
    private static Connection connection;
    private static Channel channel;
    private static final Gson gson = new Gson();

    // Bloco estático - inicializa a conexão RabbitMQ uma única vez quando a classe é carregada
    static {
        try {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost("localhost");
            factory.setUsername("user");
            factory.setPassword("password");

            // Estabelece conexão persistente
            connection = factory.newConnection();
            channel = connection.createChannel();

            // Configura infraestrutura RabbitMQ (exchange, fila e binding)
            channel.exchangeDeclare(EXCHANGE_NAME, "direct", true);
            channel.queueDeclare(QUEUE_NAME, true, false, false, null);
            channel.queueBind(QUEUE_NAME, EXCHANGE_NAME, ROUTING_KEY);
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize RabbitMQ connection: " + e.getMessage(), e);
        }
    }

    /**
     * Envia um objeto Paciente para a fila de triagem do RabbitMQ.
     * <p>
     * O método serializa o objeto Paciente para JSON usando Gson e
     * publica a mensagem na exchange configurada com a routing key
     * de triagem. A mensagem será roteada para a fila de triagem
     * onde será processada pelo sistema de triagem.
     *
     * @param paciente O objeto Paciente a ser enviado para a fila
     * @throws Exception se ocorrer erro durante a serialização ou envio da mensagem
     */
    public static void sendPaciente(Paciente paciente) throws Exception {
        // Serializa o objeto Paciente para JSON
        String message = gson.toJson(paciente);
        // Publica na exchange com routing key de triagem - será roteado para fila de triagem
        channel.basicPublish(EXCHANGE_NAME, ROUTING_KEY, null, message.getBytes("UTF-8"));
    }
}
