package ink.andromeda.dataflow.util.converter;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import ink.andromeda.dataflow.server.entity.OGGMessage;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;

public class JSONStringToOGGMessageConverter implements Converter<String, OGGMessage> {

    private final Gson gson = new GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .create();

    @Override
    public OGGMessage convert(@NonNull String source) {
        OGGMessage message = gson.fromJson(source, OGGMessage.class);
        String[] str = message.getTable().split("\\.");
        final String schemaName = str[0].toLowerCase();
        final String tableName = str[1].toLowerCase();
        message.setSchemaName(schemaName);
        message.setSimpleTableName(tableName);
        return message;
    }
}
