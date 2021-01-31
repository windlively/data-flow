package ink.windlively.dataflow.server.service;

import ink.windlively.dataflow.monitor.AppStatusData;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public interface FlowMonitorService {

    AppStatusData getClusterFullStatusData();

    List<String> getActiveInstance();

    List<String> getAllInstance();

    default AppStatusData getStatusData() {
        return getClusterFullStatusData();
    };

    default AppStatusData getStatusData(String instanceName) {
        return instanceName == null ? getStatusData() : getStatusData(Collections.singletonList(instanceName)).get(instanceName);
    };

    Map<String, AppStatusData> getStatusData(List<String> instanceName);
}
