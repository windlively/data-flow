package ink.andromeda.dataflow.core.converter;

import com.fasterxml.jackson.databind.ObjectMapper;
import ink.andromeda.dataflow.core.SourceEntity;
import ink.andromeda.dataflow.core.TransferEntity;
import org.springframework.lang.Nullable;

/**
 * 配置化的转换器
 */
public class ConfigurableDataConverter implements DataConverter{

    @Override
    @Nullable
    public TransferEntity convert(SourceEntity sourceEntity, TransferEntity transferEntity) {
        ObjectMapper objectMapper = new ObjectMapper();

        return null;
    }

    @Override
    public int export(SourceEntity sourceEntity, TransferEntity transferEntity) {
        return 0;
    }
}
