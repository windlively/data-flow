package ink.andromeda.dataflow.core;

import ink.andromeda.dataflow.entity.SourceEntity;
import ink.andromeda.dataflow.util.CommonUtils;

public class JSONStringToSourceEntityConverter implements ToSourceEntityConverter<String>{
    
    @Override
    public SourceEntity convert(String source) {
        return CommonUtils.Gson().fromJson(source, SourceEntity.class);
    }

}
