package ink.andromeda.dataflow.core.node.resolver;

import ink.andromeda.dataflow.core.SourceEntity;
import ink.andromeda.dataflow.core.SpringELExpressionService;
import ink.andromeda.dataflow.core.TransferEntity;
import ink.andromeda.dataflow.datasource.dao.CommonJdbcDao;

import java.util.Map;

public class ExportToRDBResolver extends DefaultConfigurationResolver{

    public ExportToRDBResolver(SpringELExpressionService expressionService,
                               CommonJdbcDao commonJdbcDao) {
        super(expressionService);
    }

    @Override
    public String getName() {
        return "export_to_rdb";
    }

    @Override
    public void resolve(SourceEntity source, TransferEntity input, TransferEntity target, Object config, Map<String, Object> rootData) throws Exception {

    }
}
