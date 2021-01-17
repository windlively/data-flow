package ink.windlively.dataflow.core;

import lombok.Builder;
import lombok.Data;
import org.springframework.lang.Nullable;

@Data
@Builder
public class FlowFailInfo {

    @Nullable
    private String position;

    @Nullable
    private Throwable throwable;

}
