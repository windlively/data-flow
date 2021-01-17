package ink.windlively.dataflow.core.mq;

import org.springframework.lang.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class SimpleMessageQueueContainer implements MessageQueueContainer {

    private final Map<String, MessageQueueInstance<?, ?>> instances = new ConcurrentHashMap<>();

    @Override
    public List<MessageQueueInstance<?, ?>> getAll() {
        return Collections.unmodifiableList(new ArrayList<>(instances.values()));
    }

    @Override
    public <T extends MessageQueueInstance<?, ?>> List<T> getByClass(Class<T> clazz) {
        return instances.values()
                .stream()
                .filter(clazz::isInstance)
                .map(clazz::cast)
                .collect(Collectors.toList());
    }

    @Nullable
    @Override
    public <T extends MessageQueueInstance<?, ?>> T getByName(String name, Class<T> clazz) {
        return clazz.cast(instances.get(name));
    }

    @Override
    @Nullable
    public MessageQueueInstance<?, ?> getByName(String name) {
        return instances.get(name);
    }

    @Override
    public List<MessageQueueInstance<?, ?>> getByType(String type) {
        return instances.values()
                .stream()
                .filter(m -> Objects.equals(type, m.getType()))
                .collect(Collectors.toList());
    }

    @Override
    public int add(MessageQueueInstance<?, ?> instance) {
        if(instances.containsKey(instance.getName())){
            throw new IllegalArgumentException("mq instance name '" + instance.getName() + "' has exists");
        }
        instances.put(instance.getName(), instance);
        return 1;
    }

    @Override
    public int add(List<MessageQueueInstance<?, ?>> instanceList) {
        instanceList.forEach(this::add);
        return instanceList.size();
    }

    @Override
    public int remove(Predicate<MessageQueueInstance<?, ?>> predicate) {
        AtomicInteger c = new AtomicInteger();
        instances.values().stream().filter(predicate)
                .forEach(i -> {
                    instances.remove(i.getName());
                    c.incrementAndGet();
                });
        return c.get();
    }
}
