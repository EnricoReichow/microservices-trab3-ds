package TriagemSender;

import com.google.gson.Gson;
import com.rabbitmq.client.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Implementação do Algoritmo de Bully para eleição de líder distribuído.
 * <p>
 * O processo com o maior ID é eleito como coordenador.
 * Apenas o coordenador processa mensagens da fila de triagem.
 */
public class BullyElection {
    private static final String ELECTION_EXCHANGE = "election_exchange";
    private static final String COORDINATOR_EXCHANGE = "coordinator_exchange";
    private String electionExchangeName = ELECTION_EXCHANGE;
    private static final String HEARTBEAT_EXCHANGE = "heartbeat_exchange";
    private static final int HEARTBEAT_INTERVAL_MS = 2000;
    private static final int HEARTBEAT_TIMEOUT_MS = 6000;
    private static final int ELECTION_TIMEOUT_MS = 3000;

    private final int processId;
    private final Set<Integer> knownProcessIds;
    private final AtomicInteger currentLeaderId;
    private final AtomicBoolean isLeader;
    private final AtomicBoolean electionInProgress;

    private Connection connection;
    private Channel channel;
    private String myElectionQueue;

    private final Gson gson = new Gson();
    private ScheduledExecutorService heartbeatScheduler;
    private ScheduledExecutorService leaderMonitorScheduler;
    private ScheduledFuture<?> heartbeatTask;
    private ScheduledFuture<?> monitorTask;

    private long lastHeartbeatReceived;
    private final ConcurrentHashMap<Integer, Long> processLastSeen = new ConcurrentHashMap<>();

    private volatile LeaderChangeListener leaderChangeListener;
    private final AtomicBoolean okReceived = new AtomicBoolean(false);

    public interface LeaderChangeListener {
        void onBecomeLeader();
        void onLoseLeadership();
    }

    /**
     * Mensagens trocadas durante o processo de eleição
     */
    private static class ElectionMessage {
        String type; // ELECTION, OK, COORDINATOR
        int senderId;

        ElectionMessage(String type, int senderId) {
            this.type = type;
            this.senderId = senderId;
        }
    }

    public BullyElection(int processId, Set<Integer> knownProcessIds) {
        this.processId = processId;
        this.knownProcessIds = new HashSet<>(knownProcessIds);
        this.currentLeaderId = new AtomicInteger(-1);
        this.isLeader = new AtomicBoolean(false);
        this.electionInProgress = new AtomicBoolean(false);
        this.lastHeartbeatReceived = System.currentTimeMillis();
    }

    public void setLeaderChangeListener(LeaderChangeListener listener) {
        this.leaderChangeListener = listener;
    }

    /**
     * Inicializa a conexão RabbitMQ e configura as filas de eleição
     */
    public void start() throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        factory.setUsername("election");
        factory.setPassword("password");

        connection = factory.newConnection();
        channel = connection.createChannel();

        electionExchangeName = ELECTION_EXCHANGE + "_direct";
        channel.exchangeDeclare(electionExchangeName, "direct", false);
        System.out.println("[BULLY] Usando exchange de eleição: " + electionExchangeName);

        // Exchange para anúncio de coordenador (broadcast)
        channel.exchangeDeclare(COORDINATOR_EXCHANGE, "fanout", false);

        // Exchange para heartbeat (broadcast)
        channel.exchangeDeclare(HEARTBEAT_EXCHANGE, "fanout", false);

        // Fila nomeada por ID para receber mensagens direcionadas de eleição/OK
        myElectionQueue = "election_" + processId;
        channel.queueDeclare(myElectionQueue, false, true, true, null);
        channel.queueBind(myElectionQueue, electionExchangeName, myElectionQueue);

        // Fila para receber anúncios de coordenador (fanout)
        String coordinatorQueue = channel.queueDeclare().getQueue();
        channel.queueBind(coordinatorQueue, COORDINATOR_EXCHANGE, "");

        // Fila para receber heartbeats
        String heartbeatQueue = channel.queueDeclare().getQueue();
        channel.queueBind(heartbeatQueue, HEARTBEAT_EXCHANGE, "");

