package com.example.recepcionista_backend.runner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * Este componente roda *após* a aplicação Spring iniciar
 * e loga o status da configuração do RabbitMQ.
 * Ele só executa se o modo CLI estiver DESABILITADO.
 */
@Component
public class RabbitConnectionLogger implements ApplicationRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(RabbitConnectionLogger.class);

    // Injeta a fábrica de conexões
    private final ConnectionFactory connectionFactory;

    // Injeta os valores para confirmar que foram lidos
    @Value("${app.rabbitmq.exchange}")
    private String exchangeName;

    @Value("${app.rabbitmq.queue}")
    private String queueName;

    // Injeta o status do CLI
    @Value("${app.cli.enabled:false}")
    private boolean cliEnabled;

    public RabbitConnectionLogger(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        // Só executa este log se estiver em modo WEB (CLI desabilitado)
        if (!cliEnabled) {
            try {
                // Tenta "pingar" o broker pegando o host e porta da configuração
                String host = connectionFactory.getHost();
                int port = connectionFactory.getPort();

                LOGGER.info("=======================================================================");
                LOGGER.info("Aplicação iniciada em MODO WEB.");
                LOGGER.info("Configuração do RabbitMQ pronta para conectar em: {}:{}", host, port);
                LOGGER.info("Monitorando Exchange: '{}' e Fila: '{}'", exchangeName, queueName);
                LOGGER.info("A conexão só será estabelecida e visível no RabbitMQ");
                LOGGER.info("APÓS O PRIMEIRO ENVIO de mensagem.");
                LOGGER.info("Use o endpoint 'POST /api/pacientes' para testar.");
                LOGGER.info("=======================================================================");

            } catch (Exception e) {
                LOGGER.error("=======================================================================");
                LOGGER.error("!!! FALHA AO VERIFICAR CONFIGURAÇÃO DO RABBITMQ !!!", e);
                LOGGER.error("Verifique suas configurações em application.properties.BAK (host, port, user, pass).");
                LOGGER.error("=======================================================================");
            }
        }
    }
}