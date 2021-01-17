package ink.windlively.dataflow.server.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import static ink.windlively.dataflow.util.GeneralTools.toJSONString;

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

    public static final String TOPIC_NAME = "refresh-cache";

}