        // Consumer para mensagens de eleição (direcionadas a este processo)
        DeliverCallback electionCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
            ElectionMessage msg = gson.fromJson(message, ElectionMessage.class);
            handleElectionMessage(msg);
        };
        channel.basicConsume(myElectionQueue, true, electionCallback, consumerTag -> {});

        // Consumer para anúncio de coordenador (broadcast)
        DeliverCallback coordinatorCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
            ElectionMessage msg = gson.fromJson(message, ElectionMessage.class);
            if ("COORDINATOR".equals(msg.type)) {
                handleElectionMessage(msg);
            }
        };
        channel.basicConsume(coordinatorQueue, true, coordinatorCallback, consumerTag -> {});

        // Consumer para heartbeats
        DeliverCallback heartbeatCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
            ElectionMessage msg = gson.fromJson(message, ElectionMessage.class);
            if ("HEARTBEAT".equals(msg.type)) {
                processLastSeen.put(msg.senderId, System.currentTimeMillis());
                if (msg.senderId == currentLeaderId.get()) {
                    lastHeartbeatReceived = System.currentTimeMillis();
                }
            }
        };
        channel.basicConsume(heartbeatQueue, true, heartbeatCallback, consumerTag -> {});

        System.out.println("[BULLY] Processo " + processId + " iniciado. IDs conhecidos: " + knownProcessIds);

        Thread.sleep(1000);

        startElection();

        startLeaderMonitoring();
    }

    /**
     * Processa mensagens de eleição recebidas
     */
    private void handleElectionMessage(ElectionMessage msg) {
        System.out.println("[BULLY] Processo " + processId + " recebeu " + msg.type + " de " + msg.senderId);

        switch (msg.type) {
            case "ELECTION":
                // Se receber ELECTION de processo com ID menor, responde OK
                if (msg.senderId < processId) {
                    // Responde OK diretamente ao remetente (fila dele)
                    sendElectionMessageTo(msg.senderId, new ElectionMessage("OK", processId));
                    // Inicia própria eleição se ainda não estiver em andamento
                    if (!electionInProgress.get()) {
                        new Thread(() -> startElection()).start();
                    }
                }
                break;

            case "OK":
                okReceived.set(true);
                System.out.println("[BULLY] Processo " + processId + " recebeu OK (há processo maior respondendo)");
                break;

            case "COORDINATOR":
                // Novo coordenador anunciado
                int newLeaderId = msg.senderId;
                int oldLeaderId = currentLeaderId.getAndSet(newLeaderId);
                lastHeartbeatReceived = System.currentTimeMillis();

                if (newLeaderId == processId) {
                    if (!isLeader.getAndSet(true)) {
                        System.out.println("[BULLY] ★★★ Processo " + processId + " É O NOVO COORDENADOR ★★★");
                        startHeartbeat();
                        if (leaderChangeListener != null) {
                            leaderChangeListener.onBecomeLeader();
                        }
                    }
                } else {
                    if (isLeader.getAndSet(false)) {
                        System.out.println("[BULLY] Processo " + processId + " perdeu liderança. Novo líder: " + newLeaderId);
                        stopHeartbeat();
                        if (leaderChangeListener != null) {
                            leaderChangeListener.onLoseLeadership();
                        }
                    } else {
                        System.out.println("[BULLY] Processo " + processId + " reconhece " + newLeaderId + " como coordenador");
                    }
                }
                electionInProgress.set(false);
                break;
        }
    }

    /**
     * Inicia processo de eleição (Algoritmo Bully)
     */
    public void startElection() {
        if (electionInProgress.getAndSet(true)) {
            System.out.println("[BULLY] Eleição já em andamento, ignorando...");
            return;
        }

        System.out.println("[BULLY] ⚡ Processo " + processId + " INICIANDO ELEIÇÃO ⚡");

        Set<Integer> higherProcesses = new HashSet<>();
        for (Integer id : knownProcessIds) {
            if (id > processId) {
                higherProcesses.add(id);
            }
        }

        okReceived.set(false);

        if (higherProcesses.isEmpty()) {
            becomeCoordinator();
        } else {
            for (Integer higherId : higherProcesses) {
                sendElectionMessageTo(higherId, new ElectionMessage("ELECTION", processId));
            }

            // Aguarda respostas OK por ELECTION_TIMEOUT_MS
            ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
            executor.schedule(() -> {
                if (electionInProgress.get()) {
                    if (!okReceived.get()) {
                        System.out.println("[BULLY] Timeout aguardando OK, assumindo coordenação");
                        becomeCoordinator();
                    } else {
                        System.out.println("[BULLY] OK recebido de processo maior; aguardando anúncio de COORDINATOR...");
                        // aguarda anúncio do COORDINATOR (startLeaderMonitoring lidará com timeouts)
                    }
                }
                executor.shutdown();
            }, ELECTION_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        }
    }

    /**
     * Declara-se como novo coordenador
     */
    private void becomeCoordinator() {
        currentLeaderId.set(processId);
        isLeader.set(true);
        electionInProgress.set(false);

        // Anuncia para todos que é o coordenador (broadcast)
        try {
            String json = gson.toJson(new ElectionMessage("COORDINATOR", processId));
            channel.basicPublish(COORDINATOR_EXCHANGE, "", null, json.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            System.err.println("[BULLY] Erro ao anunciar coordenador: " + e.getMessage());
        }

        System.out.println("[BULLY] ★★★ Processo " + processId + " É O NOVO COORDENADOR ★★★");

        startHeartbeat();

        if (leaderChangeListener != null) {
            leaderChangeListener.onBecomeLeader();
        }
    }

    /**
     * Envia ELECTION/OK direcionado para um processo específico usando exchange direct
     */
    private void sendElectionMessageTo(int targetId, ElectionMessage msg) {
        try {
            String routingKey = "election_" + targetId;
            String json = gson.toJson(msg);
            channel.basicPublish(electionExchangeName, routingKey, null, json.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            System.err.println("[BULLY] Erro ao enviar mensagem direcionada: " + e.getMessage());
        }
    }

    /**
     * Envia heartbeat periódico (apenas o líder)
     */
    private void startHeartbeat() {
        if (heartbeatScheduler == null || heartbeatScheduler.isShutdown()) {
            heartbeatScheduler = Executors.newSingleThreadScheduledExecutor();
        }

        if (heartbeatTask != null) {
            heartbeatTask.cancel(false);
        }

        heartbeatTask = heartbeatScheduler.scheduleAtFixedRate(() -> {
            if (isLeader.get()) {
                try {
                    ElectionMessage heartbeat = new ElectionMessage("HEARTBEAT", processId);
                    String json = gson.toJson(heartbeat);
                    channel.basicPublish(HEARTBEAT_EXCHANGE, "", null, json.getBytes(StandardCharsets.UTF_8));
                    System.out.println("[BULLY] Coordenador " + processId + " enviou heartbeat");
                } catch (IOException e) {
                    System.err.println("[BULLY] Erro ao enviar heartbeat: " + e.getMessage());
                }
            }
        }, 0, HEARTBEAT_INTERVAL_MS, TimeUnit.MILLISECONDS);
    }

    private void stopHeartbeat() {
        if (heartbeatTask != null) {
            heartbeatTask.cancel(false);
        }
    }

    /**
     * Monitora heartbeats do líder
     */
    private void startLeaderMonitoring() {
        leaderMonitorScheduler = Executors.newSingleThreadScheduledExecutor();

        monitorTask = leaderMonitorScheduler.scheduleAtFixedRate(() -> {
            if (!isLeader.get() && currentLeaderId.get() != -1) {
                long timeSinceLastHeartbeat = System.currentTimeMillis() - lastHeartbeatReceived;

                if (timeSinceLastHeartbeat > HEARTBEAT_TIMEOUT_MS) {
                    System.out.println("[BULLY] LÍDER " + currentLeaderId.get() + " FALHOU! Iniciando eleição...");
                    currentLeaderId.set(-1);
                    new Thread(() -> startElection()).start();
                }
            }
        }, HEARTBEAT_TIMEOUT_MS, 1000, TimeUnit.MILLISECONDS);
    }

    public boolean isLeader() {
        return isLeader.get();
    }

    public int getProcessId() {
        return processId;
    }

    public int getCurrentLeaderId() {
        return currentLeaderId.get();
    }

    public void shutdown() {
        try {
            if (heartbeatTask != null) heartbeatTask.cancel(false);
            if (monitorTask != null) monitorTask.cancel(false);
            if (heartbeatScheduler != null) heartbeatScheduler.shutdown();
            if (leaderMonitorScheduler != null) leaderMonitorScheduler.shutdown();
            if (channel != null && channel.isOpen()) channel.close();
            if (connection != null && connection.isOpen()) connection.close();
        } catch (Exception e) {
            System.err.println("[BULLY] Erro ao desligar: " + e.getMessage());
        }
    }
}
