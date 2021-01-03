/* Dries created on 03/01/2021 inside the package - be.kdg.distrib.util */
package be.kdg.distrib.util;

import java.util.*;

public class ClassChecker {
    private static final List<Class<?>> WRAP_CLASS = new ArrayList<>(Arrays.asList(Integer.class, Boolean.class,
            Character.class, Byte.class, Short.class, Long.class, Float.class, String.class, Void.class, Double.class));

    private static final Map<Class<?>, Class<?>> CLASS_MAP = new HashMap<Class<?>, Class<?>>(){{
        put(int.class, Integer.class);
        put(boolean.class, Boolean.class);
        put(char.class, Character.class);
        put(byte.class, Byte.class);
        put(short.class, Short.class);
        put(long.class, Long.class);
        put(float.class, Float.class);
        put(void.class, Void.class);
        put(double.class, Double.class);
    }};

    public static boolean checkWrapClass(Class<?> aClass){
        return WRAP_CLASS.contains(aClass);
    }

    public static boolean checkSimpleClass(Class<?> aClass){
        return checkWrapClass(aClass) || aClass.isPrimitive();
    }

    public static Class<?> getWrapClass(Class<?> aClass){
        if (checkWrapClass(aClass)) return aClass;

        return CLASS_MAP.get(aClass);
    }
}
