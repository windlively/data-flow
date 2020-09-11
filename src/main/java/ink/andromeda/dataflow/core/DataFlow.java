package ink.andromeda.dataflow.core;

import ink.andromeda.dataflow.core.converter.DataConverter;
import org.springframework.lang.NonNull;

import java.util.List;

public interface DataFlow {

    String getName();

    @NonNull List<DataConverter> getConverters();

    default void process(SourceEntity sourceEntity) throws Exception {
        TransferEntity transferEntity = TransferEntity.builder()
                .source(sourceEntity.getSource())
                .schema(sourceEntity.getSchema())
                .opType(sourceEntity.getOpType())
                .data(sourceEntity.getData())
                .build();
        for (DataConverter dataConverter : getConverters()) {
            transferEntity = dataConverter.convert(sourceEntity, transferEntity);
        }
    };

}
