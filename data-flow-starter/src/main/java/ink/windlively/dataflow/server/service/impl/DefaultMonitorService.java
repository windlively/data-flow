package ink.windlively.dataflow.server.service.impl;

import ink.windlively.dataflow.monitor.AppStatusData;
import ink.windlively.dataflow.monitor.AppStatusProvider;
import ink.windlively.dataflow.server.service.FlowMonitorService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class DefaultMonitorService implements FlowMonitorService {

    private final AppStatusProvider statusProvider;

    private final Map<String, Supplier<Object>> clusterStatisticCountSuppliers = new HashMap<>();

    public DefaultMonitorService(AppStatusProvider statusProvider){
        this.statusProvider = statusProvider;

        clusterStatisticCountSuppliers.put("flow_successful", () -> statusProvider.getMsgReceivedCount().values().stream().reduce(Long::sum).orElse(0L));
        clusterStatisticCountSuppliers.put("processed_msg", () -> statusProvider.getMsgReceivedCount().values().stream().reduce(Long::sum).orElse(0L));
    }

    @Override
    public Map<String, Object> getClusterStatisticCount(List<String> statisticItem) {
        return statisticItem.stream()
                .filter(clusterStatisticCountSuppliers::containsKey)
                .collect(Collectors.toMap(s -> s, s -> clusterStatisticCountSuppliers.get(s).get()));
    }

    @Override
    public AppStatusData getClusterFullStatusData() {
        return statusProvider.getAppStatusData();
    }
}
