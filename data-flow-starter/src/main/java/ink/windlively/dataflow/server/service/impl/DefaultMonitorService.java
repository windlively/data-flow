package ink.windlively.dataflow.server.service.impl;

import ink.windlively.dataflow.monitor.AppStatusData;
import ink.windlively.dataflow.monitor.AppStatusProvider;
import ink.windlively.dataflow.server.entity.HttpResult;
import ink.windlively.dataflow.server.service.FlowMonitorService;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class DefaultMonitorService implements FlowMonitorService {

    private final AppStatusProvider statusProvider;

    public DefaultMonitorService(AppStatusProvider statusProvider){
        this.statusProvider = statusProvider;
    }

    @Override
    public AppStatusData getClusterFullStatusData() {
        return statusProvider.getAppStatusData();
    }

    @Override
    public List<String> getActiveInstance() {
        return statusProvider.getActiveInstances();
    }

    @Override
    public List<String> getAllInstance() {
        return statusProvider.getAllInstances();
    }

    @Override
    public Map<String, AppStatusData> getStatusData(List<String> instanceName) {
        return statusProvider.getAppStatusData(instanceName);
    }

}
