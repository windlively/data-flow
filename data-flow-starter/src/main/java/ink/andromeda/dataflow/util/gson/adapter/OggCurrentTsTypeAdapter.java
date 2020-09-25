package ink.andromeda.dataflow.util.gson.adapter;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import java.io.IOException;
import java.util.Date;

/**
 * OGG Message中的current_ts字段适配器
 */
public class OggCurrentTsTypeAdapter extends TypeAdapter<Date> {

    private final static String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSSSS";

    @Override
    public void write(JsonWriter out, Date value) throws IOException {
        if(value == null)
            out.nullValue();
        else {
            out.value(new DateTime(value).toString(DATE_FORMAT));
        }
    }

    @Override
    public Date read(JsonReader in) throws IOException {
        String s = in.nextString();
        return DateTime.parse(s, DateTimeFormat.forPattern(DATE_FORMAT)).toDate();
    }
}
