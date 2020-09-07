package ink.andromeda.dataflow.entity;

public class CanalType {

    public final static String DELETE = "DELETE";

    public final static String UPDATE = "UPDATE";

    public final static String INSERT = "INSERT";

    public final static String MANUAL = "MANUAL";

    public static boolean isDelete(String type) {
        return DELETE.equalsIgnoreCase(type);
    }

    public static boolean isUpdate(String type) {
        return UPDATE.equalsIgnoreCase(type);
    }

    public static boolean isInsert(String type) {
        return INSERT.equalsIgnoreCase(type);
    }

    public static boolean isManual(String type) {
        return MANUAL.equalsIgnoreCase(type);
    }
}
