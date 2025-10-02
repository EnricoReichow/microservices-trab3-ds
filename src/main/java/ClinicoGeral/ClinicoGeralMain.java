package ClinicoGeral;

public class ClinicoGeralMain {
    public static void main(String[] args) {
        try {
            RabbitMQSender.start();
        } catch (Exception e) {
            System.out.println("Erro ao iniciar serviço do Clínico Geral: " + e.getMessage());
        }
    }
}
