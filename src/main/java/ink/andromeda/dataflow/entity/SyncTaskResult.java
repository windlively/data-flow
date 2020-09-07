package ink.andromeda.dataflow.entity;

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

    private SourceEntity sourceEntity;

}
