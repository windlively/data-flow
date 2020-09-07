package ink.andromeda.dataflow.service;

import ink.andromeda.dataflow.datasource.dao.CommonDao;
import ink.andromeda.dataflow.datasource.dao.CoreTableDao;
import ink.andromeda.dataflow.datasource.mapper.HaierOracleMapper;
import ink.andromeda.dataflow.entity.AppEventSubject;
import ink.andromeda.dataflow.entity.CoreResult;
import ink.andromeda.dataflow.entity.HistoryDataConsumeInfo;
import ink.andromeda.dataflow.service.event.EventMessage;
import ink.andromeda.dataflow.service.event.ProductizationEventService;
import ink.andromeda.dataflow.service.sync.CoreTableSyncService;
import lombok.extern.slf4j.Slf4j;
import ink.andromeda.dataflow.configuration.RedisConfiguration;
import ink.andromeda.dataflow.entity.SourceEntity;
import ink.andromeda.dataflow.entity.CoreEntity;
import org.bson.Document;
import org.slf4j.MDC;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static ink.andromeda.dataflow.configuration.RocketMQConfiguration.HIS_SYNC_BATCH_TAG;
import static ink.andromeda.dataflow.configuration.RocketMQConfiguration.TOPIC_NAME;

@Service
@Slf4j
public class HistoricalDataService {

    private final CoreTableSyncService coreTableSyncService;

    private final ProductizationEventService productizationEventService;

    private final CommonDao commonDao;

    private final CoreTableDao coreTableDao;

    private final MongoTemplate mongoTemplate;

    private final RedisClient redisClient;

    private final HaierOracleMapper haierOracleMapper;

    private final Producer producer;

    private final Consumer consumer;

    private final ApplicationEventService applicationEventService;

    // 批量同步详情
    public final static String BATCH_SYNC_KEY_PREFIX = RedisConfiguration.AppKeyPrefix + "batch-sync:";

    // 批量同步概览
    public final static String BATCH_SYNC_META_KEY_PREFIX = RedisConfiguration.AppKeyPrefix + "batch-sync-meta:";

    public HistoricalDataService(CoreTableSyncService coreTableSyncService,
                                 ProductizationEventService productizationEventService,
                                 CommonDao commonDao,
                                 CoreTableDao coreTableDao,
                                 MongoTemplate mongoTemplate,
                                 RedisClient redisClient,
                                 HaierOracleMapper haierOracleMapper,
                                 Producer producer, Consumer consumer,
                                 ApplicationEventService applicationEventService) {
        this.coreTableSyncService = coreTableSyncService;
        this.productizationEventService = productizationEventService;
        this.commonDao = commonDao;
        this.coreTableDao = coreTableDao;
        this.mongoTemplate = mongoTemplate;
        this.redisClient = redisClient;
        this.haierOracleMapper = haierOracleMapper;
        this.producer = producer;
        this.consumer = consumer;
        this.applicationEventService = applicationEventService;
    }

