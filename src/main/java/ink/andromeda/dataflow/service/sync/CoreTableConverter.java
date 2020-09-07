package ink.andromeda.dataflow.service.sync;

import ink.andromeda.dataflow.datasource.dao.AutoConfigurableCoreTableDao;
import ink.andromeda.dataflow.datasource.dao.CoreTableDao;
import ink.andromeda.dataflow.entity.*;
import ink.andromeda.dataflow.service.ExpressionService;
import ink.andromeda.dataflow.util.CommonUtils;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.lang.NonNull;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;


/**
 * 业务端表 -> 核心表的转换器
 * 该类应为线程安全, 不含有或保存状态
 */
@Slf4j
@Getter
@ToString
public class CoreTableConverter {

    // 直接转发, 不转换和存储
    private final boolean directForward;

    // 对应的核心库表名
    private final String destTable;

    // 简单逻辑的属性值设置
    private final Map<String, String> simpleValueConvert;

    // 是否进行简单的类型转换(直接进行相同字段赋值)
    private final boolean simpleClassConvert;

    private final List<String> customOperation;

    // 带条件的操作
    private final List<Map<String, Object>> conditional;

    // 入库配置
    private final Map<String, Object> dbUpdate;

    // 表达式解析环境配置(转换时所依赖的数据)
    private final List<Map<String, Object>> evalContextConfig;

    private final ExpressionService expressionService;

    // 反射方法、字段缓存数据, 使用spring的反射工具类代替
    // private final ReflectCache reflectCache;

    private final AutoConfigurableCoreTableDao autoConfigurableCoreTableDao;

    private final CoreTableDao coreTableDao;

    // 转换器名称
    private final String name;

    private final Map<String, Object> enums;

    // 过滤条件, 符合该表达式时将被过滤
    private final String filter;

    // 原始配置内容
    private final Map<String, Object> originalConfig;

    // 是否处理删除操作
    private final boolean concernDelete;

    private final String postProcessor;

    private CoreTableConverter(String destTable,
                               Map<String, String> simpleValueConvert,
                               List<String> customOperation, List<Map<String, Object>> conditional,
                               List<Map<String, Object>> evalContextConfig,
                               Map<String, Object> dbUpdate,
                               boolean simpleClassConvert,
                               ExpressionService expressionService,
                               AutoConfigurableCoreTableDao autoConfigurableCoreTableDao,
                               CoreTableDao coreTableDao,
                               String name,
                               Map<String, Object> enums,
                               String filter,
                               Map<String, Object> originalConfig,
                               String postProcessor,
                               boolean concernDelete) {
        this(
                destTable,
                simpleValueConvert,
                customOperation,
                conditional,
                evalContextConfig,
                dbUpdate,
                simpleClassConvert,
                expressionService,
                autoConfigurableCoreTableDao,
                coreTableDao,
                name,
                enums,
                filter,
                originalConfig,
                concernDelete,
                false,
                postProcessor
        );
    }

    private CoreTableConverter(String destTable,
                               Map<String, String> simpleValueConvert,
                               List<String> customOperation, List<Map<String, Object>> conditional,
                               List<Map<String, Object>> evalContextConfig,
                               Map<String, Object> dbUpdate,
                               boolean simpleClassConvert,
                               ExpressionService expressionService,
                               AutoConfigurableCoreTableDao autoConfigurableCoreTableDao,
                               CoreTableDao coreTableDao,
                               String name,
                               Map<String, Object> enums,
                               String filter,
                               Map<String, Object> originalConfig,
                               boolean concernDelete,
                               boolean directForward,
                               String postProcessor) {

        this.destTable = destTable;
        this.simpleValueConvert = simpleValueConvert;
        this.customOperation = customOperation;
        this.conditional = conditional;
        this.evalContextConfig = evalContextConfig;
        this.dbUpdate = dbUpdate;
        this.expressionService = expressionService;
        this.simpleClassConvert = simpleClassConvert;
        this.autoConfigurableCoreTableDao = autoConfigurableCoreTableDao;
        this.coreTableDao = coreTableDao;
        this.name = name;
        this.enums = enums;
        this.filter = filter;
        this.originalConfig = originalConfig;
        this.concernDelete = concernDelete;
        this.directForward = directForward;
        this.postProcessor = postProcessor;
    }

