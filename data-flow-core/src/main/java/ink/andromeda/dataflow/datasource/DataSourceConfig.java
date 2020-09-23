package ink.andromeda.dataflow.datasource;

import com.zaxxer.hikari.HikariConfig;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class DataSourceConfig {

    private HikariConfig[] hikari;

}
