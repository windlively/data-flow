package ink.andromeda.dataflow.service;

import ink.andromeda.dataflow.entity.HttpResult;
import lombok.extern.slf4j.Slf4j;
import ink.andromeda.dataflow.ws.AppLogPushServer;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @deprecated WebSocket已可以正常使用
 * @see AppLogPushServer
 */
@Slf4j
@Service
public class LogPushService {

    public void pushLogToClient(HttpServletResponse response) {
        response.addHeader("Cache-Control", "no-cache, no-transform");
        response.addHeader("X-Accel-Buffering", "no");
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/event-stream");
        String id = UUID.randomUUID().toString();
        Deque<String> logQueue = new ArrayDeque<>(10);
        logs.put(id, logQueue);
        log.info("sse connected, tempId: {}", id);
        try (PrintWriter writer = response.getWriter()) {
            while (true) {
                String str = logQueue.poll();
                if (str != null) {

                    writer.write(("data: " + str + "\n\n"));
                    writer.flush();
                }
                if (writer.checkError()) {
                    return;
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            log.info("sse disconnected, tempId: {}", id);
            logs.remove(id);
        }
    }

    public static ConcurrentHashMap<String, Queue<String>> logs = new ConcurrentHashMap<>();

    public static ConcurrentHashMap<String, Long> clientLastAccessTime = new ConcurrentHashMap<>();

    static {
        logs.put("MASTER", new ArrayDeque<>());
    }

    public static void pushLog(String logMsg) {
        if (logs.isEmpty())
            return;
        for (Map.Entry<String, Queue<String>> entry : logs.entrySet()) {
            Queue<String> deque = entry.getValue();
            deque.offer(logMsg);
            if (deque.size() > 2000) {
                deque.poll();
            }
        }
    }

    // 客户端超过1min未请求日志则删除其所属队列
    @Scheduled(fixedRate = 1000 * 60)
    private void checkClientAlive(){
        long now = System.currentTimeMillis();
        clientLastAccessTime.entrySet()
                .stream()
                .filter(e -> !"MASTER".equals(e.getKey()) && now - e.getValue() > 60000)
                .forEach(e -> {
                    logs.remove(e.getKey());
                    clientLastAccessTime.remove(e.getKey());
                    log.info("remove log queue: {}", e.getKey());
                });
    }

    /**
     *  由于服务器端暂不支持WebSocket，因此使用轮询方式替代
     * @param id 客户端id
     */
    public HttpResult<Collection<String>> getLog(String id){
        Queue<String> result = new ArrayDeque<>();
        Queue<String> queue = logs.computeIfAbsent(id, k -> {
            log.info("add log queue: {}", id);
            return new ArrayDeque<>(logs.get("MASTER"));
        });
        clientLastAccessTime.put(id, System.currentTimeMillis());
        String log;
        while ( (log = queue.poll()) != null ){
            result.offer(log);
        }
        return HttpResult.SUCCESS("success", result);
    }

    public HttpResult<Integer> getLogQueueSize(){
        return HttpResult.SUCCESS(logs.size());
    }
}
