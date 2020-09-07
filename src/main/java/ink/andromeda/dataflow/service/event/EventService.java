package ink.andromeda.dataflow.service.event;

import ink.andromeda.dataflow.entity.SourceEntity;
import ink.andromeda.dataflow.entity.CoreEntity;

import java.util.List;

public interface EventService {

    List<EventMessage> inferEvent(SourceEntity sourceEntity, CoreEntity coreEntity);

    default List<EventMessage> inferEvent(SourceEntity sourceEntity) {
        throw new UnsupportedOperationException();
    }

}
