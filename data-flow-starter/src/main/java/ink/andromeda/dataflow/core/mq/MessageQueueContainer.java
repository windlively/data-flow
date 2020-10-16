package ink.andromeda.dataflow.core.mq;

import org.springframework.lang.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public interface MessageQueueContainer {

    List<MessageQueueInstance<?, ?>> getAll();

    default <T extends MessageQueueInstance<?, ?>> List<T> getByClass(Class<T> clazz) {
        return getAll()
                .stream()
                .filter(clazz::isInstance)
                .map(clazz::cast)
                .collect(Collectors.toList());
    }

    @Nullable
    default <T extends MessageQueueInstance<?, ?>> T getByName(String name, Class<T> clazz) {
        return clazz.cast(getAll()
                .stream()
                .filter(i -> Objects.equals(i.getName(), name))
                .findFirst()
                .orElse(null));
    }

    @Nullable
    default MessageQueueInstance<?, ?> getByName(String name) {
        return getAll()
                .stream()
                .filter(m -> Objects.equals(name, m.getName()))
                .findFirst().orElse(null);
    }

    default List<MessageQueueInstance<?, ?>> getByType(String type) {
        return getAll()
                .stream()
                .filter(m -> Objects.equals(type, m.getType()))
                .collect(Collectors.toList());
    }

    default int add(MessageQueueInstance<?, ?> instance) {
        getAll().add(instance);
        return 1;
    }

    default int add(List<MessageQueueInstance<?, ?>> instanceList) {
        getAll().addAll(instanceList);
        return instanceList.size();
    }

    default int remove(Predicate<MessageQueueInstance<?, ?>> predicate) {
        int old = getAll().size();
        List<MessageQueueInstance<?, ?>> instanceList =
                getAll().stream().filter(predicate).collect(Collectors.toList());
        getAll().clear();
        getAll().addAll(instanceList);
        return old - instanceList.size();
    }
}
