package ink.andromeda.dataflow.core.flow;

import ink.andromeda.dataflow.core.Registry;
import ink.andromeda.dataflow.core.SourceEntity;
import ink.andromeda.dataflow.core.TransferEntity;
import ink.andromeda.dataflow.core.node.FlowNode;

import java.util.List;

/**
 * 数据流
 */
public interface DataFlow extends Registry<FlowNode> {

    /**
     * @return 流所匹配的source名称
     */
    default String getApplySource() {
        return "";
    }

    /**
     * @return 流所匹配的schema名称
     */
    default String getApplySchema() {
        return "";
    }

    /**
     * @return 流所匹配的(表)名称
     */
    default String getApplyName() {
        return "";
    }

    /**
     * @return 流的名字
     */
    String getName();

    /**
     * @return 所有的节点
     */
    List<FlowNode> getNodes();

    /**
     * 处理一条数据
     *
     * @param sourceEntity 源数据
     * @return 最后一个节点的返回结果
     * @throws Exception 处理过程发生错误
     */
    default TransferEntity inflow(SourceEntity sourceEntity) throws Exception {
        TransferEntity transferEntity = TransferEntity.builder()
                .source(sourceEntity.getSource())
                .schema(sourceEntity.getSchema())
                .opType(sourceEntity.getOpType())
                .data(sourceEntity.getData())
                .build();
        for (FlowNode flowNode : getNodes()) {
            transferEntity = flowNode.convert(sourceEntity, transferEntity);
            flowNode.export(sourceEntity, transferEntity);
        }
        return transferEntity;
    }

}
