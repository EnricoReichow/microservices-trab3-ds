package TriagemSender;

import java.util.HashSet;
import java.util.Set;

/**
 * Classe principal para inicialização do serviço de Triagem com eleição de líder (Bully Algorithm).
 * <p>
 * Esta classe contém o método main responsável por:
 * 1. Inicializar o sistema de eleição de líder distribuído (Algoritmo de Bully)
 * 2. Garantir que apenas o coordenador eleito consuma mensagens da fila de triagem
 * 3. Redistribuir mensagens para as filas especializadas conforme o sintoma apresentado
 * <p>
 * ARGUMENTOS OBRIGATÓRIOS:
 * - args[0]: ID único deste processo (inteiro, ex: 1, 2, 3, ...)
 * - args[1]: Lista de IDs conhecidos separados por vírgula (ex: "1,2,3,4,5")
 *
 * Fallback:
 * - Variáveis de ambiente: TRIAGEM_ID e TRIAGEM_IDS (ex: TRIAGEM_ID=1 TRIAGEM_IDS="1,2,3")
 * - Se nada for informado, o serviço roda em modo single-node (ID=1, IDS=1) com aviso.
 *
 * EXEMPLO DE USO:
 * java TriagemSender.TriagemSenderMain 1 "1,2,3"
 * java TriagemSender.TriagemSenderMain 2 "1,2,3"
 * java TriagemSender.TriagemSenderMain 3 "1,2,3"
 */
public class TriagemSenderMain {

    /**
     * Método principal que inicia o serviço de Triagem com eleição de líder.
     *
     * @param args [0] = ID do processo, [1] = IDs conhecidos separados por vírgula
     */
    public static void main(String[] args) {
        // Validação dos argumentos com fallback para variáveis de ambiente
        String idArg = null;
        String idsArg = null;

        if (args.length >= 2) {
            idArg = args[0];
            idsArg = args[1];
        } else {
            // tenta obter de variáveis de ambiente
            String envId = System.getenv("TRIAGEM_ID");
            String envIds = System.getenv("TRIAGEM_IDS");

            if (args.length == 1) {
                idArg = args[0];
                idsArg = envIds; // pode ser null
            } else if (envId != null || envIds != null) {
                // se qualquer uma das env vars existir, usa as que existirem
                idArg = envId;
                idsArg = envIds;
                System.out.println("[INFO] Usando variáveis de ambiente TRIAGEM_ID/TRIAGEM_IDS como fallback (parcialmente ou totalmente)");
            }
        }

        // Novas regras de fallback para evitar erro crítico na inicialização
        if (idArg == null && idsArg == null) {
            System.out.println("[WARN] Nenhum argumento ou variável de ambiente (TRIAGEM_ID/TRIAGEM_IDS) fornecido. Iniciando em modo single-node com ID=1.");
            idArg = "1";
            idsArg = "1";
        } else if (idArg != null && idsArg == null) {
            // Se ID foi informado mas lista não, assume que só conhece a si próprio
            System.out.println("[WARN] IDs conhecidos não fornecidos. Assumindo modo single-node (somente este ID: " + idArg + ")");
            idsArg = idArg;
        } else if (idArg == null && idsArg != null) {
            // Se apenas a lista foi informada, usa o primeiro ID da lista como ID deste processo
            String firstId = idsArg.split(",")[0].trim();
            System.out.println("[WARN] ID do processo não fornecido. Usando o primeiro ID da lista como processId: " + firstId);
            idArg = firstId;
        }

        try {
            // Parse do ID do processo
            int processId = Integer.parseInt(idArg);

            // Parse dos IDs conhecidos
            Set<Integer> knownProcessIds = new HashSet<>();
            String[] ids = idsArg.split(",");
            for (String id : ids) {
                knownProcessIds.add(Integer.parseInt(id.trim()));
            }

            System.out.println("╔════════════════════════════════════════════════════════════╗");
            System.out.println("║     SERVIÇO DE TRIAGEM COM ELEIÇÃO DE LÍDER (BULLY)       ║");
            System.out.println("╠════════════════════════════════════════════════════════════╣");
            System.out.println("║  Processo ID: " + String.format("%-44d", processId) + "║");
            System.out.println("║  IDs Conhecidos: " + String.format("%-39s", knownProcessIds) + "║");
            System.out.println("╚════════════════════════════════════════════════════════════╝");

            // Inicializa o RabbitMQSender (configura filas e exchanges)
            RabbitMQSender.initialize();

            // Cria instância do Bully Election
            BullyElection bullyElection = new BullyElection(processId, knownProcessIds);

            // Configura listener para mudanças de liderança
            bullyElection.setLeaderChangeListener(new BullyElection.LeaderChangeListener() {
                @Override
                public void onBecomeLeader() {
                    System.out.println("╔════════════════════════════════════════════════════════════╗");
                    System.out.println("║        ★★★ PROCESSO " + String.format("%-2d", processId) + " VIROU COORDENADOR ★★★           ║");
                    System.out.println("║     Iniciando consumo da fila de triagem...                ║");
                    System.out.println("╚════════════════════════════════════════════════════════════╝");
                    try {
                        RabbitMQSender.startConsuming();
                    } catch (Exception e) {
                        System.err.println("[ERRO] Falha ao iniciar consumo: " + e.getMessage());
                        e.printStackTrace();
                    }
                }

                @Override
                public void onLoseLeadership() {
                    System.out.println("╔════════════════════════════════════════════════════════════╗");
                    System.out.println("║      ⚠️  PROCESSO " + String.format("%-2d", processId) + " PERDEU COORDENAÇÃO ⚠️             ║");
                    System.out.println("║     Parando consumo da fila de triagem...                  ║");
                    System.out.println("╚════════════════════════════════════════════════════════════╝");
                    try {
                        RabbitMQSender.stopConsuming();
                    } catch (Exception e) {
                        System.err.println("[ERRO] Falha ao parar consumo: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            });

            // Inicia o sistema de eleição
            bullyElection.start();

            System.out.println("\n[INFO] Sistema iniciado. Aguardando eleição e mensagens...\n");

            // Adiciona shutdown hook para limpeza
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("\n[INFO] Desligando serviço de triagem...");
                bullyElection.shutdown();
                RabbitMQSender.shutdown();
                System.out.println("[INFO] Serviço encerrado.");
            }));

            // Mantém a aplicação rodando
            Thread.currentThread().join();

        } catch (NumberFormatException e) {
            System.err.println("[ERRO] IDs devem ser números inteiros válidos: " + e.getMessage());
            System.exit(1);
        } catch (Exception e) {
            System.err.println("[ERRO] Erro ao iniciar o serviço de triagem: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
