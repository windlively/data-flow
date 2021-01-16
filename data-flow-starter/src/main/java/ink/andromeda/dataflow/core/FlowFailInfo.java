package ink.andromeda.dataflow.core;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FlowFailInfo {

    private String position;

    private Throwable throwable;

}
