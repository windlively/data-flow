package ink.windlively.dataflow.core.flow;


import com.google.gson.reflect.TypeToken;
import ink.windlively.dataflow.core.Registry;
import ink.windlively.dataflow.core.SpringELExpressionService;
import ink.windlively.dataflow.core.node.ConfigurableFlowNode;
import ink.windlively.dataflow.core.node.resolver.DefaultConfigurationResolver;
import ink.windlively.dataflow.util.ConfigValidationException;
import ink.windlively.dataflow.util.JSONValidator;
import ink.windlively.dataflow.util.GeneralTools;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import javax.annotation.PostConstruct;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Slf4j
public abstract class ConfigurableDataFlowManager implements DataFlowManager {

    @Value("classpath:/config-regular/flow_config_regular.json")
    private Resource configRegularFile;

    @Value("classpath:/config-regular/regular_template.json")
    private Resource regularTemplateFile;

    protected final Map<String, List<DataFlow>> flowNamespaceIndexMap = new ConcurrentHashMap<>();

    protected final Map<String, DataFlow> flowNameIndexMap = new ConcurrentHashMap<>();

    protected Supplier<Registry<DefaultConfigurationResolver>> nodeConfigResolverRegistrySupplier = () -> {
      throw new IllegalStateException("convert resolver registry supplier not set");
    };

    protected Supplier<SpringELExpressionService> expressionServiceSupplier = () -> {
        throw new IllegalStateException("expression service supplier not set");
    };

    public void setNodeConfigResolverRegistrySupplier(Supplier<Registry<DefaultConfigurationResolver>> nodeConfigResolverRegistrySupplier) {
        this.nodeConfigResolverRegistrySupplier = nodeConfigResolverRegistrySupplier;
    }

