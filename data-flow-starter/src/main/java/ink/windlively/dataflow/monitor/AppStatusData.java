package ink.windlively.dataflow.monitor;

import lombok.Data;

import java.util.Map;

import static ink.windlively.dataflow.monitor.StatisticFields.*;

@Data
public class AppStatusData {

    /**
     * 各个namespace收到的消息(SourceEntity维度)总数 {namespace: count}
     */
    private Map<String, Long> msgReceivedCount;

    /**
     * 各个namespace已处理的消息
     */
    private Map<String, Long> msgProcessedCount;

    /**
     * 各个流收到的消息数量 {flowName: count}
     */
    private Map<String, Long> inflowCount;

    /**
     * 各个流处理成功的数量 {flowName: count}
     */
    private Map<String, Long> successfulCount;

    /**
     * 各个流处理失败的数量 {flowName: count}
     */
    private Map<String, Long> failureCount;

    /**
     * 历史收到的数据量 {date(yyyy-MM-dd): {namespace: count}}
     */
    private Map<String, Map<String, Long>> historyReceivedCount;

    private long timestamp;

    public static AppStatusData fromMap(Map<String, Map<String, Long>> statisticData){
        AppStatusData statusData = new AppStatusData();
        statusData.setMsgProcessedCount(statisticData.get(STC_FIELD_MSG_PROCESSED));
        statusData.setMsgReceivedCount(statisticData.get(STC_FIELD_MSG_RECEIVED));
        statusData.setInflowCount(statisticData.get(STC_FIELD_INFLOW));
        statusData.setSuccessfulCount(statisticData.get(STC_FIELD_SUCCESSFUL));
        statusData.setFailureCount(statisticData.get(STC_FIELD_FAILURE));
        statusData.setTimestamp(System.currentTimeMillis());
        return statusData;
    }
}
