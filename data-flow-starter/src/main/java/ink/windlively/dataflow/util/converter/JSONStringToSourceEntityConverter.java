package ink.windlively.dataflow.util.converter;

import ink.windlively.dataflow.core.SourceEntity;
import ink.windlively.dataflow.core.SourceEntityConverter;
import ink.windlively.dataflow.util.GeneralTools;

import javax.annotation.Nonnull;

public class JSONStringToSourceEntityConverter implements SourceEntityConverter<String> {
    
    @Override
    public SourceEntity convert(@Nonnull String source) {
        return GeneralTools.GSON().fromJson(source, SourceEntity.class);
    }

}
