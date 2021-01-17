package ink.windlively.dataflow.util.converter;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;

import java.util.Date;

/**
 * String字符串转为Java的日期类
 */
public class StringToJavaDateConverter implements Converter<String, Date> {

    public static String[] dateFormatters = {"yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd HH:mm:ss.SSS","yyyy/MM/dd HH:mm:ss", "yyyy/MM/dd HH:mm:ss.SSS"};

    @Override
    public Date convert(@NonNull String source) {
        if(source.equals("")) return null;
        Date date = null;
        try{
            date = DateTime.parse(source).toDate();
        }catch (Exception e){
            int i = dateFormatters.length;
            for (String pattern : dateFormatters){
                i --;
                try {
                    date = DateTime.parse(source, DateTimeFormat.forPattern(pattern)).toDate();
                    return date;
                }catch (Exception ex){
                     if(i == 0) throw ex;
                }
            }
        }
        return date;
    }

}
