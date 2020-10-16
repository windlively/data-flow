package ink.andromeda.dataflow.core;

import ink.andromeda.dataflow.core.flow.DataFlow;
import ink.andromeda.dataflow.core.flow.DataFlowManager;
import ink.andromeda.dataflow.util.TraceIdThreadPool;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import static ink.andromeda.dataflow.util.GeneralTools.randomId;

/**
 * 默认的路由策略，根据source, schema, name匹配flow
 */
@Slf4j
public class DefaultDataRouter implements DataRouter {

    @Setter
    @Getter
    private DataFlowManager dataFlowManager;

    private final ExecutorService executorService = new TraceIdThreadPool(
            Executors.newCachedThreadPool()
    );

    public DefaultDataRouter(DataFlowManager dataFlowManager) {
        this.dataFlowManager = dataFlowManager;
    }


    @Override
    public List<DataFlow> route(SourceEntity sourceEntity) {
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
        List<DataFlow> flowList = route(sourceEntity);

        CountDownLatch countDownLatch = new CountDownLatch(flowList.size());

        for (DataFlow flow : flowList) {
            MDC.put("traceId", randomId());
            executorService.submit(() -> {
                try {
                    transferEntities.add(flow.inflow(sourceEntity.clone()));
                }catch (FilteredException e){
                    log.info(e.getMessage());
                }catch (Exception e) {
                    log.error(e.getMessage(), e);
                }finally {
                    countDownLatch.countDown();
                }
            });
            MDC.remove("traceId");
        }

        countDownLatch.await();

        return transferEntities;
    }
}
