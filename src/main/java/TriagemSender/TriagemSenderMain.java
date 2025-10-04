package TriagemSender;

/**
 * Classe principal para inicialização do serviço de Triagem.
 * <p>
 * Esta classe contém o método main responsável por inicializar o serviço
 * de triagem, que irá consumir mensagens de pacientes da fila de triagem
 * e redistribuí-las para as filas especializadas conforme o sintoma apresentado.
 */
public class TriagemSenderMain {

    /**
     * Método principal que inicia o serviço de Triagem.
     * <p>
     * Chama o método start() da classe RabbitMQSender para inicializar
     * o consumidor de mensagens da fila de triagem e trata possíveis
     * exceções durante a inicialização do serviço.
     *
     * @param args argumentos da linha de comando (não utilizados)
     */
    public static void main(String[] args) {
        try {
            System.out.println("Aguardando pacientes na fila de triagem...");

            RabbitMQSender.start(); // inicia o consumer

        } catch (Exception e) {
            System.out.println("Erro ao iniciar o triagem sender: " + e.getMessage());
        }
    }
}
