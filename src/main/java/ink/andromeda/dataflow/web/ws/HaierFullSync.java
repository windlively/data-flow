package ink.andromeda.dataflow.web.ws;

import ink.andromeda.dataflow.redis.RedisClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static ink.andromeda.dataflow.util.GeneralUtils.toJSONString;

@Component
@ServerEndpoint("/ws/haier-full-sync")
@Slf4j
public class HaierFullSync {

    private Session session;

    private RedisClient redisClient;

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
        throwable.printStackTrace();
        log.error("error in web socket, id: {}, uri: {}, message: {}", session.getId(), session.getRequestURI(), throwable.getMessage(), throwable);
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

    @PostConstruct
    public void init(){
        Executors.newSingleThreadScheduledExecutor().scheduleWithFixedDelay(() -> {
            if(clients.size() == 0)
                return;
            clients.values().forEach( c ->{
                try {
                    Map<String, String> data = redisClient.hgetAll("HAIER_FULL_SYNC_STATUS");
                    c.getBasicRemote().sendText(toJSONString(data));
                } catch (IOException e) {
                    log.error(e.getMessage(), e);
                }
            });
        }, 0, 1000, TimeUnit.MILLISECONDS);

    }
}
