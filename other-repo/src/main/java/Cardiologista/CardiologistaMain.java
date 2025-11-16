package Cardiologista;

/**
 * Classe principal para inicialização do serviço de Cardiologia.
 * <p>
 * Esta classe contém o método main responsável por inicializar o serviço
 * do Cardiologista, que irá consumir mensagens de pacientes através do RabbitMQ
 * e processar consultas cardiológicas.
 */
public class CardiologistaMain {

    /**
     * Método principal que inicia o serviço do Cardiologista.
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
            System.out.println("Erro ao iniciar serviço do Cardiologista: " + e.getMessage());
        }
    }
}
