package com.example.recepcionista_backend.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // Injeta os valores do application.properties.BAK
    @Value("${app.rabbitmq.exchange}")
    private String exchangeName;

    @Value("${app.rabbitmq.queue}")
    private String queueName;

    @Value("${app.rabbitmq.routingkey}")
    private String routingKey;

    /**
     * Cria a fila no RabbitMQ.
     * durable = true (a fila sobrevive a reinícios do broker)
     */
    @Bean
    public Queue queue() {
        return new Queue(queueName, true);
    }

    /**
     * Cria a exchange do tipo 'direct'.
     * durable = true (a exchange sobrevive a reinícios do broker)
     */
    @Bean
    public DirectExchange exchange() {
        return new DirectExchange(exchangeName, true, false);
    }

    /**
     * Cria o 'binding' (ligação) entre a fila e a exchange usando a routing key.
     */
    @Bean
    public Binding binding(Queue queue, DirectExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(routingKey);
    }

    /**
     * Define o conversor de mensagens padrão para JSON (usando Jackson).
     * Isso faz com que objetos Paciente sejam enviados como JSON automaticamente.
     */
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * Configura o RabbitTemplate (principal classe de envio) para usar
     * o conversor JSON que acabamos de definir.
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        final RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }
}