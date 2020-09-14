package ink.andromeda.dataflow.core;

import ink.andromeda.dataflow.core.converter.FlowNode;
import org.springframework.lang.NonNull;

import java.util.LinkedList;
import java.util.List;

public class DefaultDataFlow implements DataFlow {

    private final LinkedList<FlowNode> nodes = new LinkedList<>();

    private final String name;

    public DefaultDataFlow(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    @NonNull
    public List<FlowNode> getConverters() {
        return get();
    }

    @Override
    public Registry<FlowNode> addLast(@NonNull FlowNode item) {
        nodes.add(item);
        return this;
    }

    @Override
    public Registry<FlowNode> addFirst(@NonNull FlowNode item) {
        nodes.addFirst(item);
        return this;
    }

    @Override
    public Registry<FlowNode> addTo(int index, @NonNull FlowNode item) {
        nodes.add(index, item);
        return this;
    }

    @Override
    @NonNull
    public List<FlowNode> get() {
        return nodes;
    }
}
