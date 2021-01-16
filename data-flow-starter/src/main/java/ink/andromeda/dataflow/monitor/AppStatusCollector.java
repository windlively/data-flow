package ink.andromeda.dataflow.monitor;

import ink.andromeda.dataflow.core.FlowFailInfo;
import ink.andromeda.dataflow.core.SourceEntity;
import org.springframework.lang.Nullable;

public interface AppStatusCollector {

    default void receiveOne(SourceEntity sourceEntity) {}

    default void inflowOne(String flowName, SourceEntity sourceEntity) {}

    default void successOne(String flowName, SourceEntity sourceEntity) {}

    default void failedOne(String flowName, SourceEntity sourceEntity) {}

    default void failedOne(String flowName, SourceEntity sourceEntity, @Nullable FlowFailInfo failInfo) {}

}
