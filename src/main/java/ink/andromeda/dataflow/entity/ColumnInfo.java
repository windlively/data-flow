package ink.andromeda.dataflow.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import static net.abakus.coresystem.util.CommonUtils.tableColumnTypeToJavaType;

@ToString
@Data
@NoArgsConstructor
public class ColumnInfo {

    private String tableName;

    private String columnName;

    private String dataType;

    private Class<?> javaType;

    public void setDataType(String dataType) {
        this.dataType = dataType;
        this.javaType = tableColumnTypeToJavaType(dataType);
    }
}
