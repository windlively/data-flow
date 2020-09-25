package ink.andromeda.dataflow.util.kafka;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.listener.KafkaListenerErrorHandler;
import org.springframework.kafka.listener.ListenerExecutionFailedException;
import org.springframework.messaging.Message;

@Slf4j
public class DataFlowKafkaListenerErrorHandler implements KafkaListenerErrorHandler {

    @Override
    public Object handleError(Message<?> message, ListenerExecutionFailedException exception) {
        log.error("kafka listener error: {}, consume message: {}", exception.getMessage(), message.getPayload());
        return null;
    }
}
