package ink.windlively.dataflow.monitor;

import ink.windlively.dataflow.core.FlowFailInfo;
import ink.windlively.dataflow.core.SourceEntity;
import org.springframework.lang.Nullable;

/**
 * 监控数据收集
 */
public interface AppStatusCollector {

    default void receiveOneMsg(SourceEntity sourceEntity) {}

    default void processOneMsg(SourceEntity sourceEntity) {}

    default void inflowOne(String flowName, SourceEntity sourceEntity) {}

    default void successOne(String flowName, SourceEntity sourceEntity) {}

    default void failedOne(String flowName, SourceEntity sourceEntity) {}

    default void failedOne(String flowName, SourceEntity sourceEntity, @Nullable FlowFailInfo failInfo) {}

    AppStatusCollector EMPTY_COLLECTOR = new AppStatusCollector() {};

}
