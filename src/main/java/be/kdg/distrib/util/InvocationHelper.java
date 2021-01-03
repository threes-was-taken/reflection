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

    /**
     * This function will get the parameters from the given prefix
     * @param parameters
     * @param classPrefix
     * @return
     */
    public static Map<String, String> getParamsFromPrefix(Map<String, String> parameters, String classPrefix) {
        Map<String, String> responseParams = new HashMap<>();

        parameters.forEach((k, v) ->{
            //Forgot to check if the key of my param starts with the prefix, this caused an error where the param name of an object ( via recursion )
            // wasn't met and the substring was too long ( index out of bounds exception )
            if (k.startsWith(classPrefix)){
                //if there is an underlying key ( e.g. classPrefix.age or classPrefix.name ) this line will remove the given prefix
                //if there isn't an underlying key, the new key becomes empty ( for simple types )
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

            // fill the parameter map with the returned map of the function parseObjectToMap.
            // this happens for every item in the given arguments
            paramsMap.putAll(parseObjectToMap(p.getName(), o));
        }
        return paramsMap;
    }

    /**
     * This function will create a keyValue map of the given object
     * @param name parameter name
     * @param o object of parameter
     * @return string map of given object
     * @throws IllegalAccessException
     */
    public static Map<String, String> parseObjectToMap(String name, Object o) throws IllegalAccessException {
        Map<String, String> objectMap = new HashMap<>();

        if (checkSimpleClass(o.getClass())){
            //no need to go over the fields of the given object if the type is simple ( e.g. int arg1 = 42 )
            //important to use String.valueOf(), some types cannot be cast to string via '(String) o' and this will give errors
            objectMap.put(name, String.valueOf(o));
        }else {
            Field[] fields = o.getClass().getDeclaredFields();

            for (Field f :
                    fields) {
                f.setAccessible(true);

                //create extended name for complex objects ( e.g. person.age or car.model )
                String extendedName = name + "." + f.getName();

                //map the object with the extended name as key and the value of the field as the value
                objectMap.put(extendedName, String.valueOf(f.get(o)));
            }
        }

        return objectMap;
    }

    /**
     * This function will create an object, with the given arguments, of the given type
     * @param args arguments of the skeleton response message
     * @param returnType type of the returned object
     * @return a new object of the given type
     */
    public static Object createObjectFromType(Map<String, String> args, Class<?> returnType) {
        Object returnObject = null;
        if (returnType.equals(Void.TYPE)){
            return null;
        }

        if (checkSimpleClass(returnType)){
            Class<?> classWrap = getWrapClass(returnType);

            // if type is simple, value doesn't have a key ( int arg1 = 42 for example -> the key here will be "" )
            String val = args.get("");

            try{
                if (classWrap.equals(Character.class)) return val.charAt(0);

                //if the type is simple ( e.g. int arg1 = 42 ) we have to access the ctor of the Wrapper of that type
                //this is because primitive types ( int, boolean, char,...) don't have a constructor but their wrapper classes (Integer, Boolean, Character,...) do.
                Constructor<?> constructor = classWrap.getConstructor(String.class);
                //forgot to access the ctor
                constructor.setAccessible(true);

                //our return object becomes the new instance of the simple type with the correct value.
                returnObject =  constructor.newInstance(val);
                return returnObject;
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException e) {
                System.err.println(e.getMessage());
            }
        }

        // because we work with complex objects, we need to declare our returnobject as a new instance of that type via the declared ctor
        try{
            returnObject = returnType.getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            System.err.println(e.getMessage());
        }

        Field[] objectFields = returnType.getDeclaredFields();
        for (Field f :
                objectFields) {
            f.setAccessible(true);

            //get the object parameters with the field name as a prefix
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
