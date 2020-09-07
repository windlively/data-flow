package ink.andromeda.dataflow.datasource.dao;

import ink.andromeda.dataflow.service.event.ProductizationConfigService;
import lombok.extern.slf4j.Slf4j;
import net.abakus.coresystem.dynamicdatasource.DynamicDataSource;
import net.abakus.coresystem.entity.TableField;
import net.abakus.coresystem.entity.po.ProdDatasourceConf;
import net.abakus.coresystem.entity.po.ProdLinkRelationConf;
import net.abakus.coresystem.mapper.ProdDatasourceConfMapper;
import net.abakus.coresystem.mapper.ProdLinkRelationConfMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 产品化配置相关操作
 */
@Repository
@Slf4j
public class ProductizationConfigDao {

    private final ProdDatasourceConfMapper datasourceConfMapper;

    private final ProdLinkRelationConfMapper linkRelationConfMapper;

    private final DynamicDataSource dynamicDataSource;

    public ProductizationConfigDao(ProdDatasourceConfMapper datasourceConfMapper,
                                   ProdLinkRelationConfMapper linkRelationConfMapper,
                                   DynamicDataSource dynamicDataSource) {
        this.datasourceConfMapper = datasourceConfMapper;
        this.linkRelationConfMapper = linkRelationConfMapper;
        this.dynamicDataSource = dynamicDataSource;
    }


    public List<ProdDatasourceConf> findAllDataSourceConfig() {
        return datasourceConfMapper.select();
    }

    public List<String> findTablesInSchema(String schema) {
        dynamicDataSource.changeLookupKey(ProductizationConfigService.PROD_SOURCE_PREFIX + schema);
        try {
            return datasourceConfMapper.selectTablesInSchema(schema);
        } finally {
            dynamicDataSource.resetToDefault();
        }
    }

    public List<TableField> findFieldsInTable(String schema, String table) {
        dynamicDataSource.changeLookupKey(ProductizationConfigService.PROD_SOURCE_PREFIX + schema);
        try {
            return datasourceConfMapper.selectFieldsInTable(schema, table);
        } finally {
            dynamicDataSource.resetToDefault();
        }
    }

    public List<String> findLinkTables(String schema, String table) {
        return linkRelationConfMapper.selectByContainsSchemaAndTable(schema, table).stream().map(c -> {
            if (c.getLeftSchema().equals(schema) && c.getLeftTable().equals(table))
                return c.getRightSchema() + "." + c.getRightTable();
            else
                return c.getLeftSchema() + "." + c.getLeftTable();
        }).collect(Collectors.toList());
    }

    public ProdLinkRelationConf findLinkRelationConf(ProdLinkRelationConf prodLinkRelationConf) {
        return linkRelationConfMapper.selectBySchemaAndTable(prodLinkRelationConf);
    }


    public List<String> findAllSchemaNames() {
        return datasourceConfMapper.selectAllSchemaNames();
    }


}
