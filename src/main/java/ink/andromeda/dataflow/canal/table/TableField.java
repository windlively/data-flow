package ink.andromeda.dataflow.canal.table;

import lombok.Data;

@Data
public class TableField {

    private int index;

    /**
     * 字段java中类型
     **/
    private int sqlType;

    /**
     * 字段名称(忽略大小写)，在mysql中是没有的
     **/
    private String name;

    /**
     * 是否是主键
     **/
    private boolean isKey;

    /**
     * 如果EventType=UPDATE,用于标识这个字段值是否有修改
     **/
    private boolean updated;

    private boolean isNull;

    private String value;

    /**
     * 对应数据对象原始长度
     **/
    private int length;

    /**
     * 字段mysql类型
     **/
    private String mysqlType;

}