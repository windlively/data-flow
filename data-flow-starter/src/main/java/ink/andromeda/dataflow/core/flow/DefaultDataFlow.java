package ink.andromeda.dataflow.core.flow;

import ink.andromeda.dataflow.core.Registry;
import ink.andromeda.dataflow.core.SourceEntity;
import ink.andromeda.dataflow.core.TransferEntity;
import ink.andromeda.dataflow.core.node.FlowNode;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;

import java.util.LinkedList;
import java.util.List;

@Slf4j
public class DefaultDataFlow implements DataFlow {

    private final LinkedList<FlowNode> nodes = new LinkedList<>();

    @Getter
    @Setter
    private String applySource;

    @Getter
    @Setter
    private String applySchema;

    @Getter
    @Setter
    private String applyName;


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
    public List<FlowNode> getNodes() {
        return get();
    }

    @Override
    public @NonNull Registry<FlowNode> addLast(@NonNull FlowNode item) {
        nodes.add(item);
        return this;
    }

    @Override
    public @NonNull Registry<FlowNode> addFirst(@NonNull FlowNode item) {
        nodes.addFirst(item);
        return this;
    }

    @Override
    public @NonNull Registry<FlowNode> addTo(int index, @NonNull FlowNode item) {
        nodes.add(index, item);
        return this;
    }

    @Override
    @NonNull
    public List<FlowNode> get() {
        return nodes;
    }

    @Override
    public TransferEntity inflow(SourceEntity sourceEntity) throws Exception {
        TransferEntity inflow = DataFlow.super.inflow(sourceEntity);
        log.info("processed by flow {}.{}.{} success.", getApplySource(), getApplySchema(), getApplyName());
        return inflow;
    }
}
