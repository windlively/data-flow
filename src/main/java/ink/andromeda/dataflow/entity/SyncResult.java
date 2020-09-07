package ink.andromeda.dataflow.entity;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SyncResult {

    private boolean success;

    private String msg;

    private String originalId;

    private String tableName;

    private String coreTable;

    private String schemaName;

    private String batchNo;

    private String orderNo;

}
