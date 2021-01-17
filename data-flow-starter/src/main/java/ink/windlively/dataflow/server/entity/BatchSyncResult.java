package ink.windlively.dataflow.server.entity;

import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Builder
@Data
public class BatchSyncResult {

    private long total;

    private long current;

    private String batchNo;

    private Date startTime;

    private Date endTime;

    private String orderNo;

    private String source;
}
