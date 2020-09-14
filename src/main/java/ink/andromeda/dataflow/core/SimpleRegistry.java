package ink.andromeda.dataflow.core;

import org.springframework.lang.NonNull;
import org.springframework.util.Assert;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class SimpleRegistry<T> implements Registry<T> {

    private final LinkedList<T> internalRegisteredList = new LinkedList<>();

    private List<T> registeredList = Collections.emptyList();

    @Override
    public Registry<T> addLast(@NonNull T bean) {
        check(bean);
        internalRegisteredList.add(bean);
        return this;
    }

    @Override
    public Registry<T> addFirst(@NonNull T bean) {
        check(bean);
        internalRegisteredList.addFirst(bean);
        return this;
    }

    @Override
    public Registry<T> addTo(int index, @NonNull T bean) {
        check(bean);
        internalRegisteredList.add(index, bean);
        return this;
    }

    @Override
    @NonNull
    public List<T> get() {
        return registeredList;
    }

    @Override
    public int remove(@NonNull Predicate<T> predicate) {
        return internalRegisteredList.removeIf(predicate) ? 1 : 0;
    }

    @Override
    public synchronized void effect() {
        registeredList = Collections.unmodifiableList(new ArrayList<>(internalRegisteredList));
    }

    private void check(T item){
        Assert.notNull(item, "input object must be non null");
        Assert.isTrue(!internalRegisteredList.contains(item), "input object '" + item +"' has exist");
    }
}
