package Neurologista;

/**
 * Classe principal para inicialização do serviço de Neurologia.
 * <p>
 * Esta classe contém o método main responsável por inicializar o serviço
 * do Neurologista, que irá consumir mensagens de pacientes através do RabbitMQ
 * e processar consultas neurológicas.
 */
public class NeurologistaMain {

    /**
     * Método principal que inicia o serviço do Neurologista.
     * <p>
     * Chama o método start() da classe RabbitMQSender para inicializar
     * o consumidor de mensagens e trata possíveis exceções durante
     * a inicialização do serviço.
     *
     * @param args argumentos da linha de comando (não utilizados)
     */
    public static void main(String[] args) {
        try {
            RabbitMQSender.start();
        } catch (Exception e) {
            System.out.println("Erro ao iniciar serviço do Neurologista: " + e.getMessage());
        }
    }
}
