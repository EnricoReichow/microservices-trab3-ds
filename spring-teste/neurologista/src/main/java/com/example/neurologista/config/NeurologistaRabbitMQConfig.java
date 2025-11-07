package com.example.neurologista.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NeurologistaRabbitMQConfig {

    // Constantes independentes deste microserviço
    private static final String EXCHANGE_NAME = "exchangePA";
    private static final String DLX_EXCHANGE = "dlxExchangePA";
    private static final String NEURO_QUEUE = "neurologia";
    private static final String FALLBACK_NEURO_QUEUE = "fallback.neuro";
    private static final String NEURO_ROUTING_KEY = "paciente.neurologia";
    private static final String FALLBACK_NEURO_KEY = "fallback.neuro";

    @Bean
    public DirectExchange exchange() {
        return new DirectExchange(EXCHANGE_NAME, true, false);
    }

    @Bean
    public DirectExchange dlxExchange() {
        return new DirectExchange(DLX_EXCHANGE, true, false);
    }

    @Bean
    public Queue neuroQueue() {
        return new Queue(NEURO_QUEUE, true);
    }

    @Bean
    public Queue fallbackNeuroQueue() {
        return new Queue(FALLBACK_NEURO_QUEUE, true);
    }

    @Bean
    public Binding neuroBinding(Queue neuroQueue, DirectExchange exchange) {
        return BindingBuilder.bind(neuroQueue)
                .to(exchange)
                .with(NEURO_ROUTING_KEY);
    }

    @Bean
    public Binding fallbackNeuroBinding(Queue fallbackNeuroQueue, DirectExchange dlxExchange) {
        return BindingBuilder.bind(fallbackNeuroQueue)
                .to(dlxExchange)
                .with(FALLBACK_NEURO_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }
}