    @PostConstruct
    public void init() {
        Stream.of(AppEventSubject.HIS_BATCH_ALL_FIN, AppEventSubject.HIS_BATCH_ONE_FIN, AppEventSubject.HIS_EVENT_FIN, AppEventSubject.HIS_SYNC_FIN).forEach(e ->
                applicationEventService.subscribe(e, "batch-sync-monitor", msg -> {
                    try{
                        JSONObject message = (JSONObject) JSON.toJSON(msg);
                        String batchNo = message.getString("batchNo");
                        message.put("msgType", e.name());
                        message.put("progress", redisClient.hgetAll(BATCH_SYNC_META_KEY_PREFIX + batchNo));
                        redisClient.rpush(BATCH_SYNC_KEY_PREFIX + batchNo, toJSONString(message));
                        redisClient.expire(BATCH_SYNC_KEY_PREFIX + batchNo, 3600 * 24);
                        redisClient.publish(BATCH_SYNC_KEY_PREFIX + batchNo, toJSONString(message));
                    }catch (Exception ex){
                        log.error("application event service exception: {}", ex.getMessage(), ex);
                    }

                }));
        consumer.subscribe(TOPIC_NAME, HIS_SYNC_BATCH_TAG, (message, context) -> {
            try {
                BatchSyncTaskInfo batchSyncTaskInfo = JSON.parseObject(new String(message.getBody(), StandardCharsets.UTF_8), BatchSyncTaskInfo.class);

                log.info("receive history data batch sync task, partition: {}, batchNo: {}, count: {}",
                        batchSyncTaskInfo.partition, batchSyncTaskInfo.batchNo, batchSyncTaskInfo.getOrderNoList().size());
                syncHistoricalData(batchSyncTaskInfo);
                log.info("finish process history data batch sync task, partition: {}, batchNo: {}",
                        batchSyncTaskInfo.partition, batchSyncTaskInfo.batchNo);
            } catch (Exception ex) {
                log.error("history data batch sync exception: {}", ex.getMessage(), ex);
            }
            return Action.CommitMessage;
        });
        if (!consumer.isStarted()) {
            consumer.start();
        }

    }

    public String syncHistoricalData(String channelOrderNo, String source, boolean pushEvent, String type) {
        return syncHistoricalData(Collections.singletonList(channelOrderNo), source, pushEvent, type);
    }

