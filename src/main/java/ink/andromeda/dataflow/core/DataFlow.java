package ink.andromeda.dataflow.core;

import ink.andromeda.dataflow.core.converter.FlowNode;
import org.springframework.lang.NonNull;

import java.util.List;

public interface DataFlow extends Registry<FlowNode>{

    String getName();

    @NonNull List<FlowNode> getConverters();

    default TransferEntity inflow(SourceEntity sourceEntity) throws Exception {
        TransferEntity transferEntity = TransferEntity.builder()
                .source(sourceEntity.getSource())
                .schema(sourceEntity.getSchema())
                .opType(sourceEntity.getOpType())
                .data(sourceEntity.getData())
                .build();
        for (FlowNode flowNode : getConverters()) {
            transferEntity = flowNode.convert(sourceEntity, transferEntity);
            flowNode.export(sourceEntity, transferEntity);
        }
        return transferEntity;
    };

}
