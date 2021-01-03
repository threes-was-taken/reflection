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
        System.out.printf("%s invoked", method.getName());
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
            String pref = k.equals(classPrefix) ? "" : k.substring(classPrefix.length() + 1);

            responseParams.put(pref, v);
        });

        return responseParams;
    }

    private Map<String, String> getParams(Parameter[] parameters, Object[] args) {
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
            }
        }
        return paramsMap;
    }

    private Object parseInvokeInstance(Map<String, String> parameters, Class<?> returnType) {
        Object returnObject = null;
        if (returnType.equals(Void.TYPE)){
            return null;
        }

        if (checkSimpleClass(returnType)){
            String val = parameters.get("");
            try{
                if (getWrapClass(returnType).equals(Character.class)) return val.charAt(0);

                Constructor<?> constructor;
                constructor = getWrapClass(returnType).getConstructor(String.class);
                //forgot to access the ctor
                constructor.setAccessible(true);
                returnObject =  constructor.newInstance(val);
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException e) {
                System.err.println(e.getMessage());
            }
        }

        return returnObject;
    }

}