    public String syncHistoricalData(BatchSyncTaskInfo batchSyncTaskInfo) {
        List<String> channelOrderNos = batchSyncTaskInfo.getOrderNoList();
        String batchNo = batchSyncTaskInfo.getBatchNo();
        String source = batchSyncTaskInfo.getSource();
        boolean pushEvent = batchSyncTaskInfo.isPushEvent();
        String type = batchSyncTaskInfo.getType();
        String keyPrefix = BATCH_SYNC_META_KEY_PREFIX + batchNo;
        Document query = new Document();
        query.put("_id", source);
        Document document = mongoTemplate.getCollection("history_data_config").find(query).first();
        if (document == null)
            throw new IllegalArgumentException("unknown source: " + source);
        JSONObject config = JSON.parseObject(document.toJson());
        String dataSource = config.getString("data_source");
        JSONObject typeListMap = config.getJSONObject("re_sync_type_list");
        JSONArray typeList;

        if (typeListMap == null || (typeList = typeListMap.getJSONArray(type)) == null || typeList.isEmpty()) {
            throw new IllegalArgumentException("type '" + type + "' has no history config");
        }

        JSONObject findHistoryConfig = config.getJSONObject("find_history_config");

        List<JSONObject> historyList = typeList.stream()
                .map(t -> findHistoryConfig.getJSONObject((String) t)).collect(Collectors.toList());
        long currentTotal = channelOrderNos.size();
        // 单个任务订单量
        int partitionSize = 1000;

        if (currentTotal > partitionSize) {

            Map<String, String> batchMetaInfo = new HashMap<>();

            // 分区数量
            int totalPartition = (int) (currentTotal % partitionSize == 0 ?
                    currentTotal / partitionSize : ((currentTotal / partitionSize) + 1));
//
//            batchMetaInfo.put("batchNo", batchNo);
//            batchMetaInfo.put("current", "0");
//            batchMetaInfo.put("finishedPartition", "0");
//            batchMetaInfo.put("total", currentTotal + "");
//            batchMetaInfo.put("totalPartition", totalPartition + "");
//            batchMetaInfo.put("finished", "false");
//            redisClient.hset(keyPrefix, batchMetaInfo);
//            redisClient.expire(keyPrefix, 3600 * 24);
            // 任务划分
            for (int i = 0; i < totalPartition; i++) {

                int fromIndex = i * partitionSize;
                int toIndex = (i + 1) * partitionSize;
                if (toIndex > currentTotal) toIndex = (int) currentTotal;

                BatchSyncTaskInfo subTask = BatchSyncTaskInfo.builder()
                        .batchNo(batchNo)
                        .totalCount(currentTotal)
                        .totalPartition(totalPartition)
                        .orderNoList(channelOrderNos.subList(fromIndex, toIndex))
                        .partition(i)
                        .source(source)
                        .type(type)
                        .pushEvent(pushEvent)
                        .build();

                Message message = new Message();
                message.setTopic(TOPIC_NAME);
                message.setTag(HIS_SYNC_BATCH_TAG);
                message.setBody(toJSONString(subTask).getBytes(StandardCharsets.UTF_8));
                message.setKey(batchNo);

                SendResult sendResult = producer.send(message);

                log.info("send history data batch sync task, {}, partition: {}, batchNo: {}", sendResult, i, batchNo);
            }

        } else {
//            if (!redisClient.exists(keyPrefix)) {
//                Map<String, String> batchMetaInfo = new HashMap<>();
//                batchMetaInfo.put("batchNo", batchNo);
//                batchMetaInfo.put("current", "0");
//                batchMetaInfo.put("finishedPartition", "0");
//                batchMetaInfo.put("total", batchSyncTaskInfo.totalCount + "");
//                batchMetaInfo.put("totalPartition", batchSyncTaskInfo.totalPartition + "");
//                batchMetaInfo.put("finished", "false");
//                redisClient.hset(keyPrefix, batchMetaInfo);
//                redisClient.expire(keyPrefix, 3600 * 24);
//            }
            ThreadPoolService.ASYNC_TASK_GROUP().execute(() -> {
                try {
                    Date batchStart = batchSyncTaskInfo.getStartTime();
                    channelOrderNos.forEach(channelOrderNo -> {

                        Date start = batchSyncTaskInfo.getStartTime();
                        AtomicInteger i = new AtomicInteger();
                        historyList.forEach(hc -> {
                            MDC.put("traceId", randomId());
                            String dataSourceName;
                            if ((dataSourceName = hc.getString("data_source")) == null) {
                                dataSourceName = dataSource;
                            }
                            int c = syncHistoricalData(hc, channelOrderNo, dataSourceName, source, pushEvent, batchNo);
                            i.addAndGet(c);
                            MDC.remove("traceId");
                        });
                        coreTableDao.insertHistoryDataConsumeInfo(HistoryDataConsumeInfo.builder()
                                .batchNo(batchNo)
                                .processCount(i.get())
                                .orderNo(channelOrderNo)
                                .build());
//                        long current = redisClient.hincrBy(keyPrefix, "current", 1);
                        // redisClient.expire(keyPrefix, 3600 * 24);
//                        applicationEventService.next(AppEventSubject.HIS_BATCH_ONE_FIN, BatchSyncResult.builder()
//                                .batchNo(batchNo)
//                                .source(source)
//                                .total(batchSyncTaskInfo.totalCount)
//                                .current(current)
//                                .startTime(start)
//                                .endTime(new Date())
//                                .orderNo(channelOrderNo)
//                                .build(), true, true);

                    });

//                    if (redisClient.hincrBy(keyPrefix, "finishedPartition", 1)
//                            == batchSyncTaskInfo.totalPartition) {
//                        redisClient.hset(keyPrefix, "finished", "true");
//                        redisClient.expire(keyPrefix, 3600 * 24);
//                        log.info("finished partition {}, batchNo: {}", batchSyncTaskInfo.getPartition(), batchNo);
//                        applicationEventService.next(HIS_BATCH_ALL_FIN, BatchSyncResult.builder()
//                                .batchNo(batchNo)
//                                .source(source)
//                                .startTime(batchStart)
//                                .endTime(new Date())
//                                .total(batchSyncTaskInfo.totalCount)
//                                .build(), true, true);
//                    }
                }catch (Throwable ex){
                    log.error("error occur in batch sync task, partition {}, batchNo: {}", batchSyncTaskInfo.getPartition(), batchNo, ex);
                }
            });
        }


        return batchNo;

    }

