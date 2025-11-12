package com.example.clinico_geral.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ClinicoGeralRabbitMQConfig {

    // Constantes independentes deste microserviço
    public static final String DLX_EXCHANGE = "dlxExchangePA";
    public static final String GERAL_QUEUE = "geral";
    public static final String EXCHANGE_NAME = "exchangePA";
    public static final String FALLBACK_GERAL_QUEUE = "fallback.geral";
    public static final String GERAL_ROUTING_KEY = "paciente.geral";
    public static final String FALLBACK_GERAL_KEY = "fallback.geral";

    @Bean
    public DirectExchange exchange() {
        return new DirectExchange(EXCHANGE_NAME, true, false);
    }

    @Bean
    public DirectExchange dlxExchange() {
        return new DirectExchange(DLX_EXCHANGE, true, false);
    }

    @Bean
    public Queue geralQueue() {
        return new Queue(GERAL_QUEUE, true);
    }

    @Bean
    public Queue fallbackGeralQueue() {
        return new Queue(FALLBACK_GERAL_QUEUE, true);
    }

    @Bean
    public Binding geralBinding(Queue geralQueue, DirectExchange exchange) {
        return BindingBuilder.bind(geralQueue)
                .to(exchange)
                .with(GERAL_ROUTING_KEY);
    }

    @Bean
    public Binding fallbackGeralBinding(Queue fallbackGeralQueue, DirectExchange dlxExchange) {
        return BindingBuilder.bind(fallbackGeralQueue)
                .to(dlxExchange)
                .with(FALLBACK_GERAL_KEY);
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
