package ink.andromeda.dataflow.util.converter;

import ink.andromeda.dataflow.core.SourceEntity;
import ink.andromeda.dataflow.core.SourceEntityConverter;
import ink.andromeda.dataflow.util.GeneralTools;

import javax.annotation.Nonnull;

public class JSONStringToSourceEntityConverter implements SourceEntityConverter<String> {
    
    @Override
    public SourceEntity convert(@Nonnull String source) {
        return GeneralTools.GSON().fromJson(source, SourceEntity.class);
    }

}
