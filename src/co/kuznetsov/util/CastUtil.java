package co.kuznetsov.util;

public class CastUtil {

    public static int asInt(Object o) {
        if (o == null) {
            throw new IllegalArgumentException("Non-null integer expected");
        }
        if (o instanceof Number) {
            return ((Number)o).intValue();
        } else {
            throw new IllegalArgumentException("Non-null integer expected");
        }
    }

    public static String asString(Object o) {
        if (o == null) {
            throw new IllegalArgumentException("Non-null string expected");
        }
        if (o instanceof String) {
            return (String)o;
        } else {
            throw new IllegalArgumentException("Non-null string expected");
        }
    }
}