    public String syncHistoricalData(List<String> channelOrderNos, String batchNo, String source, boolean pushEvent, String type) {
        return syncHistoricalData(
                BatchSyncTaskInfo.builder()
                        .orderNoList(channelOrderNos)
                        .batchNo(batchNo)
                        .source(source)
                        .partition(1)
                        .totalPartition(1)
                        .pushEvent(pushEvent)
                        .type(type)
                        .totalCount(channelOrderNos.size())
                        .startTime(new Date())
                        .build()
        );
    }


    public String syncHistoricalData(List<String> channelOrderNos, String source, boolean pushEvent, String type) {
        return syncHistoricalData(channelOrderNos, randomId(), source, pushEvent, type);
    }

    private int syncHistoricalData(JSONObject configItem, String orderNo, String dataSourceName, String source, boolean pushEvent, String batchNo) {
        String sourceTable = configItem.getString("source_table");
        String sourceSchema = configItem.getString("source_schema");
        String selectSql = configItem.getString("select_sql");
        selectSql = String.format(selectSql, orderNo);
        boolean isList = configItem.getBooleanValue("is_list");
        String idField = configItem.getString("id_field");
        @SuppressWarnings("unchecked") List<JSONObject> sourceDataList =
                (List<JSONObject>) commonDao.select(selectSql, dataSourceName, "list");
        if (sourceDataList.isEmpty()) {
            log.error("source: {}, channelOrderNo: {}, sql: {} result is empty!", source, orderNo, selectSql);
//            applicationEventService.next(HIS_SYNC_FIN, SyncResult.builder()
//                    .success(false)
//                    .batchNo(batchNo)
//                    .orderNo(orderNo)
//                    .msg(String.format("Empty query results, source: %s, orderNo: %s, sql: [%s]",
//                            source, orderNo, selectSql))
//                    .schemaName(sourceSchema)
//                    .tableName(sourceTable)
//                    .originalId("-2")
//                    .coreTable("undefined")
//                    .build(), true, true);
            return 0;
        }
        if (!isList && sourceDataList.size() > 1) {
            // 查询到多条记录
            log.error("source: {}, channelOrderNo: {}, sql: {} has multi result", source, orderNo, selectSql);
            // 终止此条数据的同步
//            applicationEventService.next(HIS_SYNC_FIN, SyncResult.builder()
//                    .success(false)
//                    .batchNo(batchNo)
//                    .orderNo(orderNo)
//                    .msg(String.format("The expected value is of type map, but the actual value is of type list, sql: [%s], result length: %s",
//                            selectSql, sourceDataList.size()))
//                    .schemaName(sourceSchema)
//                    .tableName(sourceTable)
//                    .originalId("-2")
//                    .coreTable("undefined")
//                    .build(), true, true);
            return 0;
        }

        AtomicInteger i = new AtomicInteger();

        sourceDataList.forEach(sourceData -> {
            SourceEntity sourceEntity = SourceEntity.builder()
                    .opType("MANUAL")
                    .table(sourceTable)
                    .schema(sourceSchema)
                    .data(sourceData)
                    .build();
            // 获取该条记录在原始表中的主键值
            String originalId = null;
            if (!(StringUtils.isNotEmpty(idField)
                    && (originalId = sourceEntity.getData().getString(idField)) != null))
                for (String s : new String[]{"id", "ID", "Id"}) {
                    if ((originalId = sourceEntity.getData().getString(s)) != null)
                        break;
                }
            coreTableHistoricalDataSync(sourceEntity, originalId, orderNo, batchNo, pushEvent);
            i.incrementAndGet();
        });
        return i.get();
    }

