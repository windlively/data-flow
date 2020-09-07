package ink.andromeda.dataflow.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TransferEntity {

    private String source;

    private String schema;

    private String name;

    private String opType;

    private Map<String, Object> data;

}