    @PostConstruct
    protected void init(){
        try (InputStream inputStream = configRegularFile.getInputStream();
            InputStream regularTemplateStream = regularTemplateFile.getInputStream();
        ) {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            int data;
            while ((data = inputStream.read()) != -1) os.write(data);
            String str = os.toString(StandardCharsets.UTF_8.name());
            Map<String, Object> flowConfigRegular = GeneralTools.GSON().fromJson(str, new TypeToken<Map<String, Object>>() {
            }.getType());
            os.reset();

            while ((data = regularTemplateStream.read()) != -1) os.write(data);
            str = os.toString();
            Map<String, Object> regularTemplate = GeneralTools.GSON().fromJson(str, new TypeToken<Map<String, Object>>() {
            }.getType());

            LinkedHashMap<String, String> map = new JSONValidator(regularTemplate).validate(flowConfigRegular);

            if(!map.isEmpty()){
                throw new ConfigValidationException(map);
            }

            flowConfigValidator = new JSONValidator(flowConfigRegular);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        reload();
    }

    @Override
    public List<DataFlow> getFlow() {
        return flowNamespaceIndexMap
                .values()
                .stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    @Override
    public List<DataFlow> getFlow(String source, String schema, String name) {
        return Optional.ofNullable(flowNamespaceIndexMap.get(
                String.join(":", source, schema, name)
        )).orElse(Collections.emptyList());
    }

    @Nullable
    public DataFlow getFlow(String flowName) {
        return flowNameIndexMap.get(flowName);
    }

    @NonNull
    public DataFlow getNonNullFlow(String flowName){
        return Optional.ofNullable(getFlow(flowName))
                .orElseThrow(() -> new IllegalArgumentException(String.format(
                        "flow '%s' not exist", flowName
                )));
    }

    @Override
    public synchronized void reload() {
        flowNamespaceIndexMap.clear();
        flowNameIndexMap.clear();
        getFlowConfig().forEach(m -> {
            DataFlow flow = readFlowFromConfig(m);
            refreshIndexMap(flow);
        });

        log.info("load {} flow: {}", flowNameIndexMap.size(),
                flowNameIndexMap.values()
                        .stream()
                        .map(DataFlow::getName)
                        .collect(Collectors.toList()));
    }

    @Override
    public void reload(String source, String schema, String name) {

        flowNamespaceIndexMap.computeIfAbsent(
                String.join(":", source,
                        schema,
                        name),
                k -> new ArrayList<>()).clear();

        getFlowConfig(source, schema, name).forEach(m -> {
            DataFlow flow = readFlowFromConfig(m);
            refreshIndexMap(flow);
        });
    }

    @Override
    public void reload(String flowName) {
        refreshIndexMap(readFlowFromConfig(
                Objects.requireNonNull(getFlowConfig(flowName))
        ));
    }

    private DataFlow readFlowFromConfig(Map<String, Object> flowConfig) {
        validateFlowConfig(flowConfig);
        DefaultDataFlow flow = new DefaultDataFlow((String) flowConfig.get("_id"));
        flow.setApplySource((String) flowConfig.get("source"));
        flow.setApplySchema((String) flowConfig.get("schema"));
        flow.setApplyName((String) flowConfig.get("name"));

        //noinspection unchecked
        Objects.requireNonNull((List<Map<String, Object>>) flowConfig.get("node_list"))
                .forEach(nodeConfig -> {
                    String nodeName = Objects.requireNonNull((String) nodeConfig.get("node_name"),
                    "node name is null");
                    ConfigurableFlowNode flowNode = new ConfigurableFlowNode(nodeName,
                            nodeConfigResolverRegistrySupplier.get(),
                            expressionServiceSupplier.get());
                    flowNode.setConfig(nodeConfig);
                    flow.addLast(flowNode);
                });
        return flow;
    }

    private void refreshIndexMap(DataFlow flow){
        flowNameIndexMap.put(flow.getName(), flow);
        List<DataFlow> flowList = flowNamespaceIndexMap.computeIfAbsent(
                String.join(":",
                        flow.getApplySource(),
                        flow.getApplySchema(),
                        flow.getApplyName()
                        ),
                k -> new ArrayList<>()
        );
        flowList.removeIf(f -> Objects.equals(f.getName(), flow.getName()));
        flowList.add(flow);
        log.info("reload flow: {}", flow.getName());
    }

    /**
     * 获取所有的flow配置
     * get all flow config
     *
     * @return JSON形式的配置
     */
    public abstract List<Map<String, Object>> getFlowConfig();

    /**
     * 获取某个namespace下的flow配置
     *
     * @param source 源名称
     * @param schema 库名称
     * @param name   表名称
     * @return JSON形式的配置
     */
    public abstract List<Map<String, Object>> getFlowConfig(String source, String schema, String name);

    /**
     * 根据flowName获取flow配置
     *
     * @param flowName flow名称
     * @return JSON配置
     */
    @Nullable public abstract Map<String, Object> getFlowConfig(String flowName);

    /**
     * 根据namespace批量添加flow配置
     *
     * @param configs JSON配置内容
     * @return 新增条数
     */
    public abstract int addFlowConfig(List<Map<String, Object>> configs);

    /**
     * 新增一个flow配置
     *
     * @param config flow配置
     * @return 新增条数
     */
    public abstract int addFlowConfig(Map<String, Object> config);

    /**
     * 更新一个flow配置
     *
     * @param flowName flow名称
     * @param update   新的flow配置
     * @return 更新条数
     */
    public abstract int updateFlowConfig(String flowName, Map<String, Object> update);

    /**
     * 删除给定namespace下的flow配置
     *
     * @param source 源名称
     * @param schema 库名称
     * @param name   表名称
     * @return 删除数量
     */
    public abstract int deleteFlowConfig(String source, String schema, String name);

    /**
     * 删除flow配置
     *
     * @param flowName flow名称
     * @return 删除数量
     */
    public abstract int deleteFlowConfig(String flowName);

    /**
     * 为flow新增一个节点配置
     *
     * @param flowName   flow名称
     * @param nodeConfig 新节点配置
     * @return 新增节点数
     */
    public abstract int addNodeConfig(String flowName, Map<String, Object> nodeConfig);

    /**
     * 更新flow节点配置
     *
     * @param flowName flow名称
     * @param nodeName 节点名称
     * @param update   该节点新配置
     * @return 更新的节点数量
     */
    public abstract int updateNodeConfig(String flowName, String nodeName, Map<String, Object> update);

    /**
     * 更新指定位置的flow节点配置
     * 默认不可用
     *
     * @param flowName  flow名称
     * @param nodeIndex 节点位置
     * @param update    该节点新配置
     * @return 更新数量
     */
    public int updateNodeConfig(String flowName, int nodeIndex, Map<String, Object> update) {
        throw new UnsupportedOperationException();
    }

    /**
     * 删除一个flow节点配置
     * 默认不可用
     *
     * @param flowName flow名称
     * @param nodeName 节点名称
     * @return 删除的额节点个数
     */
    public abstract int deleteNodeConfig(String flowName, String nodeName);

    /**
     * 删除一个flow指定位置的节点配置
     *
     * @param flowName  flow名称
     * @param nodeIndex 节点位置
     * @return 删除的节点数量
     */
    public int deleteNodeConfig(String flowName, int nodeIndex) {
        throw new UnsupportedOperationException();
    }

    /**
     * 获取一个节点的配置
     *
     * @param flowName flow名称
     * @param nodeName 节点名称
     * @return 节点配置
     */
    public Map<String, Object> getNodeConfig(String flowName, String nodeName) {
        //noinspection unchecked
        return Optional.ofNullable((List<Map<String, Object>>) getFlowConfig(flowName).get("node_list"))
                .orElse(Collections.emptyList())
                .stream()
                .filter(m -> Objects.equals(m.get("node_name"), nodeName))
                .findFirst()
                .orElse(null);
    }

    private JSONValidator flowConfigValidator;

    public ConfigurableDataFlowManager() {

    }

    /**
     * flow配置校验
     *
     * @param flowConfig flow配置
     * @throws ConfigValidationException 校验未通过
     */
    public void validateFlowConfig(Map<String, Object> flowConfig) throws ConfigValidationException {
        Objects.requireNonNull(flowConfigValidator, "validator is null");
        Map<String, String> errors = flowConfigValidator.validate(flowConfig);
        if (!errors.isEmpty()) {
            throw new ConfigValidationException(errors);
        }
    }

}
