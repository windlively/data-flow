package ink.windlively.dataflow.monitor;

import lombok.Data;

import java.util.Map;

@Data
public class AppStatusData {

    /**
     * 各个namespace收到的消息(SourceEntity维度)总数 {namespace: count}
     */
    private Map<String, Long> receiveMsgCount;

    /**
     * 各个namespace已处理的消息
     */
    private Map<String, Long> processMsgCount;

    /**
     * 各个流收到的消息数量 {flowName: count}
     */
    private Map<String, Long> inflowCount;

    /**
     * 各个流处理成功的数量 {flowName: count}
     */
    private Map<String, Long> successCount;

    /**
     * 各个流处理失败的数量 {flowName: count}
     */
    private Map<String, Long> failedCount;

    /**
     * 历史收到的数据量 {date(yyyy-MM-dd): {namespace: count}}
     */
    private Map<String, Map<String, Long>> historyReceiveCount;


}
