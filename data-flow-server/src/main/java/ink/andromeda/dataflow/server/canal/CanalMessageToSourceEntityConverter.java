package ink.andromeda.dataflow.server.canal;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.otter.canal.protocol.CanalEntry;
import com.alibaba.otter.canal.protocol.Message;
import com.google.common.collect.Lists;
import com.google.protobuf.InvalidProtocolBufferException;
import ink.andromeda.dataflow.core.SourceEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.List;
import java.util.stream.Collectors;

/**
 * {@link Message} -> {@link List<SourceEntity>}
 */
@Slf4j
public class CanalMessageToSourceEntityConverter implements Converter<Message, List<SourceEntity>> {

    @Nullable
    @Override
    public List<SourceEntity> convert(@NonNull Message source) {
        List<SourceEntity> result = Lists.newArrayList();
        // 将entry中的rowChangeList拆分为单独的Message
        for (CanalEntry.Entry entry : source.getEntries()) {
            CanalEntry.RowChange rowChange;
            try {
                rowChange = CanalEntry.RowChange.parseFrom(entry.getStoreValue());
                // 遍历原有的RowChange List, 一条RowChange可能包含多条数据(即RowChange对应的SQL语句影响的行)
                rowChange.getRowDatasList().forEach(rowData -> {
                    SourceEntity sourceEntity = SourceEntity.builder()
                            .before(new JSONObject(rowData.getBeforeColumnsList().stream().collect(Collectors.toMap(CanalEntry.Column::getName, CanalEntry.Column::getValue))))
                            .data(new JSONObject(rowData.getAfterColumnsList().stream().collect(Collectors.toMap(CanalEntry.Column::getName, CanalEntry.Column::getValue))))
                            .opType(entry.getHeader().getEventType().name())
                            .schema(entry.getHeader().getSchemaName())
                            .timestamp(entry.getHeader().getExecuteTime())
                            .name(entry.getHeader().getTableName())
                            .source("")
                            .build();
                    /*
                        // 一个RowData对应一条MySQL的行
                        // 新建一个Message对象, 即转换后的Message结果
                        Message transformMsg = new Message(message.getId());
                        // 转换后的Message的Entry
                        CanalEntry.Entry transformEntry;
                        // 获取转换后的Column List， 仅处理AfterData, 不处理BeforeData
                        List<CanalEntry.Column> columns = transformColumn(entry.getHeader().getSchemaName(), entry.getHeader().getTableName(), rowChange.getEventType().name(),  rowData.getAfterColumnsList());
                        // 新的RowData
                        CanalEntry.RowData newRowData = CanalEntry.RowData.newBuilder()
                                .addAllAfterColumns(columns)
                                .addAllBeforeColumns(rowData.getBeforeColumnsList())
                                .build();
                        // 新的RowChange(改动了RowData), 只包含一个RowData
                        CanalEntry.RowChange newRowChange = CanalEntry.RowChange.newBuilder()
                                .addRowDatas(newRowData)
                                .addAllProps(rowChange.getPropsList())
                                .build();
                        // 新的Entry(改动了RowChange)
                        transformEntry = CanalEntry.Entry.newBuilder()
                                .setHeader(entry.getHeader())
                                .setEntryType(entry.getEntryType())
                                .setStoreValue(newRowChange.toByteString())
                                .build();
                        // 新的Message只包含一个Entry
                        transformMsg.addEntry(transformEntry);
                    */
                    result.add(sourceEntity);
                });
            } catch (InvalidProtocolBufferException e) {
                throw new IllegalStateException(e);
            }
        }
        return result;
    }
}
