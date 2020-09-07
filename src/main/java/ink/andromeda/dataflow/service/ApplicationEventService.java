package ink.andromeda.dataflow.service;

import ink.andromeda.dataflow.entity.AppEventSubject;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * 应用内部的订阅/发布服务
 */
@Service
@Slf4j
public class ApplicationEventService {

    @Getter
    private final Map<AppEventSubject, Map<String, Consumer<Object>>> subscribeList = new ConcurrentHashMap<>();

    /**
     * @see #next(AppEventSubject, Object, boolean, boolean)
     */
    public void next(@NonNull AppEventSubject subject, Object msg) {
        next(subject, msg, true, false);
    }

    /**
     * 发布消息
     *
     * @param subject     发布内容的主题
     * @param msg         发布的消息
     * @param async       是否异步
     * @param ordered 是否按发布顺序执行, 非异步时该参数无效
     */
    public void next(@NonNull AppEventSubject subject, Object msg, boolean async, boolean ordered) {
        if (async) {
//            if (ordered)
//                ThreadPoolService.SINGLE_POOL().execute(() -> apply(subject, msg));
//            else
                ThreadPoolService.SUBSCRIBE_TASK_GROUP().execute(() -> apply(subject, msg));
            return;
        }
        apply(subject, msg);
    }

    private void apply(AppEventSubject subject, Object msg) {
        try {
            Optional.ofNullable(subscribeList.get(subject)).ifPresent(subscriber -> subscriber.values().forEach(consumer -> consumer.accept(msg)));
        }catch (Exception ex){
            log.error("error in application service: {}", ex.getMessage(), ex);
        }

    }

    // 订阅主题
    public void subscribe(@NonNull AppEventSubject subject, @NonNull String name, @NonNull Consumer<Object> action) {
        Map<String, Consumer<Object>> subjectList = subscribeList.computeIfAbsent(subject, k -> new ConcurrentHashMap<>());
        if (subjectList.get(name) != null)
            throw new IllegalArgumentException(String.format("subject: %s, name: %s, already exists! please use a new name.", subject, name));
        subjectList.put(name, action);
    }

    ;

    // 取消订阅
    public void unsubscribe(@NonNull AppEventSubject subject, @NonNull String name) {
        Map<String, Consumer<Object>> subjectList = subscribeList.get(subject);
        if (subjectList != null) {
            subjectList.remove(name);
            if (subjectList.isEmpty())
                subscribeList.remove(subject);
        }
    }
}