    // 从单个convert配置实例化一个转换器
    public static CoreTableConverter buildFromConverterConfig(Map<String, Object> converterConfig,
                                                              ExpressionService evalContextService,
                                                              AutoConfigurableCoreTableDao autoConfigurableCoreTableDao,
                                                              CoreTableDao coreTableDao,
                                                              String name,
                                                              Map<String, Object> extraConfig) {
        String destTableName = (String) converterConfig.get("dest_table");
        Map<String, Object> originalConfig = converterConfig;
        // converter之外的配置, 优先级低于当前转换器的配置
        extraConfig.forEach(originalConfig::putIfAbsent);
        //noinspection unchecked
        return new CoreTableConverter(destTableName,
                (Map<String, String>) originalConfig.get("simple_value_convert"),
                (List<String>) originalConfig.get("custom_operation"),
                (List<Map<String, Object>>) originalConfig.get("conditional"),
                (List<Map<String, Object>>) originalConfig.getOrDefault("eval_context", Collections.emptyList()),
                (Map<String, Object>) originalConfig.get("db_update"),
                (boolean) originalConfig.getOrDefault("simple_class_convert", false),
                evalContextService,
                autoConfigurableCoreTableDao,
                coreTableDao,
                name,
                (Map<String, Object>) originalConfig.get("enums"),
                (String) originalConfig.get("filter"),
                originalConfig,
                (boolean) originalConfig.getOrDefault("concern_delete", false),
                (boolean) originalConfig.getOrDefault("direct_forward", false),
                (String) originalConfig.get("post_processor")
        );
    }

    public static CoreTableConverter directForwardConverter() {
        return new CoreTableConverter(
                null,
                null,
                null,
                null,
                null,
                null,
                false,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                "",
                true);
    }

    // 递归执行条件语句
    private void processConditional(Map<String, Object> conditionalItem, StandardEvaluationContext context) {
        String condition = (String) conditionalItem.get("condition");

        if (Objects.requireNonNull(expressionService.executeExpression(condition, context, boolean.class))) {
            Object expression = conditionalItem.get("expression");
            if (expression instanceof String) {
                expressionService.executeExpression((String) expression, context);
                return;
            }
            if (expression instanceof List) {
                //noinspection unchecked
                ((List<Object>) expression).forEach(exp -> {
                    if (exp instanceof String) {
                        expressionService.executeExpression((String) exp, context);
                        return;
                    }
                    if (exp instanceof Map) {
                        //noinspection unchecked
                        processConditional((Map<String, Object>) exp, context);
                    }
                });
            }
        }
    }

