package ink.andromeda.dataflow.service.sync;

import ink.andromeda.dataflow.entity.AppEventSubject;
import ink.andromeda.dataflow.entity.CoreResult;
import ink.andromeda.dataflow.entity.SyncResult;
import ink.andromeda.dataflow.service.ThreadPoolService;
import ink.andromeda.dataflow.service.event.EventMessage;
import ink.andromeda.dataflow.service.event.ProductizationEventService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ink.andromeda.dataflow.entity.SourceEntity;
import ink.andromeda.dataflow.entity.TransferEntity;
import ink.andromeda.dataflow.service.ApplicationEventService;
import org.slf4j.MDC;

import java.util.List;
import java.util.concurrent.Callable;

/**
 * 数据同步任务
 */
@AllArgsConstructor
@Slf4j
public class SyncTask implements Callable<SyncResult> {

    private final CoreTableConverter converter;

    private final SourceEntity sourceEntity;

    private final ProductizationEventService productizationEventService;

    private final ApplicationEventService applicationEventService;

    @Override
    public SyncResult call() {
        boolean success;
        String msg;
        TransferEntity transferEntity;
        try {
            CoreResult<TransferEntity> convertCoreResult = converter.convertAndStore(sourceEntity);
            transferEntity = convertCoreResult.getData();
            if (transferEntity == null) {
                return SyncResult.builder().success(false).msg(convertCoreResult.getMsg()).build();
            }
            if (productizationEventService != null) {
                // 数据同步成功
                applicationEventService.next(AppEventSubject.SYNC_SUCCESS, sourceEntity);
                final TransferEntity finalTransferEntity = transferEntity;
                ThreadPoolService.CS_EVENT_TASK_GROUP().submit(() -> {
                    try {
                        List<EventMessage> list = productizationEventService.inferEvent(sourceEntity, finalTransferEntity);
                        if (list.stream().anyMatch(EventMessage::isSuccess)) {
                            applicationEventService.next(AppEventSubject.EVENT_MATCHED, sourceEntity);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        log.error(e.getMessage(), e);
                    }
                    MDC.clear();
                });
            }
            success = true;
            msg = convertCoreResult.getMsg();
        } catch (Exception ex) {
            log.error("error in convert business entity: {}, message: {}", sourceEntity, ex.getMessage(), ex);
            msg = ex.toString();
            success = false;
            ex.printStackTrace();
        }
        MDC.clear();
        return SyncResult.builder()
                .msg(msg)
                .success(success)
                .build();
    }

}
