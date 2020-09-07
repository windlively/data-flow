package ink.andromeda.dataflow.service;

import lombok.extern.slf4j.Slf4j;
import net.abakus.coresystem.util.TraceIdThreadPool;

import java.util.concurrent.*;

@Slf4j
public class ThreadPoolService {

    private static final ExecutorService CS_SYNC_TASK_GROUP = new TraceIdThreadPool(new ThreadPoolExecutor(10, 100,
            60L, TimeUnit.SECONDS, new LinkedBlockingQueue<>()));

    private static final ExecutorService CS_EVENT_TASK_GROUP = new TraceIdThreadPool(new ThreadPoolExecutor(10, 100,
            60L, TimeUnit.SECONDS, new LinkedBlockingQueue<>()));

    private static final ExecutorService ASYNC_TASK_GROUP = new TraceIdThreadPool(new ThreadPoolExecutor(20, 100,
            1L,TimeUnit.MINUTES, new LinkedBlockingQueue<>(),
            r -> {
                Thread thread = new Thread(r);
                thread.setUncaughtExceptionHandler((t, e) -> log.error(e.getMessage(), e));
                return thread;
            }, new ThreadPoolExecutor.CallerRunsPolicy()));

    private static final ExecutorService SINGLE_POOL = new TraceIdThreadPool(Executors.newSingleThreadExecutor());

    private static final ExecutorService SUBSCRIBE_TASK_GROUP = new TraceIdThreadPool(Executors.newCachedThreadPool());


    public static ExecutorService CS_SYNC_TASK_GROUP() {
        return CS_SYNC_TASK_GROUP;
    }

    public static ExecutorService CS_EVENT_TASK_GROUP() {
        return CS_EVENT_TASK_GROUP;
    }

    public static ExecutorService ASYNC_TASK_GROUP() {
        return ASYNC_TASK_GROUP;
    }

    public static ExecutorService SUBSCRIBE_TASK_GROUP(){
        return SUBSCRIBE_TASK_GROUP;
    }

    public static ExecutorService SINGLE_POOL(){
        return SINGLE_POOL;
    }

}