    /**
     * 将BusinessBean转换为CoreEntity
     */
    @NonNull
    public CoreResult<TransferEntity> convertToCoreEntity(SourceEntity sourceEntity) {

        if (directForward) return CoreResult.<TransferEntity>builder()
                .success(true)
                .data(TransferEntity.builder()
                        .opType(sourceEntity.getOpType())
                        .data(sourceEntity.getData())
                        .source(sourceEntity.getName())
                        .build())
                .msg("direct forward")
                .build();

        StandardEvaluationContext evaluationContext = expressionService.evaluationContext();
        if (CanalType.isDelete(sourceEntity.getOpType())) {
            // 不考虑删除，直接返回
            if (!concernDelete)
                return new CoreResult<>("delete operation and config is not concern delete.", null, true);
            // 安全的删除操作, 仅可使用原始表的值
            if (dbUpdate.get("safe_delete") != null) {
                @SuppressWarnings("unchecked")
                Map<String, Object> safeDelete = (Map<String, Object>) dbUpdate.get("safe_delete");
                String sql = (String) safeDelete.get("sql");
                sourceEntity.getBefore().forEach(evaluationContext::setVariable);
                sql = expressionService.executeExpression(sql, evaluationContext, String.class, true);
                coreTableDao.commonDelete(sql);
                return CoreResult.<TransferEntity>builder()
                        .msg("delete operation.")
                        .build();
            }
        }
        Map<String, Object> contextRoot = new HashMap<>();
        TransferEntity transferEntity = new TransferEntity();
        transferEntity.setOpType(sourceEntity.getOpType());
        transferEntity.setSource(destTable);
        Map<String, Object> entity = new HashMap<>();
        transferEntity.setData(entity);
        List<ColumnInfo> columnInfos = expressionService.getColumnInfo(destTable);
        if (columnInfos == null || columnInfos.isEmpty()) {
            log.error("could not found column info for core table: {}", destTable);
            return CoreResult.<TransferEntity>builder()
                    .msg("could not found column info for core table: " + destTable)
                    .build();
        }
        evaluationContext.setRootObject(contextRoot);
        contextRoot.put("CE", entity);

        // 枚举类型
        if (enums != null)
            contextRoot.put("enums", enums);

        // 直接转换, 适用于两张表字段完全或大部分一致的情况
        if (simpleClassConvert)
            entity.putAll(sourceEntity.getData());

        // evaluation context 为线程级别的单例, 因此此处可能会传入所有类型的canalBaseBean的字段变量, 配置中不使用即可
        sourceEntity.getData().forEach(evaluationContext::setVariable);
        evaluationContext.setVariable("CANAL_TYPE", sourceEntity.getOpType());
        // 表达式执行的环境变量配置
        if (evalContextConfig != null) {
            evalContextConfig.forEach(item -> {
                @SuppressWarnings("unchecked") List<String> onOpTypes = (List<String>) item.get("on_op_types");
                if (onOpTypes != null && !onOpTypes.isEmpty()
                        && onOpTypes.stream().noneMatch(t -> sourceEntity.getOpType().equalsIgnoreCase(t.trim())))
                    return;
                String name = (String) item.get("name");
                if ("CE".equals(name)) {
                    log.error("数据项:[{}]命名与默认数据项名称冲突，跳过该项。", name);
                    return;
                }
                contextRoot.put(name, expressionService.genEvalContextVal(item, evaluationContext));
            });
        }

        // 直接设置的属性
        if (simpleValueConvert != null) {
            simpleValueConvert.forEach((k, v) -> {
                if (k != null && v != null) {
                    try {
                        Object val = expressionService.executeExpression(v, evaluationContext);
                        entity.put(k, val);
                    } catch (Exception ex) {
                        log.error("current field: {}, expression: {}, error message: {}", k, v, ex.toString(), ex);
                        throw new IllegalStateException(String.format("simple value convert error, current field: %s, expression: %s, error message: %s", k, v, ex.toString()), ex);
                    }
                }
            });
            // 根据数据库的列类型转换计算结果的格式
            /*
                columnInfos.forEach(columnInfo -> {
                    String columnName = columnInfo.getColumnName();
                    String expression;
                    if ((expression = simpleValueConvert.get(columnName)) != null) {
                        try {
                            Object val = expressionService.executeExpression(expression, evaluationContext);
                            entity.put(columnName, conversionService().convert(val, columnInfo.getJavaType()));
                        } catch (Exception ex) {
                            log.error("current field: {}, expression: {}, error message: {}", columnName, expression, ex.toString(), ex);
                            throw new IllegalStateException(String.format("simple value convert error, current field: %s, expression: %s, error message: %s", columnName, expression, ex.toString()), ex);
                        }
                    }
                });
            */
        }

        // 执行按条件触发的表达式
        if (conditional != null)
            conditional.forEach(item -> processConditional(item, evaluationContext));

        if (!StringUtils.isEmpty(filter)) {
            boolean match = expressionService.executeExpression(filter, evaluationContext, boolean.class);
            if (!match) {
                log.info("business bean: {} is filtered by [{}]", sourceEntity, filter);
                return CoreResult.<TransferEntity>builder()
                        .msg(String.format("filtered by [%s])", filter))
                        .build();
            }
        }

        // 删除操作, 在此时删除可能会存在异常
        if (CanalType.isDelete(sourceEntity.getOpType())) {
            if (concernDelete) {
                @SuppressWarnings("unchecked")
                Map<String, Object> deleteConfig = (Map<String, Object>) dbUpdate.get("delete");
                @SuppressWarnings({"unchecked"})
                String[] deleteQualifiedField = ((List<String>) deleteConfig.get("qualified_field")).toArray(new String[0]);
                autoConfigurableCoreTableDao.delete(transferEntity, deleteQualifiedField);
            }
            return CoreResult.<TransferEntity>builder()
                    .msg("delete operation.")
                    .build();
        }

        CoreResult<TransferEntity> coreResult = CoreResult.<TransferEntity>builder()
                .msg("success")
                .data(transferEntity)
                .success(true)
                .build();

        try {
            if (!StringUtils.isEmpty(postProcessor))
                expressionService.executeExpression(postProcessor, evaluationContext);
        } catch (Exception ex) {
            coreResult.setMsg(ex.toString());
            log.error(ex.getMessage(), ex);
        }
        return coreResult;
    }

