package ink.andromeda.dataflow.datasource.mapper;

import ink.andromeda.dataflow.entity.ColumnInfo;
import ink.andromeda.dataflow.entity.HistoryDataConsumeInfo;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Mapper
@Component
public interface CoreTableMapper {

    @Select("${sql}")
    Map<String, Object> commonSelect(@Param("sql") String sql);

    @Select("${sql}")
    List<Map<String, Object>> commonMultiResultSelect(@Param("sql") String sql);

    @Insert("${sql}")
    int commonInsert(@Param("sql") String sql);

    @Update("${sql}")
    int commonUpdate(@Param("sql") String sql);

    @Delete("${sql}")
    int commonDelete(@Param("sql") String sql);

    @Select("SELECT TABLE_NAME tableName from information_schema.TABLES where TABLE_SCHEMA = 'core_system'")
    List<String> findAllTables();

    @Select("SELECT TABLE_NAME tableName, COLUMN_NAME columnName, DATA_TYPE dataType FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = 'core_system'")
    List<ColumnInfo> findAllColumns();

    @Select("SELECT TABLE_NAME tableName, COLUMN_NAME columnName, DATA_TYPE dataType FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = #{schemaName} AND TABLE_NAME = #{tableName}")
    List<ColumnInfo> findAllColumnsWithTableName(@Param("tableName") String tableName, @Param("schemaName") String schemaName);

    @Insert("INSERT INTO history_data_consume_info VALUES (DEFAULT, #{orderNo}, #{batchNo}, #{processCount}, NOW())")
    int insertHistoryDataConsumeInfo(HistoryDataConsumeInfo info);
}
