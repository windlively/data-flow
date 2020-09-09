package ink.andromeda.dataflow.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.sql.*;
import java.util.Date;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.springframework.util.ReflectionUtils.*;

@Slf4j
public class GeneralTools {

    // Spring的类型转换服务
    private final static ThreadLocal<DefaultConversionService> conversionService = ThreadLocal.withInitial(() -> {
        DefaultConversionService defaultConversionService = new DefaultConversionService();
        // String -> Date 转换器
        // defaultConversionService.addConverter(new StringToJavaDateConverter());

        return defaultConversionService;
    });

    private final static ThreadLocal<Gson> gson = ThreadLocal.withInitial(() -> new GsonBuilder()
            .setDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .create());

    public static Gson GSON() {
        return gson.get();
    }

    public static DefaultConversionService conversionService() {
        return conversionService.get();
    }

    // 取出SQL中所用到的库和表的正则表达式(不支持"SELECT ... FROM t1, t2 WHERE t1.xxx = t2.xxx"的连接查询方式)
    public final static Pattern SQL_TABLE_NAME_REGEX = Pattern.compile("(?<=((FROM)|(JOIN)))(\\s+)((\\w|\\.)*)(?=(,|\\s))", Pattern.CASE_INSENSITIVE);

    // BigDecimal相加, 为避免空指针异常
    public static BigDecimal calcSum(BigDecimal... figure) {
        return Stream.of(figure).filter(Objects::nonNull).reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
    }

