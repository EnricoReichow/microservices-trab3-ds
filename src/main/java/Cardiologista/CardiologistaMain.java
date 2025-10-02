package Cardiologista;

public class CardiologistaMain {
    public static void main(String[] args) {
        try {
            RabbitMQSender.start();
        } catch (Exception e) {
            System.out.println("Erro ao iniciar serviço do Cardiologista: " + e.getMessage());
        }
    }
}