    @Transactional
    public int updateOrInsertByCoreEntity(TransferEntity transferEntity) {

        if (directForward) return 1;

        String method = (String) dbUpdate.get("method");

        switch (method) {
            case "no_store":
                return 1;
            case "auto": {
                // 正常操作
                @SuppressWarnings("unchecked")
                Map<String, Object> insert = (Map<String, Object>) dbUpdate.get("insert");
                @SuppressWarnings("unchecked")
                Map<String, Object> update = (Map<String, Object>) dbUpdate.get("update");
                @SuppressWarnings("unchecked")
                Map<String, Object> select = (Map<String, Object>) dbUpdate.get("select");
                String insertSql = (String) insert.get("sql");
                String updateSql = (String) update.get("sql");
                String selectSql = (String) select.get("sql");
                StandardEvaluationContext context = expressionService.evaluationContext();
                context.setRootObject(transferEntity.getData());
                Map<String, Object> originalData;

                boolean autoId = (boolean) insert.getOrDefault("auto_id", false);
                @SuppressWarnings({"unchecked"})
                String[] updateQualifiedField = ((List<String>) update.get("qualified_field")).toArray(new String[0]);

                // 从核心库中查询旧数据
                if (!StringUtils.isEmpty(selectSql)) {
                    selectSql = expressionService.executeExpression(selectSql, context, String.class, true);
                    originalData = coreTableDao.commonSelect(selectSql);
                } else {
                    @SuppressWarnings("unchecked")
                    String[] selectQualifiedField = ((List<String>) select.get("qualified_field")).toArray(new String[0]);
                    originalData = autoConfigurableCoreTableDao.findOriginal(transferEntity, false, selectQualifiedField);
                }

                // 如果未查询到旧数据, 则进行插入操作
                if (originalData == null) {
                    log.debug("插入: {}", CommonUtils.toJSONString(transferEntity));
                    if (!StringUtils.isEmpty(insertSql)) {
                        insertSql = expressionService.executeExpression(insertSql, context, String.class, true);
                        return coreTableDao.commonInsert(insertSql);
                    }
                    return autoConfigurableCoreTableDao.insert(transferEntity);
                }

                // 查询到旧数据, 进行更新操作
                if (!StringUtils.isEmpty(updateSql)) {
                    updateSql = expressionService.executeExpression(updateSql, context, String.class, true);
                    return coreTableDao.commonUpdate(updateSql);
                }
                TransferEntity original = TransferEntity.builder()
                        .data(originalData)
                        .opType(transferEntity.getOpType())
                        .source(transferEntity.getSource())
                        .build();
                return autoConfigurableCoreTableDao.update(transferEntity, original, true, updateQualifiedField);
            }
            case "custom": {
                // TODO 自定义数据入库方式
                break;
            }
        }
        return 0;
    }

    /**
     * 转换并存储
     */
    public CoreResult<TransferEntity> convertAndStore(SourceEntity sourceEntity) {
        CoreResult<TransferEntity> coreResult = convertToCoreEntity(sourceEntity);
        TransferEntity transferEntity = coreResult.getData();
        if (transferEntity != null) {
            if (updateOrInsertByCoreEntity(transferEntity) > 0)
                log.info("convert result, table: {}, msg: {}, data: {}", transferEntity.getSource(), coreResult.getMsg(), CommonUtils.toJSONString(transferEntity));
            else
                log.info("store failed");
        } else {
            log.info("convert result is null, msg: {}", coreResult.getMsg());
        }
        return coreResult;
    }

}
