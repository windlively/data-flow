package ink.windlively.dataflow.core.flow;

import org.springframework.lang.Nullable;

import java.util.List;

/**
 * flow管理器, 用于加载、获取flow操作
 * @see DataFlow
 */
public interface DataFlowManager {

    /**
     * 获取所有的flow
     *
     * @return flow list
     */
    List<DataFlow> getFlow();

    /**
     * 获取某个namespace下的所有flow
     *
     * @param source 源名称
     * @param schema 库名称
     * @param name   表名称
     * @return flow list
     */
    List<DataFlow> getFlow(String source, String schema, String name);

    /**
     * 根据flow名称获取flow
     *
     * @param flowName flow名称
     * @return flow
     */
    @Nullable DataFlow getFlow(String flowName);

    /**
     * 根据namespace和flow名称获取flow
     * 默认不可用
     *
     * @param source   源名称
     * @param schema   库名称
     * @param name     表名称
     * @param flowName flow名称
     * @return flow
     */
    default DataFlow getFlow(String source, String schema, String name, String flowName) {
        throw new UnsupportedOperationException();
    }

    /**
     * 重新加载所有flow
     */
    void reload();

    /**
     * 重新加载某个namespace下的flow
     *
     * @param source 源名称
     * @param schema 库名称
     * @param name   表名称
     */
    void reload(String source, String schema, String name);

    /**
     * 重新加载一个flow
     *
     * @param flowName flow名称
     */
    void reload(String flowName);


    /**
     * 根据namespace和flow名称删除flow
     * 默认不可用
     *
     * @param source   源名称
     * @param schema   库名称
     * @param name     表名称
     * @param flowName flow名称
     */
    default void reload(String source, String schema, String name, String flowName) {
        throw new UnsupportedOperationException();
    }
}
