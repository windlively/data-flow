package ink.windlively.dataflow.server.service;

import java.util.List;
import java.util.Map;

public interface FlowMonitorService {

    Map<String, Object> getClusterStatisticCount(List<String> statisticItem);

}
