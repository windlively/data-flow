package ink.andromeda.dataflow.util;


import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.lang.NonNull;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 当父线程的ThreadLocal携带有TraceId时，传递给线程池中的子线程
 * 需要传入{@link ExecutorService}才能正常工作
 */
@Slf4j
public class TraceIdThreadPool extends AbstractExecutorService {

    @Getter
    private final ExecutorService executor;

    private static final String TRACE_ID_CONST = "traceId";

    public TraceIdThreadPool(ExecutorService executor) {
        this.executor = executor;
    }

    @Override
    public void execute(@NonNull Runnable command) {
        String traceId = MDC.get(TRACE_ID_CONST);
        executor.execute(() -> {
            if (!StringUtils.isEmpty(traceId))
                MDC.put(TRACE_ID_CONST, traceId);
            try {
                command.run();
            } catch (Throwable tx) {
                log.error("{}", tx.getMessage(), tx);
            } finally {
                MDC.remove("traceId");
            }
        });
    }

    @Override
    public void shutdown() {
        executor.shutdown();
    }

    @Override
    @NonNull
    public List<Runnable> shutdownNow() {
        return executor.shutdownNow();
    }

    @Override
    public boolean isShutdown() {
        return executor.isShutdown();
    }

    @Override
    public boolean isTerminated() {
        return executor.isTerminated();
    }

    @Override
    public boolean awaitTermination(long timeout, @NonNull TimeUnit unit) throws InterruptedException {
        return executor.awaitTermination(timeout, unit);
    }
}
