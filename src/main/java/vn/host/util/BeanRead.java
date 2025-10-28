package vn.host.util;

import java.lang.reflect.Field;

public class BeanRead {
    public static Object readField(Object bean, String field) throws Exception {
        Field f = bean.getClass().getDeclaredField(field);
        f.setAccessible(true);
        return f.get(bean);
    }
    public static void writeField(Object bean, String field, Object val) throws Exception {
        Field f = bean.getClass().getDeclaredField(field);
        f.setAccessible(true);
        f.set(bean, val);
    }
}