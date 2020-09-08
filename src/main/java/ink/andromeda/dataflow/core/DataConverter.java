package ink.andromeda.dataflow.core;

public interface DataConverter {

    TransferEntity convert(OriginEntity originEntity, TransferEntity transferEntity);

    default TransferEntity convert(OriginEntity originEntity){
        return convert(originEntity, TransferEntity.builder()
                .data(originEntity.getData())
                .name(originEntity.getName())
                .opType(originEntity.getOpType())
                .schema(originEntity.getSchema())
                .source(originEntity.getSource())
                .build());
    };

    default int outTo(OriginEntity originEntity, TransferEntity transferEntity){
        return 0;
    }

}
