package ink.andromeda.dataflow.core.node;

import ink.andromeda.dataflow.core.SourceEntity;
import ink.andromeda.dataflow.core.TransferEntity;

/**
 * flow节点
 * 每一个节点拥有转换和导出操作,
 */
public interface FlowNode {

    default String getName() {
        return "default";
    }

    TransferEntity convert(SourceEntity sourceEntity, TransferEntity transferEntity) throws Exception;

    default TransferEntity convert(SourceEntity sourceEntity) throws Exception {
        return convert(sourceEntity, TransferEntity.builder()
                .data(sourceEntity.getData())
                .name(sourceEntity.getName())
                .opType(sourceEntity.getOpType())
                .schema(sourceEntity.getSchema())
                .source(sourceEntity.getSource())
                .build());
    }

    default int export(SourceEntity sourceEntity, TransferEntity transferEntity) throws Exception {
        return 0;
    }

    default TransferEntity convertAndExport(SourceEntity sourceEntity, TransferEntity transferEntity) throws Exception {
        TransferEntity next = convert(sourceEntity, transferEntity);
        int i = export(sourceEntity, next);
        return next;
    }

}
