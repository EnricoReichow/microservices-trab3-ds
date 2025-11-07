package com.example.cardiologista.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CardiologistaRabbitMQConfig {

    // Constantes independentes deste microserviço
    public static final String EXCHANGE_NAME = "exchangePA";
    public static final String DLX_EXCHANGE = "dlxExchangePA";
    public static final String CARDIO_QUEUE = "cardiologia";
    public static final String FALLBACK_CARDIO_QUEUE = "fallback.cardio";
    public static final String CARDIO_ROUTING_KEY = "paciente.cardiologia";
    public static final String FALLBACK_CARDIO_KEY = "fallback.cardio";

    @Bean
    public DirectExchange exchange() {
        return new DirectExchange(EXCHANGE_NAME, true, false);
    }

    @Bean
    public DirectExchange dlxExchange() {
        return new DirectExchange(DLX_EXCHANGE, true, false);
    }

    @Bean
    public Queue cardioQueue() {
        return new Queue(CARDIO_QUEUE, true);
    }

    @Bean
    public Queue fallbackCardioQueue() {
        return new Queue(FALLBACK_CARDIO_QUEUE, true);
    }

    @Bean
    public Binding cardioBinding(Queue cardioQueue, DirectExchange exchange) {
        return BindingBuilder.bind(cardioQueue)
                .to(exchange)
                .with(CARDIO_ROUTING_KEY);
    }

    @Bean
    public Binding fallbackCardioBinding(Queue fallbackCardioQueue, DirectExchange dlxExchange) {
        return BindingBuilder.bind(fallbackCardioQueue)
                .to(dlxExchange)
                .with(FALLBACK_CARDIO_KEY);
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
