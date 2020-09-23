package ink.andromeda.dataflow.datasource.dao;

import ink.andromeda.dataflow.datasource.mapper.CoreTableMapper;
import ink.andromeda.dataflow.entity.HistoryDataConsumeInfo;
import ink.andromeda.dataflow.entity.ColumnInfo;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public class CoreTableDao {

    private final CoreTableMapper coreTableMapper;

    public CoreTableDao(CoreTableMapper coreTableMapper) {
        this.coreTableMapper = coreTableMapper;
    }

    // 通用查询
    public Map<String, Object> commonSelect(String sql){
        return coreTableMapper.commonSelect(sql);
    }

    // 通用的多条结果查询
    public List<Map<String, Object>> commonMultiResultSelect(String sql){
        return coreTableMapper.commonMultiResultSelect(sql);
    }

    public int commonInsert(String sql){
        return coreTableMapper.commonInsert(sql);
    }

    public int commonUpdate(String sql){
        return coreTableMapper.commonUpdate(sql);
    }

    public int commonDelete(String sql){
        return coreTableMapper.commonDelete(sql);
    }

    // 查询核心库所有的表
    public List<String> findAllTables(){
        return coreTableMapper.findAllTables();
    };

    // 查询核心库所有的列
    public List<ColumnInfo> findAllColumns(){
        return coreTableMapper.findAllColumns();
    };

    // 查询某个表的所有列
    public List<ColumnInfo> findAllColumns(String tableName, String schemaName){
        return coreTableMapper.findAllColumnsWithTableName(tableName, schemaName);
    };

    public int insertHistoryDataConsumeInfo(HistoryDataConsumeInfo info){
        return coreTableMapper.insertHistoryDataConsumeInfo(info);
    }

}
