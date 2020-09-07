package ink.andromeda.dataflow.datasource.dao;


import ink.andromeda.dataflow.util.CommonUtils;
import lombok.extern.slf4j.Slf4j;
import ink.andromeda.dataflow.util.SqlGenerator;
import ink.andromeda.dataflow.entity.CoreEntity;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Service;

import java.util.Map;

import static ink.andromeda.dataflow.util.SqlGenerator.*;

/**
 * 自动化的核心库dao服务, 需要数据库及实体类命名完全规范
 */
@Service
@Slf4j
public class AutoConfigurableCoreTableDao {

    private final CoreTableDao coreTableDao;

    private final ConversionService conversionService = CommonUtils.conversionService();

    public AutoConfigurableCoreTableDao(CoreTableDao coreTableDao) {
        this.coreTableDao = coreTableDao;
    }

    public Map<String, Object> findOriginal(CoreEntity entity, boolean ignoreNull, String... qualifiedField){
        String SQL = genSelect(entity.getEntity(),entity.getName(), ignoreNull, qualifiedField);
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

    public int update(CoreEntity newEntity, CoreEntity oldEntity, boolean selective, String... qualifiedField){
        if(selective)
            oldEntity.getEntity().forEach(newEntity.getEntity()::putIfAbsent);
        String SQL = genUpdate(newEntity.getEntity(), newEntity.getName(), qualifiedField);
        log.info("update, SQL: {}", SQL);
        return coreTableDao.commonUpdate(SQL);
    }

    public int insert(CoreEntity entity){
        String SQL = SqlGenerator.genInsert(entity.getEntity(), entity.getName(), true);
        log.info("insert, SQL: {}", SQL);
        return coreTableDao.commonInsert(SQL);
    }

    public int delete(CoreEntity entity, String... qualifiedField){
        String SQL = genDelete(entity.getEntity(), entity.getName(), qualifiedField);
        log.info("delete, SQL {}", SQL);
        return coreTableDao.commonDelete(SQL);
    }

}
