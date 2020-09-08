package ink.andromeda.dataflow.core;

import ink.andromeda.dataflow.util.GeneralUtils;

import javax.annotation.Nonnull;

public class JSONStringToSourceEntityConverter implements SourceEntityConverter<String> {
    
    @Override
    public SourceEntity convert(@Nonnull String source) {
        return GeneralUtils.GSON().fromJson(source, SourceEntity.class);
    }

}
