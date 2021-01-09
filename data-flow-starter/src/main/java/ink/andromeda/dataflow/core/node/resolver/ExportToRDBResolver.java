package ink.andromeda.dataflow.core.node.resolver;

import ink.andromeda.dataflow.core.SourceEntity;
import ink.andromeda.dataflow.core.SpringELExpressionService;
import ink.andromeda.dataflow.core.TransferEntity;
import ink.andromeda.dataflow.datasource.dao.CommonJdbcDao;
import ink.andromeda.dataflow.util.SQLGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 将输入数据导出到关系型数据库
 */
@Slf4j
public class ExportToRDBResolver extends DefaultConfigurationResolver {

    private final CommonJdbcDao commonJdbcDao;

    public ExportToRDBResolver(SpringELExpressionService expressionService,
                               CommonJdbcDao commonJdbcDao) {
        super(expressionService);
        this.commonJdbcDao = commonJdbcDao;
    }

    @Override
    public String getName() {
        return "export_to_rdb";
    }

    private long timestamp;

    private long elapsedTime;


    @SuppressWarnings("unchecked")
    @Override
    @Transactional
    public void resolve(SourceEntity source, TransferEntity input, TransferEntity target, Object config, Map<String, Object> rootData) throws Exception {
        if (config == null) return;
        checkConfigType(config, Map.class, "Map<String,Object>");
        String method = (String) ((Map<String, Object>) config)
                .getOrDefault("method", "upsert");
        String targetDataSource = (String) ((Map<String, Object>) config).getOrDefault("target_data_source", "master");
        String targetSchema = (String) ((Map<String, Object>) config).get("target_schema");
        String targetTable = (String) ((Map<String, Object>) config).get("target_table");
        boolean sqlLog = (boolean) ((Map<String, Object>) config).getOrDefault("sql_log", true);

        Map<String, Object> data = target.getData();
        String dataExpression;
        if ((dataExpression = (String) ((Map<?, ?>) config).get("data")) != null) {
            data = Objects.requireNonNull(expressionService.executeExpression(dataExpression, Map.class));
        }

        switch (method) {
            case "upsert": {
                Map<String, Object> findOriginalConfig =
                        (Map<String, Object>) ((Map<?, ?>) config).get("find_original");

                String sql;

                if ((sql = (String) findOriginalConfig.get("sql")) != null) {
                    sql = Objects.requireNonNull(
                            expressionService.executeExpression(sql, String.class, true)
                    );
                } else {
                    sql = SQLGenerator.genSelectSQL(data,
                            null,
                            (List<String>) findOriginalConfig.get("match_fields"),
                            targetSchema,
                            targetTable
                    );
                }

                timestamp = System.currentTimeMillis();
                Object original = commonJdbcDao.select(sql, targetDataSource, "map");
                elapsedTime = System.currentTimeMillis() - timestamp;

                if(sqlLog) log.info("select original sql [{}], result: {}, used time: {}ms", sql, original, elapsedTime);

                if (original == null) {
                    Map<String, Object> insertConfig = (Map<String, Object>) ((Map<?, ?>) config).get("insert");
                    insert(insertConfig, data, targetDataSource, targetSchema, targetTable, sqlLog);
                } else {
                    Map<String, Object> updateConfig = (Map<String, Object>) ((Map<?, ?>) config).get("update");
                    if ((sql = (String) updateConfig.get("sql")) != null) {
                        sql = Objects.requireNonNull(
                                expressionService.executeExpression(sql, String.class, true)
                        );
                    } else {
                        sql = SQLGenerator.genUpdateSQL(data,
                                (Map<String, String>) updateConfig.get("custom_fields"),
                                (List<String>) updateConfig.get("match_fields"),
                                targetSchema,
                                targetTable);
                    }
                    commonJdbcDao.setDataSource(targetDataSource);

                    timestamp = System.currentTimeMillis();
                    commonJdbcDao.update(sql);
                    elapsedTime = System.currentTimeMillis() - timestamp;

                    if(sqlLog) log.info("update data sql: [{}], used time: {}ms", sql, elapsedTime);
                }
                commonJdbcDao.setDataSource("master");
                break;
            }
            case "insert": {
                insert((Map<String, Object>) ((Map<?, ?>) config).get("insert"), data, targetDataSource, targetSchema, targetTable, sqlLog);
                break;
            }
            default:
                throw new IllegalArgumentException("unknown export to rdb method: " + method);
        }

    }

    private int insert(Map<String, Object> insertConfig, Map<String, Object> data, String dataSource, String schema, String table, boolean sqlLog) {
        String sql;
        if ((sql = (String) insertConfig.get("sql")) != null) {
            sql = Objects.requireNonNull(
                    expressionService.executeExpression(sql, String.class, true)
            );
        } else {
            //noinspection unchecked
            sql = SQLGenerator.genInsertSQL(data,
                    (Map<String, String>) insertConfig.get("custom_fields"),
                    schema,
                    table);
        }
        commonJdbcDao.setDataSource(dataSource);

        timestamp = System.currentTimeMillis();
        int i = commonJdbcDao.insert(sql);
        elapsedTime = System.currentTimeMillis() - timestamp;
        if(sqlLog) log.info("insert data sql [{}], used time: {}ms", sql, elapsedTime);
        return i;
    }
}
