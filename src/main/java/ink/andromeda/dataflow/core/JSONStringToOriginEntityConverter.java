package ink.andromeda.dataflow.core;

import ink.andromeda.dataflow.util.GeneralUtils;
import org.springframework.lang.NonNull;

public class JSONStringToOriginEntityConverter implements ToOriginEntityConverter<String> {
    
    @Override
    public OriginEntity convert(@NonNull String source) {
        return GeneralUtils.GSON().fromJson(source, OriginEntity.class);
    }

}
