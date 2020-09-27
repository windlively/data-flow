package ink.andromeda.dataflow.core;

import org.springframework.lang.Nullable;

import java.util.HashMap;

import static ink.andromeda.dataflow.util.GeneralTools.conversionService;

/**
 * 环境上下文
 */
public class EnvironmentContext extends HashMap<String, Object> {

    public EnvironmentContext() {
        super(4);
    }

    public EnvironmentContext(int initialCapacity) {
        super(initialCapacity);
    }

    @SuppressWarnings("unchecked")
    @Nullable
    public <T> T getVariable(String name) {
        return (T) get(name);
    }

    @Nullable
    public <T> T getVariable(String name, Class<T> clazz) {
        Object obj = get(name);
        if(obj == null) return null;
        if(clazz.isInstance(obj)) //noinspection unchecked
            return (T) obj;
        return conversionService().convert(get(name), clazz);
    }

    public EnvironmentContext setVariable(String name, Object val) {
        put(name, val);
        return this;
    }

}
