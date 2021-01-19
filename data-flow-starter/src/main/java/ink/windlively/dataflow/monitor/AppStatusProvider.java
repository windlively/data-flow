package ink.windlively.dataflow.monitor;

import java.util.List;
import java.util.Map;

/**
 * 监控数据提供者
 */
public interface AppStatusProvider {

    default void setInstanceName(String instanceName) {};

    // 获取在线的实例名称
    List<String> getActiveInstances();

    // 获取所有的实例名称
    List<String> getAllInstances();

    // 获取各个namespace下收到的消息数量
    Map<String, Long> getMsgReceivedCount();

    Map<String, Long> getFlowSuccessCount();

    // 获取某个namespace下收到的消息数量
    default long getReceiveMsgCount(String namespace) {
        return getMsgReceivedCount().values().stream().reduce(Long::sum).orElse(0L);
    }

    AppStatusData getAppStatusData();

    Map<String, AppStatusData> getAppStatusData(List<String> instances);

}
