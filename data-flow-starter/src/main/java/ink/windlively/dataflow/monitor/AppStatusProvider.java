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

    AppStatusData getAppStatusData();

    Map<String, AppStatusData> getAppStatusData(List<String> instances);

}
