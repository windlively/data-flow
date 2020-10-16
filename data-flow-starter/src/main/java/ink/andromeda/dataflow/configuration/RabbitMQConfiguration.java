//package ink.andromeda.dataflow.configuration;
//
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.amqp.core.Binding;
//import org.springframework.amqp.core.BindingBuilder;
//import org.springframework.amqp.core.Queue;
//import org.springframework.amqp.core.TopicExchange;
//import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
//import org.springframework.amqp.rabbit.core.RabbitTemplate;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//import java.util.HashMap;
//import java.util.Map;
//
////@Configuration
//@Slf4j
//public class RabbitMQConfiguration {
//
//    private final CachingConnectionFactory connectionFactory;
//
//    public RabbitMQConfiguration(CachingConnectionFactory connectionFactory) {
//        this.connectionFactory = connectionFactory;
//    }
//
//    @Bean
//    TopicExchange topicExchange(){
//        return new TopicExchange("core-system-datachannel");
//    }
//
//    @Bean
//    Queue eventQueue(){
//        return new Queue("event-message");
//    }
//
//    @Bean
//    Queue canalQueue(){
//        return new Queue("scb-canal-queue");
//    }
//
//    @Bean
//    Binding bindingExchangeMessage(){
//        return BindingBuilder.bind(eventQueue())
//                .to(topicExchange())
//                .with("cs-event");
//    }
//
//    @Bean
//    Queue deadQueue(){
//        Map<String,Object> args = new HashMap<>();
//        args.put("x-message-ttl", 60000);
//        args.put("x-dead-letter-exchange", "core-system-datachannel");
//        args.put("x-dead-letter-routing-key", "cs-event");
//        return new Queue("event-message-dead-delay", true, false, false, args);
//    }
//
//    @Bean
//    Binding deadQueueBinding(){
//        return BindingBuilder.bind(deadQueue())
//                .to(topicExchange())
//                .with("cs-event-delay");
//    }
//
//    @Bean
//    RabbitTemplate rabbitTemplate(){
//        connectionFactory.setPublisherConfirms(true);
//        connectionFactory.setPublisherReturns(true);
//        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
//        rabbitTemplate.setMandatory(true);
//        rabbitTemplate.setExchange("core-system-datachannel");
//        rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
//            if(ack)
//                log.debug("message send success, correlationData: {}, cause: {}", correlationData, cause);
//            else
//                log.error("message send failed, correlationData: {}, cause: {}", correlationData, cause);
//        });
//
//        rabbitTemplate.setReturnCallback((message, replyCode, replyText, exchange, routingKey) -> {
//            log.error("message lose, message: {}, replyCode: {}, replyText: {}, exchange: {}, routingKey: {}", message, replyCode, replyText, exchange, routingKey);
//        });
//
//        return rabbitTemplate;
//    }
//}
