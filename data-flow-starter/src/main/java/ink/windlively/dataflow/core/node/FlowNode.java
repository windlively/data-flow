package ink.windlively.dataflow.core.node;

import ink.windlively.dataflow.core.FlowNodeProcessException;
import ink.windlively.dataflow.core.SourceEntity;
import ink.windlively.dataflow.core.TransferEntity;

/**
 * flow节点
 *
 */
public interface FlowNode {

    default String getName() {
        return SourceEntity.DEFAULT_FIELD_STR_VALUE;
    }

    TransferEntity apply(SourceEntity source, TransferEntity input) throws FlowNodeProcessException;

    default TransferEntity apply(SourceEntity sourceEntity) throws FlowNodeProcessException {
        return apply(sourceEntity, TransferEntity.builder()
                .data(sourceEntity.getData())
                .name(sourceEntity.getName())
                .opType(sourceEntity.getOpType())
                .schema(sourceEntity.getSchema())
                .source(sourceEntity.getSource())
                .build());
    }

    default FlowNode then(FlowNode node){
        return (source, input) -> node.apply(source, FlowNode.this.apply(source, input));
    }

}
