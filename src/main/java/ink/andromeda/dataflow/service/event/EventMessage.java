package ink.andromeda.dataflow.service.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class EventMessage {

    private Map<String, Object> data;
    /**
     * 事件名称
     */
    private String eventName;
    /**
     * 事件描述
     */
    private String description;
    /**
     * 数据库名
     */
    private String eventSourceSchema;
    /**
     * 表名
     */
    private String eventSourceTable;
    /**
     * 事件操作类型 UPDATE、INSERT、DELETE
     */
    private String eventSourceType;
    /**
     * 订单号
     */
    private String channelOrderNo;

    private boolean success;

    private String msg;
}
