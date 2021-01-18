package ink.windlively.dataflow.monitor;

import java.util.List;
import java.util.Map;

/**
 * 监控数据提供者
 */
public interface AppStatusProvider {

    default void setInstanceName(String instanceName) {};

    List<String> getActiveInstances();

    List<String> getAllInstances();

    // 获取各个namespace下收到的消息数量
    Map<String, Long> getReceiveMsgCount();

    // 获取某个namespace下收到的消息数量
    default long getReceiveMsgCount(String namespace) {
        return getReceiveMsgCount().values().stream().reduce(Long::sum).orElse(0L);
    }

}
