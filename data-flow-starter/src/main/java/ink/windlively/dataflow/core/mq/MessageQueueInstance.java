package ink.windlively.dataflow.core.mq;

public interface MessageQueueInstance<PRODUCER, CONSUMER> extends AutoCloseable{

    String getName();

    String getType();

    PRODUCER getProducer();

    default CONSUMER getConsumer(){
        throw new UnsupportedOperationException();
    }
}
