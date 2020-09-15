package ink.andromeda.dataflow.core;

import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static ink.andromeda.dataflow.util.GeneralTools.checkNotEmpty;

public class DefaultDataFlowManager extends ConfigurableDataFlowManager {

    public static final String FLOW_COLLECTION_NAME = "data_flow_list";

    private final MongoTemplate mongoTemplate;

    public DefaultDataFlowManager(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
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
    protected int addFlowConfig(String source, String schema, String name, List<Map<String, Object>> configs) {
        configs.forEach(flowConfig -> {
            
        });
        return 0;


    }

    @Override
    protected int addFlowConfig(String flowName, Map<String, Object> config) {
        return 0;
    }

    @Override
    protected int updateFlowConfig(String flowName, Map<String, Object> update) {
        return 0;
    }

    @Override
    protected int deleteFlowConfig(String source, String schema, String name) {
        return 0;
    }

    @Override
    protected int deleteFlowConfig(String flowName) {
        return 0;
    }

    @Override
    protected int addNodeConfig(String flowName, String nodeName, Map<String, Object> nodeConfig) {
        return 0;
    }

    @Override
    protected int updateNodeConfig(String flowName, String nodeName, Map<String, Object> update) {
        return 0;
    }

    @Override
    protected int deleteNodeConfig(String flowName, String nodeName) {
        return 0;
    }

    @Override
    public Map<String, Object> getNodeConfig(String flowName, String nodeName) {
        return super.getNodeConfig(flowName, nodeName);
    }

    /**
     * 检查必要配置字段
     * @param flowConfig 输入的flow配置
     */
    private void checkFlowConfig(Map<String, Object> flowConfig){
        checkNotEmpty((String) flowConfig.get("schema"), "schema");
        checkNotEmpty((String) flowConfig.get("source"), "source");
        checkNotEmpty((String) flowConfig.get("name"), "name");
        checkNotEmpty((String) flowConfig.get("_id"), "_id(flow_name)");

    }
}