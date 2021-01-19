package ink.windlively.dataflow.monitor;

import ink.windlively.dataflow.core.FlowFailInfo;
import ink.windlively.dataflow.core.SourceEntity;
import org.springframework.lang.Nullable;

/**
 * 监控数据收集
 */
public interface AppStatusCollector {

    /**
     * 收到一条(SourceEntity)消息
     */
    default void receiveOneMsg(SourceEntity sourceEntity) {
    }

    /**
     * 处理一条(SourceEntity)消息
     */
    default void processOneMsg(SourceEntity sourceEntity) {
    }

    /**
     * 流收到一条数据
     */
    default void inflowOne(String flowName, SourceEntity sourceEntity) {
    }

    /**
     * 流成功处理一条数据
     */
    default void successOne(String flowName, SourceEntity sourceEntity) {
    }

    /**
     * 流处理数据时发生异常
     */
    default void failedOne(String flowName, SourceEntity sourceEntity) {
    }

    /**
     * 流处理数据时发生异常
     *
     * @param flowName     流的名称
     * @param sourceEntity 原始消息数据
     * @param failInfo     失败信息
     */
    default void failedOne(String flowName, SourceEntity sourceEntity, @Nullable FlowFailInfo failInfo) {
    }

    /**
     * 空的收集器，不作任何操作
     */
    AppStatusCollector EMPTY_COLLECTOR = new AppStatusCollector() {
    };

}
