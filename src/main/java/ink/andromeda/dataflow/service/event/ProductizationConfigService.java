package ink.andromeda.dataflow.service.event;

import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import ink.andromeda.dataflow.datasource.dao.ProductizationConfigDao;
import net.abakus.coresystem.dynamicdatasource.DynamicDataSource;
import net.abakus.coresystem.entity.TableField;
import net.abakus.coresystem.entity.po.ProdDatasourceConf;
import net.abakus.coresystem.entity.po.ProdLinkRelationConf;
import net.abakus.coresystem.redis.RedisClient;
import net.abakus.coresystem.util.CommonUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.ConcurrentReferenceHashMap;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;

/**
 * 产品化配置service
 */
@Service
@Slf4j
public class ProductizationConfigService {

    private final ProductizationConfigDao configDao;

    private final DynamicDataSource dynamicDataSource;

    public static final String PROD_SOURCE_PREFIX = "$prod-source:";

    public ProductizationConfigService(ProductizationConfigDao configDao,
                                       DynamicDataSource dynamicDataSource) {

        this.configDao = configDao;
        this.dynamicDataSource = dynamicDataSource;
    }

    // @PostConstruct
    public void init() {
        refreshProductizationDataSourceMap();
    }

    public synchronized void refreshProductizationDataSourceMap() {
        List<ProdDatasourceConf> allDataSourceConfig = configDao.findAllDataSourceConfig();

        // 移除所有产品化配置的数据源
        dynamicDataSource.getIncludedDataSource()
                .keySet()
                .stream()
                .filter(k -> k instanceof String && ((String) k).startsWith(PROD_SOURCE_PREFIX))
                .forEach(lookUpKey -> {
                    DataSource dataSource = dynamicDataSource.removeDataSource(lookUpKey);
                    ((HikariDataSource) dataSource).close();
                });

        // 重新载入
        allDataSourceConfig.forEach(prodDatasourceConf -> {
            DataSource dataSource = CommonUtils.buildDataSource(prodDatasourceConf.getUrl(),
                    prodDatasourceConf.getPort(),
                    prodDatasourceConf.getUserName(),
                    prodDatasourceConf.getPassword(),
                    prodDatasourceConf.getSchemaName(),
                    prodDatasourceConf.getArgs());
            dynamicDataSource.appendDataSource(PROD_SOURCE_PREFIX + prodDatasourceConf.getSchemaName(), dataSource);
        });
    }

    public void refreshCache() {
        schemaWithsTablesRefCache.clear();
        tableWithFieldsRefCache.clear();
        tableLinkRelationCache.clear();
    }

    /**
     * 数据库所包含的表
     */
    private final Map<String, List<String>> schemaWithsTablesRefCache =
            new ConcurrentReferenceHashMap<>(16, ConcurrentReferenceHashMap.ReferenceType.WEAK);

    /**
     * 表所包含的字段
     */
    private final Map<String, List<TableField>> tableWithFieldsRefCache =
            new ConcurrentReferenceHashMap<>(16, ConcurrentReferenceHashMap.ReferenceType.WEAK);

    /**
     * 表连接关系
     */
    private final Map<String, List<String>> tableLinkRelationCache =
            new ConcurrentReferenceHashMap<>(16, ConcurrentReferenceHashMap.ReferenceType.WEAK);

    public List<String> getTablesInSchema(String schema) {
        return schemaWithsTablesRefCache.computeIfAbsent(schema, k -> configDao.findTablesInSchema(schema));
    }

    public List<TableField> getFieldsInSchema(String schema, String table) {
        return tableWithFieldsRefCache.computeIfAbsent(schema + "." + table, k -> configDao.findFieldsInTable(schema, table));
    }

    public List<String> getLinkTables(String schema, String table) {
        return tableLinkRelationCache.computeIfAbsent(schema + "." + table, k -> configDao.findLinkTables(schema, table));
    }

    public ProdLinkRelationConf getProdLinkRelationCondition(ProdLinkRelationConf prodLinkRelationConf) {
        return configDao.findLinkRelationConf(prodLinkRelationConf);
    }

    public List<String> getAllSchemaNames() {
        return configDao.findAllSchemaNames();
    }

}
