package ink.windlively.dataflow.server.canal.table;

import lombok.Data;

@Data
public class TableEntry {

    CanalMetaInfo header;

    TableRowChange rowChange;

}
