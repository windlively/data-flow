package ink.andromeda.dataflow.datasource.dao;


import ink.andromeda.dataflow.core.TransferEntity;
import ink.andromeda.dataflow.util.GeneralTools;
import ink.andromeda.dataflow.util.SqlGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 自动化的核心库dao服务, 需要数据库及实体类命名完全规范
 */
@Service
@Slf4j
public class AutoConfigurableCoreTableDao {

    private final CoreTableDao coreTableDao;

    private final ConversionService conversionService = GeneralTools.conversionService();

    public AutoConfigurableCoreTableDao(CoreTableDao coreTableDao) {
        this.coreTableDao = coreTableDao;
    }

    public Map<String, Object> findOriginal(TransferEntity entity, boolean ignoreNull, String... qualifiedField){
        String SQL = SqlGenerator.genSelect(entity.getData(),entity.getSource(), ignoreNull, qualifiedField);
        log.debug(SQL);
        Map<String, Object> result;
        try {
            // 有可能发生查出多条结果的异常
            result = coreTableDao.commonSelect(SQL);
        }catch (Exception ex){
            throw new IllegalStateException(String.format("exception in SQL: [%s], message: %s", SQL, ex.getMessage()));
        }
        // 未查询到结果
        if(result == null) {
            log.info("SQL: [{}] result is null.", SQL);
            return null;
        }
        return result;
    }

    public int update(TransferEntity newEntity, TransferEntity oldEntity, boolean selective, String... qualifiedField){
        if(selective)
            oldEntity.getData().forEach(newEntity.getData()::putIfAbsent);
        String SQL = SqlGenerator.genUpdate(newEntity.getData(), newEntity.getSource(), qualifiedField);
        log.info("update sql: {}", SQL);
        return coreTableDao.commonUpdate(SQL);
    }

    public int insert(TransferEntity entity){
        String SQL = SqlGenerator.genInsert(entity.getData(), entity.getSource(), true);
        log.info("insert sql: {}", SQL);
        return coreTableDao.commonInsert(SQL);
    }

    public int delete(TransferEntity entity, String... qualifiedField){
        String SQL = SqlGenerator.genDelete(entity.getData(), entity.getSource(), qualifiedField);
        log.info("delete sql: {}", SQL);
        return coreTableDao.commonDelete(SQL);
    }

}
