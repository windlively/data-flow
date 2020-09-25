package ink.andromeda.dataflow.core.flow;

import ink.andromeda.dataflow.core.Registry;
import ink.andromeda.dataflow.core.SourceEntity;
import ink.andromeda.dataflow.core.TransferEntity;
import ink.andromeda.dataflow.core.node.FlowNode;
import org.springframework.lang.NonNull;

import java.util.List;

public interface DataFlow extends Registry<FlowNode> {

    default String getApplySource(){return "";}

    default String getApplySchema(){return "";}

    default String getApplyName(){return "";}

    String getName();

    @NonNull List<FlowNode> getNodes();

    default TransferEntity inflow(SourceEntity sourceEntity) throws Exception {
        TransferEntity transferEntity = TransferEntity.builder()
                .source(sourceEntity.getSource())
                .schema(sourceEntity.getSchema())
                .opType(sourceEntity.getOpType())
                .data(sourceEntity.getData())
                .build();
        for (FlowNode flowNode : getNodes()) {
            transferEntity = flowNode.convert(sourceEntity, transferEntity);
            flowNode.export(sourceEntity, transferEntity);
        }
        return transferEntity;
    };

}
