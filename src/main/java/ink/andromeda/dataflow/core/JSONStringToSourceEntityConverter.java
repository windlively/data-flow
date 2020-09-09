package ink.andromeda.dataflow.core;

import ink.andromeda.dataflow.util.GeneralTools;

import javax.annotation.Nonnull;

public class JSONStringToSourceEntityConverter implements SourceEntityConverter<String> {
    
    @Override
    public SourceEntity convert(@Nonnull String source) {
        return GeneralTools.GSON().fromJson(source, SourceEntity.class);
    }

}