    /**
     * 历史数据同步
     *
     * @param sourceEntity 数据
     * @param pushEvent      是否推送事件
     */
    public void coreTableHistoricalDataSync(SourceEntity sourceEntity, String originalId, String orderNo, String batchNo, boolean pushEvent) {
        String schemaName = sourceEntity.getSchema();
        String tableName = sourceEntity.getTable();
        if (originalId == null)
            originalId = "-1";
        String finalOriginalId = originalId;
        coreTableSyncService.getConverter(schemaName, tableName).forEach((id, converter) -> {
            CoreEntity coreEntity = null;
            // 数据同步
            boolean syncSuccess = false;
            String syncErrorMsg = null;
            try {
                CoreResult<CoreEntity> convertCoreResult = converter.convertAndStore(sourceEntity);
                coreEntity = convertCoreResult.getData();
                if (coreEntity == null) {
                    syncErrorMsg = convertCoreResult.getMsg();
                    return;
                }
                syncSuccess = true;
//                applicationEventService.next(AppEventSubject.SYNC_SUCCESS, businessEntity);
            } catch (Exception e) {
                syncErrorMsg = e.toString();
                log.error(e.getMessage(), e);
                e.printStackTrace();
            } finally {
//                applicationEventService.next(HIS_SYNC_FIN, SyncResult.builder()
//                        .success(syncSuccess)
//                        .batchNo(batchNo)
//                        .orderNo(orderNo)
//                        .msg(syncErrorMsg)
//                        .schemaName(businessEntity.getSchemaName())
//                        .tableName(businessEntity.getTableName())
//                        .originalId(finalOriginalId)
//                        .coreTable(converter.getDestTable())
//                        .build(), true, true);
            }

            // 事件同步
            if (syncSuccess && pushEvent) {
                try {
                    List<EventMessage> list = productizationEventService.inferEvent(sourceEntity, coreEntity);
//                    list.forEach(e -> applicationEventService.next(HIS_EVENT_FIN, EventResult.builder()
//                            .success(e.isSuccess())
//                            .batchNo(batchNo)
//                            .schemaName(businessEntity.getSchemaName())
//                            .orderNo(orderNo)
//                            .originalId(finalOriginalId)
//                            .coreTable(converter.getDestTable())
//                            .tableName(businessEntity.getTableName())
//                            .eventKey(e.getEventName())
//                            .eventName(e.getDescription())
//                            .msg(e.getMsg())
//                            .build(), true, true));
//                    if (list.stream().anyMatch(EventMessage::isSuccess))
//                        applicationEventService.next(EVENT_MATCHED, businessEntity);

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
//                    applicationEventService.next(HIS_EVENT_FIN, EventResult.builder()
//                            .success(false)
//                            .batchNo(batchNo)
//                            .orderNo(orderNo)
//                            .originalId(finalOriginalId)
//                            .schemaName(businessEntity.getSchemaName())
//                            .coreTable(converter.getDestTable())
//                            .tableName(businessEntity.getTableName())
//                            .msg(e.toString())
//                            .build(), true, true);
                }
            }
        });
    }

    public List<String> findSupportHistoryConfigTableNames(String source) {
        List<String> result = new ArrayList<>();
        //noinspection unchecked
        mongoTemplate.executeQuery(new Query(Criteria.where("_id").is(source)),
                "history_data_config", document -> result.addAll(document.get("re_sync_type_list", Map.class).keySet()));
        return result;
    }


    public void haierSyncHistoricalData(JSONObject data) {
        data.remove("row_num");
        data.remove("ROW_NUM");
        coreTableSyncService.coreTableSync(SourceEntity.builder()
                .data(data)
                .before(new JSONObject())
                .opType("MANUAL")
                .table("lm_loan")
                .schema("abak")
                .build());
        String loanNo = data.getString("LOAN_NO");

        haierOracleMapper.selectLmPmShdByLoanNo(loanNo).forEach(
                s -> coreTableSyncService.coreTableSync(
                        SourceEntity.builder()
                                .opType("MANUAL")
                                .schema("abak")
                                .table("lm_pm_shd")
                                .before(new JSONObject())
                                .data(s)
                                .build()
                )
        );

        haierOracleMapper.selectLmSetlmtLogByLoanNo(loanNo).forEach(
                s -> coreTableSyncService.coreTableSync(
                        SourceEntity.builder()
                                .opType("MANUAL")
                                .schema("abak")
                                .table("lm_setlmt_log")
                                .before(new JSONObject())
                                .data(s)
                                .build()
                )
        );

        haierOracleMapper.selectLmPmLogByLoanNo(loanNo).forEach(
                s -> coreTableSyncService.coreTableSync(
                        SourceEntity.builder()
                                .opType("MANUAL")
                                .schema("abak")
                                .table("lm_pm_log")
                                .before(new JSONObject())
                                .data(s)
                                .build()
                )
        );

    }

