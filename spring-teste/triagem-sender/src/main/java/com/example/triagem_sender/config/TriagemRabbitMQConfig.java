package com.example.triagem_sender.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TriagemRabbitMQConfig {

    // Constantes independentes deste microserviço
    private static final String EXCHANGE_NAME = "exchangePA";
    private static final String TRIAGEM_QUEUE = "triagem";
    private static final String CARDIO_QUEUE = "cardiologia";
    private static final String NEURO_QUEUE = "neurologia";
    private static final String GERAL_QUEUE = "geral";
    
    private static final String CARDIO_ROUTING_KEY = "paciente.cardiologia";
    private static final String NEURO_ROUTING_KEY = "paciente.neurologia";
    private static final String GERAL_ROUTING_KEY = "paciente.geral";

    @Bean
    public DirectExchange exchange() {
        return new DirectExchange(EXCHANGE_NAME, true, false);
    }

    @Bean
    public Queue triagemQueue() {
        return new Queue(TRIAGEM_QUEUE, true);
    }

    @Bean
    public Queue cardioQueue() {
        return new Queue(CARDIO_QUEUE, true);
    }

    @Bean
    public Queue neuroQueue() {
        return new Queue(NEURO_QUEUE, true);
    }

    @Bean
    public Queue geralQueue() {
        return new Queue(GERAL_QUEUE, true);
    }

    @Bean
    public Binding cardioBinding(Queue cardioQueue, DirectExchange exchange) {
        return BindingBuilder.bind(cardioQueue)
                .to(exchange)
                .with(CARDIO_ROUTING_KEY);
    }

    @Bean
    public Binding neuroBinding(Queue neuroQueue, DirectExchange exchange) {
        return BindingBuilder.bind(neuroQueue)
                .to(exchange)
                .with(NEURO_ROUTING_KEY);
    }

    @Bean
    public Binding geralBinding(Queue geralQueue, DirectExchange exchange) {
        return BindingBuilder.bind(geralQueue)
                .to(exchange)
                .with(GERAL_ROUTING_KEY);
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
