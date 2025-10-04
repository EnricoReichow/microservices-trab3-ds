package Recepcionista;

import java.util.*;

/**
 * Classe principal para o sistema de recepção de pacientes.
 * <p>
 * Esta classe simula a interface de uma recepção hospitalar onde são
 * cadastrados pacientes com sintomas específicos. O sistema permite
 * a seleção de sintomas de uma lista predefinida e gera automaticamente
 * dados pessoais aleatórios para os pacientes, enviando-os para o
 * sistema de mensageria RabbitMQ para processamento.
 */
public class RecepcionistaMain {
    // Arrays com dados predefinidos para geração aleatória de perfis de pacientes
    private static final String[] SEXOS = {"Masculino", "Feminino"};
    private static final String[] TIPOS_SANGUINEOS = {"A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"};
    private static final String[] ESTADOS_CIVIS = {"Solteiro", "Casado", "Divorciado", "Viúvo"};
    // Lista de sintomas disponíveis para seleção pelo usuário
    private static final String[] SINTOMAS = {
            "Infarto",
            "Dor de cabeça",
            "Perda dos movimentos de um lado (princípio AVC)",
            "Dor de barriga",
            "Pressão alta",
            "Vômito náusea",
            "Dor muscular"
    };

    /**
     * Método principal que inicia o sistema de recepção de pacientes.
     * <p>
     * Executa um loop infinito onde o usuário pode selecionar sintomas de uma
     * lista predefinida. Para cada seleção, gera automaticamente dados pessoais
     * aleatórios (nome, sexo, tipo sanguíneo, estado civil, data de nascimento),
     * cria um objeto Paciente e o envia para o RabbitMQ através do RabbitMQSender.
     * O processo continua até que o usuário interrompa a execução (Ctrl+C).
     *
     * @param args argumentos da linha de comando (não utilizados)
     */
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Random random = new Random();

        while (true) {
            // Exibe menu de sintomas para seleção do usuário
            System.out.println("Qual o sintoma?");
            for (int i = 0; i < SINTOMAS.length; i++) {
                System.out.printf("%d - %s%n", i + 1, SINTOMAS[i]);
            }

            // Validação da entrada do usuário para seleção de sintoma
            int sintomaIndex = -1;
            while (sintomaIndex < 0 || sintomaIndex >= SINTOMAS.length) {
                System.out.print("Escolha o número do sintoma: ");
                try {
                    sintomaIndex = Integer.parseInt(scanner.nextLine()) - 1;
                } catch (NumberFormatException e) {
                    sintomaIndex = -1; // Reset em caso de entrada inválida
                }
            }

            // Geração automática de dados pessoais aleatórios para o paciente
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
                // Envia o paciente para a fila de triagem via RabbitMQ
                RabbitMQSender.sendPaciente(paciente);
                System.out.println("Paciente enviado para RabbitMQ.");
            } catch (Exception e) {
                System.out.println("Erro ao enviar paciente para RabbitMQ: " + e.getMessage());
            }

            // Loop infinito - o usuário deve usar Ctrl+C para sair
            System.out.println("Registrar mais um paciente? (pressione Ctrl+C para sair)\n");
        }
    }
}
