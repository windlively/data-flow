package ink.andromeda.dataflow.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * 方法的执行结果, 为了向上层反馈信息
 */
@AllArgsConstructor
@Data
@Builder
public class CoreResult<E> {

    private String msg;

    private E data;

    private boolean success;
}
