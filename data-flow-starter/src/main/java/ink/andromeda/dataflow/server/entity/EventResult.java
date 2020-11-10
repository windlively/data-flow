package ink.andromeda.dataflow.server.entity;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EventResult {

    private String orderNo;

    private String originalId;

    private boolean success;

    private String eventName;

    private String eventKey;

    private String schemaName;

    private String tableName;

    private String coreTable;

    private String msg;

    private String batchNo;
}
