package ink.andromeda.dataflow.core;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

public interface Registry<T> {

    Registry<T> addLast(@NonNull T item);

    Registry<T> addFirst(@NonNull T item);

    Registry<T> addTo(int index, @NonNull T item);

    default Registry<T> addTo(@NonNull Function<List<T>, Integer> findIndex, @NonNull T item){
        return addTo(findIndex.apply(get()), item);
    }

    @NonNull List<T> get();

    @Nullable default T get(@NonNull Predicate<T> predicate){
        Objects.requireNonNull(predicate);
        return get().stream().filter(predicate).findFirst().orElse(null);
    }

    default int remove(@NonNull Predicate<T> predicate){
        throw new UnsupportedOperationException();
    }

    default void effect() {};
}
