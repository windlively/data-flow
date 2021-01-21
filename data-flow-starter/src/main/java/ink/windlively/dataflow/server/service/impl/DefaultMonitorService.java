package ink.windlively.dataflow.server.service.impl;

import ink.windlively.dataflow.monitor.AppStatusData;
import ink.windlively.dataflow.monitor.AppStatusProvider;
import ink.windlively.dataflow.server.service.FlowMonitorService;

import java.util.List;

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
}
