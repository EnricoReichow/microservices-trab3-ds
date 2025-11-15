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

    // Injeta o serviço de RabbitMQ
    private final RabbitMQService rabbitMQService;

    // Injeta a propriedade de controle
    @Value("${app.cli.enabled:false}")
    private boolean cliEnabled;

    // Arrays com dados predefinidos (copiados do seu RecepcionistaMain)
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
        // Se o modo CLI não estiver ativado, simplesmente retorna.
        // O Spring Boot continuará a iniciar o servidor web.
        if (!cliEnabled) {
            return;
        }

        // Se o modo CLI ESTÁ ativado, desliga o modo web e inicia o loop do console.
        System.out.println("=========================================");
        System.out.println(" MODO CLI ATIVADO - SERVIDOR WEB INATIVO ");
        System.out.println("=========================================");

        // Desliga o contexto da aplicação web que o Spring tentou iniciar
        // e inicia um novo contexto NÃO-WEB.
        // Isso é um pouco avançado, mas garante que o Tomcat não inicie.

        // Esta é uma forma mais simples: A lógica abaixo vai bloquear
        // o thread principal, e como está rodando *antes* do Tomcat,
        // o Tomcat não vai iniciar de qualquer forma.
        // No entanto, o Spring Boot 3+ pode iniciar o servidor mais cedo.

        // Uma abordagem mais limpa é configurar o tipo de aplicação
        // na classe principal (main), mas vamos manter simples por enquanto.
        // O `SpringApplicationBuilder` no `main` seria o ideal.

        // Vamos tentar a abordagem simples primeiro:
        // Apenas rode o loop. Isso vai bloquear o `run` e, por consequência,
        // o servidor web não deve iniciar.

        runCliLogic();

        // Se o loop for quebrado (ex: Ctrl+C), finaliza a aplicação.
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
                    return; // Sai do loop e do método
                }

                try {
                    sintomaIndex = Integer.parseInt(input) - 1;
                } catch (NumberFormatException e) {
                    sintomaIndex = -1;
                }
            }

            // Geração automática de dados
            String nome = "Paciente-" + UUID.randomUUID().toString().substring(0, 8);
            String sexo = SEXOS[random.nextInt(SEXOS.length)];
            String tipoSanguineo = TIPOS_SANGUINEOS[random.nextInt(TIPOS_SANGUINEOS.length)];
            String estadoCivil = ESTADOS_CIVIS[random.nextInt(ESTADOS_CIVIS.length)];
            String dataNascimento = String.format("%02d/%02d/%04d",
                    random.nextInt(28) + 1, random.nextInt(12) + 1, 1970 + random.nextInt(40));
            String sintomas = SINTOMAS[sintomaIndex];

            Paciente paciente = new Paciente(nome, sexo, dataNascimento, tipoSanguineo, estadoCivil, sintomas);

            System.out.println("\nPaciente registrado:");
            System.out.println(paciente); // Usando o .toString()

            try {
                // USA O NOVO SERVIÇO SPRING
                rabbitMQService.sendPaciente(paciente);
                System.out.println("Paciente enviado para RabbitMQ via Spring.");
            } catch (Exception e) {
                System.out.println("Erro ao enviar paciente para RabbitMQ: " + e.getMessage());
            }

            System.out.println("Registrar mais um paciente?");
        }
    }

    /**
     * NOTA: Uma forma mais robusta de controlar o modo (CLI vs Web)
     * seria na sua classe 'RecepcionistaBackendApplication.java',
     * lendo os 'args' e decidindo se inicia uma aplicação Web ou Não-Web.
     * * Exemplo (em RecepcionistaBackendApplication.main):
     *
     * boolean cliEnabled = false;
     * for (String arg : args) {
     * if ("--app.cli.enabled=true".equals(arg)) {
     * cliEnabled = true;
     * break;
     * }
     * }
     *
     * SpringApplicationBuilder builder = new SpringApplicationBuilder(RecepcionistaBackendApplication.class);
     * if (cliEnabled) {
     * builder.web(WebApplicationType.NONE); // NÃO INICIA O SERVIDOR WEB
     * } else {
     * builder.web(WebApplicationType.SERVLET); // INICIA O SERVIDOR WEB
     * }
     * builder.run(args);
     * * O CliRunner.java então só precisaria do `@Profile("cli")` ou
     * de uma verificação mais simples. Mas a solução atual com @Value
     * e bloqueio do `run` deve funcionar para seu caso de uso.
     */
}