package ink.andromeda.dataflow.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class HistoryDataConsumeInfo {

    private Long id;

    private String orderNo;

    private String batchNo;

    private int processCount;

    private Date createTime;

}
