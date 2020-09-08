package ink.andromeda.dataflow.entity;

import ink.andromeda.dataflow.core.OriginEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SyncTaskResult {

    private boolean success;

    private String error;

    private int successCount;

    private OriginEntity sourceEntity;

}
