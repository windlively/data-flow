package ink.andromeda.dataflow.server.canal.table;

import lombok.Data;

@Data
public class CanalMetaInfo {

    private String logfileName;

    private long logfileOffset;

    private long serverId;

    private String serverenCode;

    private long executeTime;

    private String schemaName;

    private String tableName;

    private long eventLength;

    private String eventType;

    private String gtid;

}
