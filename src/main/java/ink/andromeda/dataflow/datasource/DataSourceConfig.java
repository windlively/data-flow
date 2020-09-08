package ink.andromeda.dataflow.datasource;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class DataSourceConfig {

    private List<Map<String, Object>> hikari;

}
