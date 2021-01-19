package ink.windlively.dataflow.server.service;

import ink.windlively.dataflow.monitor.AppStatusData;

import java.util.List;
import java.util.Map;

public interface FlowMonitorService {

    Map<String, Object> getClusterStatisticCount(List<String> statisticItem);

    AppStatusData getClusterFullStatusData();

}
