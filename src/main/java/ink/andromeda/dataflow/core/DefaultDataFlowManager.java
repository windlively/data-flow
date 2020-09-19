package ink.andromeda.dataflow.core;

import ink.andromeda.dataflow.core.converter.configuarion.SpringELConfigurationResolver;
import ink.andromeda.dataflow.util.ConfigValidationException;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.Assert;

import java.util.*;
import java.util.stream.Collectors;

import static ink.andromeda.dataflow.util.GeneralTools.checkNotEmpty;

@Slf4j
public class DefaultDataFlowManager extends ConfigurableDataFlowManager {

    public static final String FLOW_COLLECTION_NAME = "data_flow_list";

    private final MongoTemplate mongoTemplate;

    private final RedisTemplate<String, String> redisTemplate;

    public DefaultDataFlowManager(MongoTemplate mongoTemplate,
                                  RedisTemplate<String, String> redisTemplate,
                                  Registry<SpringELConfigurationResolver> convertResolverRegistry,
                                  Registry<SpringELConfigurationResolver> exportResolverRegistry) {
        this.mongoTemplate = mongoTemplate;
        this.redisTemplate = redisTemplate;
        super.convertResolverRegistrySupplier = () -> convertResolverRegistry;
        super.exportResolverRegistrySupplier = () -> exportResolverRegistry;
    }

    @Override
    public List<Map<String, Object>> getFlowConfig() {
        return mongoTemplate.findAll(Document.class, FLOW_COLLECTION_NAME).stream()
                .map(document -> (Map<String, Object>) document)
                .collect(Collectors.toList());
    }

    @Override
    public List<Map<String, Object>> getFlowConfig(String source, String schema, String name) {
        checkNotEmpty(source, "source");
        checkNotEmpty(schema, "schema");
        checkNotEmpty(name, "name");
        return mongoTemplate.find(Query.query(Criteria.where("source").is(source))
                        .addCriteria(Criteria.where("schema").is(schema))
                        .addCriteria(Criteria.where("name").is(name)),
                Document.class, FLOW_COLLECTION_NAME)
                .stream()
                .map(document -> (Map<String, Object>) document)
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> getFlowConfig(String flowName) {
        return mongoTemplate.findOne(Query.query(Criteria.where("_id").is(flowName)), Document.class, FLOW_COLLECTION_NAME);
    }

    @Override
    protected int addFlowConfig(List<Map<String, Object>> configs) {
        /*
            // 非原子性操作
            int c = 0;
            for (Map<String, Object> config : configs)
                c += addFlowConfig(config);
            return c;
        */
        Assert.notEmpty(configs, "config list is empty");
        configs.forEach(this::checkInsertFlowConfig);
        return mongoTemplate.insertAll(configs).size();

    }

    @Override
    protected int addFlowConfig(Map<String, Object> config) {
        checkInsertFlowConfig(config);
        return mongoTemplate.insert(config, FLOW_COLLECTION_NAME).size();
    }

    private void checkInsertFlowConfig(Map<String ,Object> flowConfig){
        validateFlowConfig(flowConfig);
        String flowName = (String) flowConfig.get("_id");
        if(mongoTemplate.find(Query.query(Criteria.where("_id").is(flowName)), Document.class).size() > 0){
            throw new IllegalArgumentException(String.format(
                    "flow '%s' already exists", flowName
            ));
        }
    }

    @Override
    public void validateFlowConfig(Map<String, Object> flowConfig) throws ConfigValidationException {
        try {
            super.validateFlowConfig(flowConfig);
        } catch (ConfigValidationException ex) {
            log.error("error config: \n {}",
                    ex.getErrorInfo()
                            .entrySet()
                            .stream()
                            .map(e -> e.getKey() + ": " + e.getValue())
                            .collect(Collectors.joining()));
            throw ex;
        }
    }

    @Override
    protected int updateFlowConfig(String flowName, Map<String, Object> update) {
        Document query = new Document();
        query.put("_id", flowName);

        if(mongoTemplate.getCollection(FLOW_COLLECTION_NAME).countDocuments(query) < 1){
            throw new IllegalArgumentException(String.format("flow '%s' not exists", flowName));
        }

        return (int) mongoTemplate.getCollection(FLOW_COLLECTION_NAME)
                .replaceOne(query,  new Document(update)).getModifiedCount();
    }

    @Override
    protected int deleteFlowConfig(String source, String schema, String name) {
        Document query = new Document();
        query.put("schema", schema);
        query.put("source", source);
        query.put("name", name);
        return (int) mongoTemplate.getCollection(FLOW_COLLECTION_NAME)
                .deleteMany(query).getDeletedCount();
    }

    @Override
    protected int deleteFlowConfig(String flowName) {
        return (int) mongoTemplate.remove(Query.query(Criteria.where("_id").is(flowName)), FLOW_COLLECTION_NAME)
                .getDeletedCount();
    }

    @Override
    protected int addNodeConfig(String flowName, Map<String, Object> nodeConfig) {
        Map<String, Object> flowConfig = getFlowConfig(flowName);
        Assert.notNull(flowConfig, String.format("flow '%s' not exist", flowName));

        Object nodes = flowConfig.get("execution_chain");
        if(nodes == null){
            nodes = new LinkedList<Map<String, Object>>();
        }

        Assert.state(nodes instanceof List, "execution_chain isn't a list type");

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> executionChainList = (List<Map<String, Object>>) nodes;

        executionChainList.add(nodeConfig);

        flowConfig.put("execution_chain", executionChainList);

        return updateFlowConfig(flowName, flowConfig);
    }

    @Override
    protected int updateNodeConfig(String flowName, String nodeName, Map<String, Object> update) {
        Map<String, Object> flowConfig = getFlowConfig(flowName);
        Assert.notNull(flowConfig, String.format("flow '%s' not exist", flowName));
        Object nodes = Optional.ofNullable(flowConfig.get("execution_chain"))
                .orElse(Collections.<Map<String, Object>>emptyList());

        Assert.state(nodes instanceof List, "execution_chain isn't a list type");

        @SuppressWarnings("unchecked")
        Map<String, Object> nodeConfig = ((List<Map<String, Object>>) nodes).stream()
                .filter(e -> Objects.equals(nodeName, e.get("node_name")))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(String.format(
                        "flow '%s' has no node '%s'", flowName, nodeName
                )));
        nodeConfig.clear();
        nodeConfig.putAll(update);
        return updateFlowConfig(flowName, flowConfig);
    }

    @Override
    protected int deleteNodeConfig(String flowName, String nodeName) {
        Map<String, Object> flowConfig = getFlowConfig(flowName);
        Assert.notNull(flowConfig, String.format("flow '%s' not exist", flowName));
        Object nodes = Optional.ofNullable(flowConfig.get("execution_chain"))
                .orElse(Collections.<Map<String, Object>>emptyList());

        Assert.state(nodes instanceof List, "execution_chain isn't a list type");

        //noinspection unchecked
        return ((List<Map<String, Object>>) nodes)
                .removeIf(m -> Objects.equals(nodeName, m.get("node_name"))) ? 1 : 0;
    }

    @Override
    public Map<String, Object> getNodeConfig(String flowName, String nodeName) {
        return super.getNodeConfig(flowName, nodeName);
    }

    public void reload(boolean cluster){
        if(cluster)
            redisTemplate.convertAndSend("", "");
        else
            reload();
    }
}