    public String haierFullHistoricalDataSync(int pageSize, String key, int limit) {

        if (!Objects.equals(key, "58603924715"))
            throw new IllegalArgumentException("key error, no permission");
        AtomicLong count = new AtomicLong(haierOracleMapper.selectLmLoanTotalCount());
        if (limit > 0) count.set(limit);
        if (redisClient.incr("HAIER_FULL_SYNC") != 1) {
            throw new IllegalStateException("haier full sync task is in running");
        }
        redisClient.hset("HAIER_FULL_SYNC_STATUS", "total", "" + count.get());
        redisClient.hset("HAIER_FULL_SYNC_STATUS", "page", "1");
        redisClient.hset("HAIER_FULL_SYNC_STATUS", "pageSize", pageSize + "");
        redisClient.hset("HAIER_FULL_SYNC_STATUS", "done", "false");
        ThreadPoolService.ASYNC_TASK_GROUP().submit(() -> {
            AtomicInteger processCount = new AtomicInteger();
            List<JSONObject> orders = Collections.emptyList();
            int page = 1;
            try {
                while (!(orders = haierOracleMapper.selectLmLoanWithPageable(page++, pageSize)).isEmpty()) {
                    for (JSONObject o : orders) {
                        haierSyncHistoricalData(o);
                        processCount.incrementAndGet();
                        redisClient.hset("HAIER_FULL_SYNC_STATUS", "current", "" + processCount.get());

                        if (limit > 0 && processCount.get() >= limit) {
                            log.info("haier full sync stop by limit {}, current: {}, page: {}, total: {}",
                                    limit, processCount.get(), page, count.get());
                            return;
                        }
                    }
                    if (limit <= 0) {
                        count.set(haierOracleMapper.selectLmLoanTotalCount());
                        redisClient.hset("HAIER_FULL_SYNC_STATUS", "total", count.get() + "");
                    }
                    redisClient.hset("HAIER_FULL_SYNC_STATUS", "page", page + "");
                    log.info("haier full sync status, total: {}, current: {}, page: {}", count.get(), processCount.get(), page);
                }
                log.info("haier full sync finished, limit: {}, current: {}, page: {}, total, {}, last order size: {}",
                        limit, processCount.get(), page, count.get(), orders.size());
            } catch (Throwable ex) {
                log.error(ex.getMessage(), ex);
            } finally {
                redisClient.hset("HAIER_FULL_SYNC_STATUS", "done", "true");
                redisClient.del("HAIER_FULL_SYNC");
                log.info("(finally)haier full sync finished, limit: {}, current: {}, page: {}, total, {}, last order size: {}",
                        limit, processCount.get(), page, count.get(), orders.size());
            }
        });

        return null;
    }

    public static void main(String[] args) throws IOException {
        List<String> all = new ArrayList<>();
        List<String> success = new ArrayList<>();
        Scanner scanner = new Scanner(new FileInputStream("/Users/andromeda/Downloads/360.txt"));
        Scanner successScanner = new Scanner(new FileInputStream("/Users/andromeda/Desktop/history_data_consume_info.tsv"));
        while (scanner.hasNextLine()){
            String str = scanner.nextLine().trim();
            all.add(str);
        }
        while (successScanner.hasNextLine()){
            String str = successScanner.nextLine().trim();
            success.add(str);
        }

        File file = new File("/Users/andromeda/Desktop/failed.tsv");
        FileOutputStream outputStream = new FileOutputStream(file, false);
        all.removeAll(success);

        all.forEach(o -> {
            try {
                outputStream.write((o + "\n").getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        outputStream.close();
    }
}
