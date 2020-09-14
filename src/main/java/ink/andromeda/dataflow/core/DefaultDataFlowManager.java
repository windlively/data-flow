package ink.andromeda.dataflow.core;

import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.List;
import java.util.Map;

public class DefaultDataFlowManager extends AbstractDataFlowManager {

    public static final String CONFIG_COLLECTION_NAME = "";

    private final MongoTemplate mongoTemplate;

    public DefaultDataFlowManager(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    protected List<Map<String, Object>> getFlowConfig() {
        List<Document> documents = mongoTemplate.findAll(Document.class, CONFIG_COLLECTION_NAME);
        return null;
    }

    @Override
    protected List<Map<String, Object>> getFlowConfig(String source, String schema, String name) {
        return null;
    }

    @Override
    protected Map<String, Object> getFlowConfig(String source, String schema, String name, String flowName) {
        return null;
    }
}