package TriagemSender;

public class TriagemSenderMain {
    public static void main(String[] args) {
        try {
            System.out.println("Aguardando pacientes na fila de triagem...");

            RabbitMQSender.start(); // inicia o consumer

        } catch (Exception e) {
            System.out.println("Erro ao iniciar o triagem sender: " + e.getMessage());
        }
    }
}
