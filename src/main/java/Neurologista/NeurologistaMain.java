package Neurologista;

public class NeurologistaMain {
    public static void main(String[] args) {
        try {
            RabbitMQSender.start();
        } catch (Exception e) {
            System.out.println("Erro ao iniciar serviço do Neurologista: " + e.getMessage());
        }
    }
}
