package com.example.recepcionista.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RecepcionistaRabbitMQConfig {

    // Constantes independentes deste microserviço
    private static final String EXCHANGE_NAME = "exchangePA";
    private static final String TRIAGEM_QUEUE = "triagem";
    private static final String TRIAGEM_ROUTING_KEY = "paciente.triagem";

    @Bean
    public DirectExchange exchange() {
        return new DirectExchange(EXCHANGE_NAME, true, false);
    }

    @Bean
    public Queue triagemQueue() {
        return new Queue(TRIAGEM_QUEUE, true);
    }

    @Bean
    public Binding triagemBinding(Queue triagemQueue, DirectExchange exchange) {
        return BindingBuilder.bind(triagemQueue)
                .to(exchange)
                .with(TRIAGEM_ROUTING_KEY);
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
