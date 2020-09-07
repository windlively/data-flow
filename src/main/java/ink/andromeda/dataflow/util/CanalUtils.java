package ink.andromeda.dataflow.util;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.otter.canal.protocol.CanalEntry;
import com.google.protobuf.InvalidProtocolBufferException;
import ink.andromeda.dataflow.canal.table.*;
import lombok.extern.slf4j.Slf4j;
import net.abakus.coresystem.data.canal.table.*;
import ink.andromeda.dataflow.entity.SourceEntity;
import org.apache.commons.lang.StringUtils;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class CanalUtils {

    public static CanalEntry.RowChange getRowChange(CanalEntry.Entry entry) {
        CanalEntry.RowChange rowChange;
        try {
            rowChange = CanalEntry.RowChange.parseFrom(entry.getStoreValue());
            return rowChange;
        } catch (InvalidProtocolBufferException exception) {
            log.error("failed to deserialize CanalEntry.Entry", exception);
        }
        return null;
    }

    public static CanalEntry.Column getColumnByName(CanalEntry.RowData rowData, String columnName) {
        for (CanalEntry.Column column : rowData.getAfterColumnsList()) {
            if (StringUtils.equalsIgnoreCase(columnName, column.getName())) {
                return column;
            }
        }
        return null;
    }

    public static String getValueByName(CanalEntry.RowData rowData, String columnName) {
        CanalEntry.Column column = getColumnByName(rowData, columnName);
        if (column != null) {
            return column.getValue();
        }
        return null;
    }



    public static TableEntry convertCanalEntry(CanalEntry.Entry entry) {
        TableEntry tableEntry = new TableEntry();
        try {
            CanalEntry.RowChange rowChange = CanalUtils.getRowChange(entry);
            List<TableRow> tableRowList = new ArrayList<>();
            assert rowChange != null;
            List<CanalEntry.RowData> rowDataList = rowChange.getRowDatasList();
            
            for (CanalEntry.RowData rowData : rowDataList) {
                if (CollectionUtils.isEmpty(rowData.getAfterColumnsList()) && CollectionUtils.isEmpty(rowData.getBeforeColumnsList())) {
                    log.warn("empty canal entry {}", entry.toString());
                }
                TableRow tableRow = new TableRow();
                List<TableField> afterRow = new ArrayList<>();
                for (CanalEntry.Column column : rowData.getAfterColumnsList()) {
                    convertCanalColumnToTableRow(afterRow, column);
                }
                tableRow.setRow(afterRow);
                List<TableField> beforeRow = new ArrayList<>();
                for (CanalEntry.Column column : rowData.getBeforeColumnsList()) {
                    convertCanalColumnToTableRow(beforeRow, column);
                }
                tableRow.setBeforeRow(beforeRow);
                tableRowList.add(tableRow);
            }

            TableRowChange tableRowChange = new TableRowChange();
            tableRowChange.setRowData(tableRowList);
            tableRowChange.setEventType(entry.getHeader().getEventType().name());
            tableRowChange.setSql(rowChange.getSql());
            tableRowChange.setTableId(rowChange.getTableId());

            CanalMetaInfo canalMetaInfo = new CanalMetaInfo();
            canalMetaInfo.setEventLength(entry.getHeader().getEventLength());
            canalMetaInfo.setExecuteTime(entry.getHeader().getExecuteTime());
            canalMetaInfo.setLogfileName(entry.getHeader().getLogfileName());
            canalMetaInfo.setSchemaName(entry.getHeader().getSchemaName());
            canalMetaInfo.setTableName(entry.getHeader().getTableName());

            canalMetaInfo.setEventType(entry.getHeader().getEventType().name());
            tableEntry.setHeader(canalMetaInfo);
            tableEntry.setRowChange(tableRowChange);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            e.printStackTrace();
        }
        return tableEntry;
    }

    private static void convertCanalColumnToTableRow(List<TableField> row, CanalEntry.Column column) {
        TableField field = new TableField();
        field.setIndex(column.getIndex());
        field.setKey(column.getIsKey());
        field.setLength(column.getLength());
        field.setName(column.getName());
        field.setValue(column.getValue());
        row.add(field);
    }

    public static List<SourceEntity> convertTableEntryToBusinessBean(TableEntry entry){
        List<TableRow> rowList= entry.getRowChange().getRowData();
        List<SourceEntity> list = new ArrayList<>(rowList.size());
        CanalMetaInfo metaInfo = entry.getHeader();
        rowList.forEach(row -> {
            JSONObject data = new JSONObject();
            JSONObject before = new JSONObject();
            row.getRow().forEach(f -> data.put(f.getName(), f.getValue()));
            row.getBeforeRow().forEach(f -> before.put(f.getName(), f.getValue()));
            SourceEntity sourceEntity = SourceEntity.builder()
                    .data(data)
                    .schema(metaInfo.getSchemaName())
                    .name(metaInfo.getTableName())
                    .opType(metaInfo.getEventType())
                    .before(before)
                    .build();
            list.add(sourceEntity);
            // ApplicationEventService.next(REC_BUS_BEAN, businessEntity);
        });
        return list;
    }
}
