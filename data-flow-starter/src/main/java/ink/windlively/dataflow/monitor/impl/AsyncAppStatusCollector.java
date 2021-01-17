package ink.windlively.dataflow.monitor.impl;

import ink.windlively.dataflow.core.FlowFailInfo;
import ink.windlively.dataflow.core.SourceEntity;
import ink.windlively.dataflow.monitor.AppStatusCollector;
import org.springframework.lang.Nullable;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class AsyncAppStatusCollector implements AppStatusCollector {

    private final AppStatusCollector realCollector;

    private final ThreadPoolExecutor executor;

    public AsyncAppStatusCollector(AppStatusCollector realCollector) {
        this.realCollector = realCollector;
        executor = (ThreadPoolExecutor) Executors.newCachedThreadPool();
    }

    public AsyncAppStatusCollector(AppStatusCollector realCollector, ThreadPoolExecutor executor) {
        this.realCollector = realCollector;
        this.executor = executor;
    }

    @Override
    public void receiveOne(SourceEntity sourceEntity) {
        executor.execute(() -> realCollector.receiveOne(sourceEntity));
    }

    @Override
    public void inflowOne(String flowName, SourceEntity sourceEntity) {
        executor.execute(() -> realCollector.inflowOne(flowName, sourceEntity));
    }

    @Override
    public void successOne(String flowName, SourceEntity sourceEntity) {
        executor.execute(() -> realCollector.successOne(flowName, sourceEntity));
    }

    @Override
    public void failedOne(String flowName, SourceEntity sourceEntity) {
        executor.execute(() -> realCollector.failedOne(flowName, sourceEntity));
    }

    @Override
    public void failedOne(String flowName, SourceEntity sourceEntity, @Nullable FlowFailInfo failInfo) {
        executor.execute(() -> realCollector.failedOne(flowName, sourceEntity, failInfo));
    }
}
