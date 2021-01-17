package ink.windlively.dataflow.server.service;

import ink.windlively.dataflow.core.flow.DataFlowManager;

import java.util.List;
import java.util.Map;

/**
 *
 * flow配置管理，与{@link DataFlowManager}相似，
 * 但是此类主要为与http接口交互服务，而DataFlowManager不会向外部暴露可调用方法
 *
 */
public interface FlowConfigService {

    List<Map<String, Object>> getAllFlowConfig();

    int updateFlowConfig(Map<String, Object> flowConfig);

    int addFlowConfig(Map<String, Object> flowConfig);

    int deleteFlowConfig(String[] flowIds);

    int reloadFlow(String[] flowIds);

    int reloadFlow();

    int reloadFlow(String source, String schema, String name);
}
