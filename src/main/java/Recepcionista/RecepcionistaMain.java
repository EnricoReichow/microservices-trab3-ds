package Recepcionista;

import java.util.*;

public class RecepcionistaMain {
    private static final String[] SEXOS = {"Masculino", "Feminino"};
    private static final String[] TIPOS_SANGUINEOS = {"A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"};
    private static final String[] ESTADOS_CIVIS = {"Solteiro", "Casado", "Divorciado", "Viúvo"};
    private static final String[] SINTOMAS = {
            "Infarto",
            "Dor de cabeça",
            "Perda dos movimentos de um lado (princípio AVC)",
            "Dor de barriga",
            "Pressão alta",
            "Vômito náusea",
            "Dor muscular"
    };

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Random random = new Random();

        while (true) {
            System.out.println("Qual o sintoma?");
            for (int i = 0; i < SINTOMAS.length; i++) {
                System.out.printf("%d - %s%n", i + 1, SINTOMAS[i]);
            }
            int sintomaIndex = -1;
            while (sintomaIndex < 0 || sintomaIndex >= SINTOMAS.length) {
                System.out.print("Escolha o número do sintoma: ");
                try {
                    sintomaIndex = Integer.parseInt(scanner.nextLine()) - 1;
                } catch (NumberFormatException e) {
                    sintomaIndex = -1;
                }
            }

            String nome = "Paciente-" + UUID.randomUUID().toString().substring(0, 8);
            String sexo = SEXOS[random.nextInt(SEXOS.length)];
            String tipoSanguineo = TIPOS_SANGUINEOS[random.nextInt(TIPOS_SANGUINEOS.length)];
            String estadoCivil = ESTADOS_CIVIS[random.nextInt(ESTADOS_CIVIS.length)];
            String dataNascimento = String.format("%02d/%02d/%04d",
                    random.nextInt(28) + 1, random.nextInt(12) + 1, 1970 + random.nextInt(40));
            String sintomas = SINTOMAS[sintomaIndex];

            Paciente paciente = new Paciente(nome, sexo, dataNascimento, tipoSanguineo, estadoCivil, sintomas);

            System.out.println("\nPaciente registrado:");
            System.out.println("Nome: " + paciente.getNome());
            System.out.println("Sexo: " + paciente.getSexoBiologico());
            System.out.println("Data de Nascimento: " + paciente.getDataNascimento());
            System.out.println("Tipo Sanguíneo: " + paciente.getTipoSanguineo());
            System.out.println("Estado Civil: " + paciente.getEstadoCivil());
            System.out.println("Sintomas: " + paciente.getSintomas());
            System.out.println();

            try {
                RabbitMQSender.sendPaciente(paciente);
                System.out.println("Paciente enviado para RabbitMQ.");
            } catch (Exception e) {
                System.out.println("Erro ao enviar paciente para RabbitMQ: " + e.getMessage());
            }


            System.out.println("Registrar mais um paciente? (pressione Ctrl+C para sair)\n");
        }
    }
}
