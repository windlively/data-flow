package ink.andromeda.dataflow.core.mq;

import org.apache.rocketmq.client.consumer.MQConsumer;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.MQProducer;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.remoting.exception.RemotingException;

public class RocketInstance implements MessageQueueInstance<MQProducer, MQConsumer>{


    private final String name;

    private final MQProducer producer;

    public RocketInstance(String name, String groupId, String nameServ) throws MQClientException {
        this.name = name;
        DefaultMQProducer producer = new DefaultMQProducer(groupId);
        producer.setInstanceName(name);
        producer.setNamesrvAddr(nameServ);
        producer.start();
        this.producer = producer;
    }

    public RocketInstance(String name, MQProducer producer){
        this.name = name;
        this.producer = producer;
    }

    @Override
    public String getName() {
        return "rocket";
    }

    @Override
    public String getType() {
        return "rocket";
    }

    public SendResult send(Message message) throws InterruptedException, RemotingException, MQClientException, MQBrokerException {
        return producer.send(message);
    }

    public void send(Message message, SendCallback callback) throws RemotingException, MQClientException, InterruptedException {
        producer.send(message, callback);
    }

    public MQProducer getProducer(){
        return producer;
    }

    @Override
    public void close() {
        producer.shutdown();
    }
}
