package ink.andromeda.dataflow.entity;

public enum AppEventSubject {

    SYNC_SUCCESS("同步成功"),
    EVENT_MATCHED("匹配到事件"),
    REC_BUS_BEAN("得到一条BusinessBean"),
    HIS_BATCH_ONE_FIN("单条历史数据处理完毕"),
    HIS_BATCH_ALL_FIN("全部历史数据处理完毕"),
    HIS_SYNC_FIN("历史数据同步成功"),
    HIS_EVENT_FIN("历史数据事件匹配成功");

    AppEventSubject(String description) {

    }
}
