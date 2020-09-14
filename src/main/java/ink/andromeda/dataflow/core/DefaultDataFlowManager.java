package ink.andromeda.dataflow.core;

import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DefaultDataFlowManager extends ConfigurableDataFlowManager {

    public static final String CONFIG_COLLECTION_NAME = "data_flow_config";

    private final MongoTemplate mongoTemplate;

    public DefaultDataFlowManager(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    protected List<Map<String, Object>> getFlowConfig() {
        return mongoTemplate.findAll(Document.class, CONFIG_COLLECTION_NAME).stream()
                .map(document -> (Map<String, Object>)document)
                .collect(Collectors.toList());
    }

    @Override
    protected List<Map<String, Object>> getFlowConfig(String source, String schema, String name) {
        return null;
    }

    @Override
    protected Map<String, Object> getFlowConfig(String source, String schema, String name, String flowName) {
        return null;
    }

    @Override
    protected int addFlowConfig(String source, String schema, String name, List<Map<String, Object>> configs) {
        return 0;
    }

    @Override
    protected int addFlowConfig(String source, String schema, String name, String flowName, Map<String, Object> config) {
        return 0;
    }

    @Override
    protected int updateFlowConfig(String source, String schema, String name, String flowName, Map<String, Object> update) {
        return 0;
    }

    @Override
    protected int deleteFlowConfig(String source, String schema, String name) {
        return 0;
    }

    @Override
    protected int deleteFlowConfig(String source, String schema, String name, String flowName) {
        return 0;
    }
}