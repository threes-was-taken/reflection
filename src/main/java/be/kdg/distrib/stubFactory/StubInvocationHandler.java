/* Dries created on 03/01/2021 inside the package - be.kdg.distrib.stubFactory */
package be.kdg.distrib.stubFactory;

import be.kdg.distrib.communication.MessageManager;
import be.kdg.distrib.communication.MethodCallMessage;
import be.kdg.distrib.communication.NetworkAddress;

import java.lang.reflect.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static be.kdg.distrib.util.ClassChecker.checkSimpleClass;
import static be.kdg.distrib.util.ClassChecker.getWrapClass;

public class StubInvocationHandler implements InvocationHandler {
    private final NetworkAddress receiveAddress;
    private final MessageManager messageManager;

    public StubInvocationHandler(NetworkAddress address) {
        this.receiveAddress = address;
        this.messageManager = new MessageManager();
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        System.out.printf("%s invoked\n", method.getName());
        System.out.printf(" -> %s as return type", method.getReturnType().getSimpleName());

        MethodCallMessage callMessage = new MethodCallMessage(this.messageManager.getMyAddress(), method.getName());

        Map<String, String> messageParams = getParams(method.getParameters(), args);

        messageParams.forEach(callMessage::setParameter);

        callMessage.getParameters().forEach((k,v) -> System.out.printf("\t%s PARAMS: %s = %s\n", callMessage.getMethodName().toUpperCase(), k, v));

        this.messageManager.send(callMessage, receiveAddress);

        MethodCallMessage response = this.messageManager.wReceive();

        Map<String, String> responseParams = getResponseParams(response.getParameters(), "result");

        return parseInvokeInstance(responseParams, method.getReturnType());
    }

    private Map<String, String> getResponseParams(Map<String, String> parameters, String classPrefix) {
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

    private Map<String, String> getParams(Parameter[] parameters, Object[] args) throws IllegalAccessException {
        //when no params/ args, return empty map (for void functions)
        if (parameters.length == 0 || args == null){
            return Collections.emptyMap();
        }

        Map<String, String> paramsMap = new HashMap<>();

        for (int i = 0; i < args.length; i++) {
            Parameter p = parameters[i];
            Object o = args[i];

            if (checkSimpleClass(o.getClass())){
                paramsMap.put(p.getName(), String.valueOf(o));
            }else {
                Field[] fields = o.getClass().getDeclaredFields();

                for (Field f :
                        fields) {
                    f.setAccessible(true);

                    String name = p.getName() + "." + f.getName();

                    paramsMap.put(name, String.valueOf(f.get(o)));
                }
            }
        }
        return paramsMap;
    }

    private Object parseInvokeInstance(Map<String, String> args, Class<?> returnType) {
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

            Map<String, String> objectParams = getResponseParams(args, f.getName());

            //recursion if field is of a complex object type
            Object o = parseInvokeInstance(objectParams, f.getType());

            try{
                f.set(returnObject, o);
            } catch (IllegalAccessException e) {
                System.err.println(e.getMessage());
            }
        }

        return returnObject;
    }

}
