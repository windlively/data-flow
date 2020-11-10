package ink.andromeda.dataflow.server.entity;

import lombok.Data;

@Data
public class RefreshCacheMessage {

    private String cacheType;

    private String subExpression;

}
