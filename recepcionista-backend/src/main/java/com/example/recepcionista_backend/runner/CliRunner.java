package com.example.recepcionista_backend.runner;

import com.example.recepcionista_backend.model.Paciente;
import com.example.recepcionista_backend.service.RabbitMQService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;
import java.util.Random;
import java.util.Scanner;
import java.util.UUID;

@Component
public class CliRunner implements CommandLineRunner {

    private final RabbitMQService rabbitMQService;

    @Value("${app.cli.enabled:false}")
    private boolean cliEnabled;

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

    public CliRunner(RabbitMQService rabbitMQService) {
        this.rabbitMQService = rabbitMQService;
    }

    @Override
    public void run(String... args) throws Exception {
        if (!cliEnabled) {
            return;
        }

        System.out.println("=========================================");
        System.out.println(" MODO CLI ATIVADO - SERVIDOR WEB INATIVO ");
        System.out.println("=========================================");

        runCliLogic();

        System.exit(0);
    }

    /**
     * Contém a lógica exata do seu RecepcionistaMain.java,
     * mas agora usa o RabbitMQService injetado.
     */
    private void runCliLogic() {
        Scanner scanner = new Scanner(System.in);
        Random random = new Random();

        while (true) {
            System.out.println("\nQual o sintoma?");
            for (int i = 0; i < SINTOMAS.length; i++) {
                System.out.printf("%d - %s%n", i + 1, SINTOMAS[i]);
            }

            int sintomaIndex = -1;
            while (sintomaIndex < 0 || sintomaIndex >= SINTOMAS.length) {
                System.out.print("Escolha o número do sintoma (ou 'exit' para sair): ");
                String input = scanner.nextLine();

                if ("exit".equalsIgnoreCase(input)) {
                    System.out.println("Saindo do modo CLI.");
                    return;
                }

                try {
                    sintomaIndex = Integer.parseInt(input) - 1;
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
            System.out.println(paciente);

            try {
                rabbitMQService.sendPaciente(paciente);
                System.out.println("Paciente enviado para RabbitMQ via Spring.");
            } catch (Exception e) {
                System.out.println("Erro ao enviar paciente para RabbitMQ: " + e.getMessage());
            }

            System.out.println("Registrar mais um paciente?");
        }
    }
}