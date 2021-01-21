package ink.windlively.dataflow.server.service;

import ink.windlively.dataflow.monitor.AppStatusData;

import java.util.List;

public interface FlowMonitorService {

    AppStatusData getClusterFullStatusData();

    List<String> getActiveInstance();

    List<String> getAllInstance();

}