    // 根据数据库表生成JavaBean
    public static String dbTableToJavaBean(Connection connection, String tableName) {
        StringBuilder result = new StringBuilder();
        try (
                PreparedStatement preparedStatement = connection.prepareStatement(String.format("select * from %s LIMIT 1", tableName));
                ResultSet rs = preparedStatement.executeQuery();
        ) {
            ResultSetMetaData metaData = rs.getMetaData();
            List<String> columnNames = new ArrayList<>(metaData.getColumnCount());

            result.append("package ;\n" +
                    "\n" +
                    "import java.io.Serializable;\n" +
                    "import java.math.BigDecimal;\n" +
                    "import java.util.Date;\n\n");
            result.append("public class ").append(upCaseToCamelCase(tableName, true)).append(" implements Serializable {\n\n");
            for (int i = 1; i <= metaData.getColumnCount(); i++) {
                String className = metaData.getColumnClassName(i).substring(metaData.getColumnClassName(i).lastIndexOf('.') + 1);
                switch (className) {
                    case "Timestamp":
                        className = "Date";
                        break;
                    case "Byte":
                        className = "Integer";
                        break;
                    default:
                        break;
                }
                result.append("\tprivate ").append(className).append(" ").append(upCaseToCamelCase(metaData.getColumnName(i), false)).append(";\n");
                result.append("\n");
            }
            result.append("}");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result.toString();
    }


    // 获取数据库连接，简化操作，因此连接未手动关闭，不可循环批量查询

    // 获取所有列名
    private static void getTableColumns(Connection connection) {
        PreparedStatement preparedStatement = null;
        try {
            long t1 = System.currentTimeMillis();
            preparedStatement = connection.prepareStatement("SELECT * FROM origin_capital_platform.cp_bank_bill_stage cbbs LEFT JOIN origin_capital_platform.cp_channel_order cco ON cbbs.channel_order_serial_num = cco.channel_order_serial_num WHERE  cco.order_serial_num = 'C0707-7427-1918'");
            ResultSet rs = preparedStatement.executeQuery();
            System.out.println("耗时" + (System.currentTimeMillis() - t1));
            ResultSetMetaData metaData = rs.getMetaData();
            List<String> columnNames = new ArrayList<>(metaData.getColumnCount());

            for (int i = 1; i <= metaData.getColumnCount(); i++) {
                System.out.print(metaData.getColumnName(i) + ",");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 下划线字符串转驼峰式字符串
    public static String upCaseToCamelCase(String s, boolean bigCamelCase) {
        return separatorSegmentToCamelCase(s, '_', bigCamelCase);
    }

    // 驼峰转下划线
    public static String camelCaseToUpCase(String s) {
        return camelCaseToSeparatorSegment(s, '_');
    }

    /**
     * 分隔符分割转驼峰式
     *
     * @param s            原分隔符式字符串
     * @param separator    分隔符
     * @param bigCamelCase 是否为大驼峰
     */
    public static String separatorSegmentToCamelCase(String s, char separator, boolean bigCamelCase) {
        StringBuilder res = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            if (ch == separator) {
                res.append((char) (s.charAt(++i) - 32));
                continue;
            }
            if (i == 0 && bigCamelCase && ch > 90)
                res.append((char) (ch - 32));
            else
                res.append(ch);
        }
        return res.toString();
    }

    /**
     * 驼峰式转分隔符式
     *
     * @param s         原驼峰式字符串
     * @param separator 分隔符
     */
    public static String camelCaseToSeparatorSegment(String s, char separator) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            if (ch <= 90) {
                if (i > 0)
                    stringBuilder.append(separator);
                stringBuilder.append((char) (ch + 32));
                continue;
            }
            stringBuilder.append(ch);
        }
        return stringBuilder.toString();
    }

    // 获取两个日期之间相差的天数
    public static int getIntervalOfDays(Date d1, Date d2) {
        long t1 = d1.getTime();
        long t2 = d2.getTime();
        return (int) (Math.abs(t1 - t2) / (1000 * 3600 * 24));
    }

    public static String toJSONString(Object object) {
        return GSON().toJson(object);
    }

    /**
     * 转换两个对象, 从 src -> dest
     *
     * @param source     源对象
     * @param dest       要转换的对象
     * @param forceCover 当dest对象字段不为空时, 是否强行覆盖
     */
    public static <SRC, DEST> void copyFields(SRC source, DEST dest, boolean forceCover) {
        Class<?> destClass = dest.getClass();
        Class<?> sourceClass = source.getClass();
        // 仅处理当前对象的字段, 不处理继承的字段
        doWithLocalFields(sourceClass, field -> {
            makeAccessible(field);
            Object srcVal = getField(field, source);
            if (srcVal != null) {
                Field destField = findField(destClass, field.getName());
                if (destField != null) {
                    makeAccessible(destField);
                    Object destVal = getField(destField, dest);
                    if (destVal == null || forceCover) {
                        if (field.getType().equals(destField.getType()))
                            setField(destField, dest, srcVal);
                        else
                            setField(destField, dest, conversionService().convert(srcVal, destField.getType()));
                    }
                }
            }
        });
    }

    /**
     * @see #copyFields(Object, Object, boolean)
     */
    public static <SRC, DEST> void copyFields(SRC source, DEST dest) {
        copyFields(source, dest, false);
    }


    // 根据JavaBean生成SQL语句
    public static String printFields(Class<?> clazz) {
        StringBuilder stringBuilder = new StringBuilder();
        for (Field field : clazz.getDeclaredFields()) {
            stringBuilder.append(camelCaseToUpCase(field.getName())).append("=#{")
                    .append(field.getName())
                    .append("}, ");
        }
        return stringBuilder.toString();
    }

    // 根据JavaBean生成MayBatis映射关系
    public static String printResultMap(Class<?> clazz) {
        StringBuilder stringBuilder = new StringBuilder();
        for (Field field : clazz.getDeclaredFields())
            stringBuilder.append(String.format("@Result(column = \"%s\", property = \"%s\"),\n", camelCaseToUpCase(field.getName()), field.getName()));
        return stringBuilder.toString();
    }

    // 批量代码生成
    public static void main(String[] args) throws Exception {
//        System.out.println(dbTableToJavaBean(getCoreConnection(), "core_order"));
//        genAllCoreBean();
//        checkTableFields();
//        System.out.println(camelCaseToSeparatorSegment("RepaymentPlanBean", '-'));
//        System.out.println(printFields(CoreZfptRepaymentBatchPushBean.class));
//        System.out.println(printResultMap(ZfptDailyRepaymentReconciliationResult.class));
//        System.out.println(upCaseToCamelCase("totalPaymentAmount",false));;
//        System.out.println("select".matches("(select)|(insert)|(delete)|(update)"));


    }

    public static void genAllCoreBean() throws IOException {

//        while (scanner.hasNextLine()) {
//            String tableName = scanner.nextLine();
//            if (!StringUtils.isEmpty(tableName)) {
//                String className = upCaseToCamelCase(tableName, true);
//                File file = new File("/Users/baijh/Desktop/class/" + className + ".java");
//                if (!file.exists())
//                    file.createNewFile();
//                try (
//                        OutputStream outputStream = new FileOutputStream(file);
//                        Connection connection = getCoreConnection();
//                ) {
//                    outputStream.write(dbTableToJavaBean(connection, tableName).getBytes(StandardCharsets.UTF_8));
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//
//        }

    }

    ;

    public static String genPrefixedMethodName(String filedName, String prefix) {
        assert filedName != null && prefix != null;
        char ch = filedName.charAt(0);
        if (ch >= 97)
            ch = (char) (ch - 32);
        return prefix + ch + filedName.substring(1);
    }

    public static String genGetterMethodName(String fieldName) {
        return genPrefixedMethodName(fieldName, "get");
    }

    public static String genSetterMethodName(String fieldName) {
        return genPrefixedMethodName(fieldName, "set");
    }

    /**
     * 合并两个对象, 其中master对象的非空值不会被覆盖
     *
     * @param master        以该对象的值为主
     * @param slave         该对象的值做补充
     * @param mergeToMaster 是否合并到master对象
     * @param <M>           对象类型
     * @return 如果mergeToMaster=true, 返回值即为处理后的master对象, 即在master对象做处理, 否则, 返回一个新的对象, 不修改源对象
     */
    public static <M> M mergeObject(@NonNull M master, @NonNull M slave, boolean mergeToMaster) {
        @SuppressWarnings("unchecked") Class<M> clazz = (Class<M>) master.getClass();

        try {

            M result;
            if (mergeToMaster)
                result = master;
            else
                result = clazz.newInstance();

            // 不处理继承到的字段
            doWithLocalFields(clazz, f -> {
                makeAccessible(f);
                Object masterVal = getField(f, master);
                Object slaveVal = getField(f, slave);
                if (masterVal != null && !mergeToMaster)
                    setField(f, result, masterVal);
                else if (masterVal == null && slaveVal != null)
                    setField(f, result, slaveVal);
            });
            return result;
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    /**
     * @see #mergeObject(Object, Object, boolean)
     */
    public static <M> void mergeObject(M master, M slave) {
        mergeObject(master, slave, true);
    }

    /**
     * 为对象设置属性值
     *
     * @param config             配置
     * @param object             对象
     * @param configKeySeparator 配置的key的分隔符, 对应对象实例的驼峰式字段名称
     */
    public static void setBeanProperties(Map<String, Object> config, Object object, char configKeySeparator) {
        Class<?> clazz = object.getClass();
        config.forEach((k, v) -> {
            String methodName = "set" + separatorSegmentToCamelCase(k, configKeySeparator, true);
            Method method = findMethod(clazz, methodName, null);
            if (method != null)
                invokeMethod(method, object, conversionService().convert(v, method.getParameterTypes()[0]));
            else
                log.warn("config item [{}={}] maybe invalid, could not found [{}] method in class [{}]", k, v, methodName, clazz.getSimpleName());
        });

    }

    public static void toString(Object object) {
        System.out.println(object);
    }

    public static String getMongoConfigId(String schemaName, String tableName) {
        return String.format("%s-%s", schemaName, tableName);
    }

    // 数据库类型对应的Java类型
    public static Class<?> jdbcTypeToJavaType(String columnType) {
        switch (columnType) {
            case "varchar":
            case "text":
            case "char":
            case "mediumtext":
            case "longtext":
                return String.class;
            case "blob":
                return byte[].class;
            case "int":
            case "smallint":
            case "tinyint":
                return Integer.class;
            case "date":
            case "datetime":
            case "timestamp":
                return Date.class;
            case "bigint":
                return Long.class;
            case "decimal":
                return BigDecimal.class;
        }
        throw new IllegalArgumentException("not supported mysql column type: " + columnType);
    }

    public static String randomId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    public static void testDataSource(DataSource dataSource) throws SQLException {
        try (
                Connection ignored = dataSource.getConnection();
        ) {}
    }

    public static void testDataSourceConfig(String address, int port, String userName, String password, String schema, @Nullable String args) throws SQLException {
        String jdbcUrl = String.format(
                "jdbc:mysql://%s:%s/%s", address, port, schema
        );
        if (!StringUtils.isEmpty(args)) jdbcUrl += '?' + args;
        testDataSourceConfig(jdbcUrl, userName, password);
    }

    public static void testDataSourceConfig(String jdbcUrl, String userName, String password) throws SQLException {
        try (
                Connection ignored = DriverManager.getConnection(jdbcUrl, userName, password);
        ) {}
    }

    public static DataSource buildDataSource(String jdbcUrl, String userName, String password) {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(jdbcUrl);
        dataSource.setUsername(userName);
        dataSource.setPassword(password);
        dataSource.setConnectionTestQuery("SELECT 1");
        dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
        dataSource.setConnectionTimeout(1000 * 10);
        return dataSource;
    }

    public static DataSource buildDataSource(String address, int port, String userName, String password, String schema, @Nullable String args) {
        String jdbcUrl = String.format(
                "jdbc:mysql://%s:%s/%s", address, port, schema
        );
        if (!StringUtils.isEmpty(args)) jdbcUrl += '?' + args;
        return buildDataSource(jdbcUrl, userName, password);
    }

    public static String javaValToSqlVal(Object javaValue) {
        String strVal;

        if (javaValue instanceof String)
            strVal = "'" + javaValue + "'";
        else if (javaValue instanceof Date)
            strVal = "'" + new DateTime(javaValue).toString(DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")) + "'";
        else
            strVal = javaValue == null ? "null" : javaValue.toString();

        return strVal;
    }
}
