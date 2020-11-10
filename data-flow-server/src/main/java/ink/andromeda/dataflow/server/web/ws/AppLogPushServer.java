package ink.andromeda.dataflow.server.web.ws;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 向客户端推送日志
 */
@Component
@ServerEndpoint("/ws/log")
@Slf4j
public class AppLogPushServer {

    private Session session;

    private static ConcurrentHashMap<String, AppLogPushServer> clients = new ConcurrentHashMap<>();

    private static Queue<String> logCache = new LinkedList<>();

    @OnOpen
    public void onOpen(Session session) {
        this.session = session;
        clients.put(session.getId(), this);
        try {
            for (String s : logCache) {
                session.getBasicRemote().sendText(s);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        log.info("open web socket, id: {}, uri: {}", session.getId(), session.getRequestURI());
    }

    @OnClose
    public void onClose() {
        clients.remove(session.getId());
        log.info("close web socket, id: {}, uri: {}", session.getId(), session.getRequestURI());
    }

    @OnError
    public void onError(Throwable throwable, Session session) {
        throwable.printStackTrace();
        log.error("error in web socket, id: {}, uri: {}, message: {}", session.getId(), session.getRequestURI(), throwable.getMessage(), throwable);
    }

    public static void pushLog(String log) {
        logCache.offer(log);
        if (logCache.size() > 2000)
            logCache.poll();
        if (clients.isEmpty())
            return;
        clients.values().forEach(c -> {
            try {
                c.session.getBasicRemote().sendText(log);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }


    public static void main(String[] args) {
        Queue<String> queue = new LinkedList<>();
        for (int i = 0; i < 10; i++) {
            queue.offer("" + i);
        }
        queue.forEach(System.out::println);
        System.out.println("===");
        while (!queue.isEmpty()) {
            System.out.println(queue.poll());
        }
    }

}
