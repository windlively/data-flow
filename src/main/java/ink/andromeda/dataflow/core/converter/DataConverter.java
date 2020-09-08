package ink.andromeda.dataflow.core.converter;

import ink.andromeda.dataflow.core.SourceEntity;
import ink.andromeda.dataflow.core.TransferEntity;

public interface DataConverter {

    TransferEntity convert(SourceEntity sourceEntity, TransferEntity transferEntity);

    default TransferEntity convert(SourceEntity sourceEntity){
        return convert(sourceEntity, TransferEntity.builder()
                .data(sourceEntity.getData())
                .name(sourceEntity.getName())
                .opType(sourceEntity.getOpType())
                .schema(sourceEntity.getSchema())
                .source(sourceEntity.getSource())
                .build());
    };

    default int export(SourceEntity sourceEntity, TransferEntity transferEntity){
        return 0;
    }

}
