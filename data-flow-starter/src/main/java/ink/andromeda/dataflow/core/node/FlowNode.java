package ink.andromeda.dataflow.core.node;

import ink.andromeda.dataflow.core.SourceEntity;
import ink.andromeda.dataflow.core.TransferEntity;

/**
 * flow节点
 *
 */
public interface FlowNode {

    default String getName() {
        return "default";
    }

    TransferEntity apply(SourceEntity source, TransferEntity input) throws Exception;

    default TransferEntity apply(SourceEntity sourceEntity) throws Exception {
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
