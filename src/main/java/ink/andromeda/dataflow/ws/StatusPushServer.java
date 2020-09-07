package ink.andromeda.dataflow.ws;

import ink.andromeda.dataflow.service.ApplicationMonitor;
import lombok.extern.slf4j.Slf4j;
import net.abakus.coresystem.redis.RedisClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static net.abakus.coresystem.util.CommonUtils.toJSONString;

@Component
@ServerEndpoint("/ws/monitor/status")
@Slf4j
public class StatusPushServer {

    private Session session;

    @Autowired
    private ApplicationMonitor applicationMonitor;

    private static final ConcurrentHashMap<String, Session> clients = new ConcurrentHashMap<>();

    @OnOpen
    public void onOpen(Session session) {
        this.session = session;
        clients.put(session.getId(), session);
        log.info("open web socket, id: {}, uri: {}", session.getId(), session.getRequestURI());
    }

    @OnClose
    public void onClose() {
        clients.remove(session.getId());
        log.info("close web socket, id: {}, uri: {}", session.getId(), session.getRequestURI());
    }

    @OnError
    public void onError(Throwable throwable, Session session) {
        clients.remove(session.getId());
        log.error("error in web socket, id: {}, uri: {}, message: {}", session.getId(), session.getRequestURI(), throwable.getMessage(), throwable);
    }

    @PostConstruct
    public void init(){
        Executors.newSingleThreadScheduledExecutor().scheduleWithFixedDelay(() -> {
            if(clients.size() == 0)
                return;
            String message = toJSONString(applicationMonitor.getServerStatus());
            clients.values().forEach( c ->{
                try {
                    c.getBasicRemote().sendText(message);
                } catch (IOException e) {
                    log.error(e.getMessage(), e);
                }
            });
        }, 0, 2000, TimeUnit.MILLISECONDS);
    }

    public static void pushStatus(String msg) {
        if (clients.isEmpty())
            return;

        clients.values().forEach(c -> {
            try {
                c.getBasicRemote().sendText(msg);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}
