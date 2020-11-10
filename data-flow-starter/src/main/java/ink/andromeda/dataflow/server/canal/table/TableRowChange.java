package ink.andromeda.dataflow.server.canal.table;

import lombok.Data;

import java.util.List;

@Data
public class TableRowChange {

    private long tableId;

    private String eventType;

    private String sql;

    private List<TableRow> rowData;

}
