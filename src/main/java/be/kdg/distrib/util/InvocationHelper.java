/* Dries created on 03/01/2021 inside the package - be.kdg.distrib.util */
package be.kdg.distrib.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static be.kdg.distrib.util.ClassChecker.checkSimpleClass;
import static be.kdg.distrib.util.ClassChecker.getWrapClass;

public class InvocationHelper {
    public static Map<String, String> getParamsFromPrefix(Map<String, String> parameters, String classPrefix) {
        Map<String, String> responseParams = new HashMap<>();

        parameters.forEach((k, v) ->{
            //Forgot to check if the key of my param starts with the prefix, this caused an error where the param name of an object ( via recursion )
            // wasn't met and the substring was too long ( index out of bounds exception )
            if (k.startsWith(classPrefix)){

                String pref = k.equals(classPrefix) ? "" : k.substring(classPrefix.length() + 1);

                responseParams.put(pref, v);
            }
        });

        return responseParams;
    }

    public static Map<String, String> getParamsFromArgs(Parameter[] parameters, Object[] args) throws IllegalAccessException {
        //when no params/ args, return empty map (for void functions)
        if (parameters.length == 0 || args == null){
            return Collections.emptyMap();
        }

        Map<String, String> paramsMap = new HashMap<>();

        for (int i = 0; i < args.length; i++) {
            Parameter p = parameters[i];
            Object o = args[i];

            paramsMap.putAll(parseObjectToMap(p.getName(), o));
        }
        return paramsMap;
    }

    public static Map<String, String> parseObjectToMap(String name, Object o) throws IllegalAccessException {
        Map<String, String> objectMap = new HashMap<>();

        if (checkSimpleClass(o.getClass())){
            objectMap.put(name, String.valueOf(o));
        }else {
            Field[] fields = o.getClass().getDeclaredFields();

            for (Field f :
                    fields) {
                f.setAccessible(true);

                String extendedName = name + "." + f.getName();

                objectMap.put(extendedName, String.valueOf(f.get(o)));
            }
        }

        return objectMap;
    }

    public static Object createObjectFromType(Map<String, String> args, Class<?> returnType) {
        Object returnObject = null;
        if (returnType.equals(Void.TYPE)){
            return null;
        }

        if (checkSimpleClass(returnType)){
            Class<?> classWrap = getWrapClass(returnType);

            String val = args.get("");
            try{
                if (classWrap.equals(Character.class)) return val.charAt(0);

                Constructor<?> constructor;
                constructor = classWrap.getConstructor(String.class);
                //forgot to access the ctor
                constructor.setAccessible(true);
                returnObject =  constructor.newInstance(val);
                return returnObject;
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException e) {
                System.err.println(e.getMessage());
            }
        }

        try{
            returnObject = returnType.getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            System.err.println(e.getMessage());
        }

        Field[] objectFields = returnType.getDeclaredFields();
        for (Field f :
                objectFields) {
            f.setAccessible(true);

            Map<String, String> objectParams = getParamsFromPrefix(args, f.getName());

            //recursion if field is of a complex object type
            Object o = createObjectFromType(objectParams, f.getType());

            try{
                f.set(returnObject, o);
            } catch (IllegalAccessException e) {
                System.err.println(e.getMessage());
            }
        }

        return returnObject;
    }
}
