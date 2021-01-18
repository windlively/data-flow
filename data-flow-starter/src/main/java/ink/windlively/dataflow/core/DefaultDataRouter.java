package ink.windlively.dataflow.core;

import ink.windlively.dataflow.core.flow.DataFlow;
import ink.windlively.dataflow.core.flow.DataFlowManager;
import ink.windlively.dataflow.monitor.AppStatusCollector;
import ink.windlively.dataflow.util.TraceIdThreadPool;
import ink.windlively.dataflow.util.GeneralTools;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * 默认的路由策略，根据source, schema, name匹配flow
 */
@Slf4j
public class DefaultDataRouter implements DataRouter {

    @Setter
    @Getter
    private DataFlowManager dataFlowManager;

    private final AppStatusCollector statusCollector;

    private final ExecutorService executorService = new TraceIdThreadPool(
            Executors.newCachedThreadPool()
    );

    public DefaultDataRouter(DataFlowManager dataFlowManager,
                             AppStatusCollector statusCollector) {
        this.dataFlowManager = dataFlowManager;
        this.statusCollector = statusCollector;
    }

    @Override
    public AppStatusCollector getStatusCollector() {
        return statusCollector;
    }

    @Override
    public List<DataFlow> route(SourceEntity sourceEntity) {

        getStatusCollector().receiveOneMsg(sourceEntity);

        List<DataFlow> flowList = dataFlowManager.getFlow(
                sourceEntity.getSource(),
                sourceEntity.getSchema(),
                sourceEntity.getName());
        if (flowList.isEmpty()) {
            log.info("source: {}, schema: {}, name: {} cannot be routed to at least one flow",
                    sourceEntity.getSource(), sourceEntity.getSchema(), sourceEntity.getName());
        } else {
            log.info("be routed to flow: {}", flowList.stream().map(DataFlow::getName).collect(Collectors.joining(",")));
        }
        return flowList;
    }

    @Override
    public List<TransferEntity> routeAndProcess(SourceEntity sourceEntity) throws Exception {
        List<TransferEntity> transferEntities = new ArrayList<>(8);

        String parentTraceId = Optional.ofNullable(MDC.get("traceId")).orElse(GeneralTools.shortTraceId());
        MDC.put("traceId", parentTraceId);
        log.info("starting process source entity: {}", sourceEntity);

        List<DataFlow> flowList = route(sourceEntity);
        CountDownLatch countDownLatch = new CountDownLatch(flowList.size());
        for (DataFlow flow : flowList) {
            MDC.put("traceId", parentTraceId + "-" + GeneralTools.shortTraceId());
            executorService.submit(() -> {
                try {

                    getStatusCollector().inflowOne(flow.getName(), sourceEntity);

                    transferEntities.add(flow.inflow(sourceEntity.clone()));

                    getStatusCollector().successOne(flow.getName(), sourceEntity);

                } catch (FlowNodeProcessException e) {
                    // 被过滤的数据认为处理成功
                    if (e.getOriginalException() instanceof FilteredException) {
                        log.info(e.getOriginalException().getMessage());
                        getStatusCollector().successOne(flow.getName(), sourceEntity);
                        return;
                    }

                    log.error(e.getMessage(), e);
                    getStatusCollector().failedOne(flow.getName(), sourceEntity,
                            FlowFailInfo.builder()
                                    .position(e.getNodeName())
                                    .throwable(e.getOriginalException())
                                    .build());

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    getStatusCollector().failedOne(flow.getName(), sourceEntity,
                            FlowFailInfo.builder()
                                    .throwable(e)
                                    .build());
                } finally {
                    countDownLatch.countDown();
                }
            });
            MDC.remove("traceId");
        }
        countDownLatch.await();
        getStatusCollector().processOneMsg(sourceEntity);
        return transferEntities;
    }
}
