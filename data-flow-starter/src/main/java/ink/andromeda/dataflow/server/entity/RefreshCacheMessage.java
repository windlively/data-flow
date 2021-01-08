package ink.andromeda.dataflow.server.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import static ink.andromeda.dataflow.util.GeneralTools.toJSONString;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RefreshCacheMessage {

    private String cacheType;

    private String subExpression;

    @Override
    public String toString(){
        return toJSONString(this);
    }

}
