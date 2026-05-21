package com.ecommerce.inventory_service.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

  // nome da fila onde o Inventory publica após debitar o estoque
  public static final String QUEUE = "stock-updated";
  // exchange que roteia as mensagens para a fila correta
  public static final String EXCHANGE = "stock-exchange";
  // chave de roteamento que conecta a fila ao exchange
  public static final String ROUTING_KEY = "stock.updated";

  @Bean
  public Queue queue() {
    return new Queue(QUEUE, true); // true = durable (sobrevive restart do RabbitMQ)
  }

  @Bean
  public DirectExchange exchange() {
    return new DirectExchange(EXCHANGE);
  }

  @Bean
  public Binding binding(Queue queue, DirectExchange exchange) {
    return BindingBuilder.bind(queue).to(exchange).with(ROUTING_KEY);
  }

  @Bean
  public MessageConverter messageConverter() {
    return new JacksonJsonMessageConverter(); // serializa objetos Java para JSON
  }
}