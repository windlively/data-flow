package ink.andromeda.dataflow.datasource;


/**
 * @see SwitchSource
 * <p>
 * 数据源名称的枚举, 数据源从配置文件实例化, 名称需要与配置文件中的pool-name一致
 * 此枚举为方便书写代码使用, 也可使用{@link SwitchSource#name()}指定数据源名称
 */
public enum Sources {

    SLAVE("slave", ""),
    MASTER("master", "");

    private final String value;

    public String value(){
        return value;
    }

    Sources(String value, String description) {
        this.value = value;
    }
}
