/* Dries created on 03/01/2021 inside the package - be.kdg.distrib.skeletonFactory */
package be.kdg.distrib.skeletonFactory;

import be.kdg.distrib.communication.MessageManager;
import be.kdg.distrib.communication.MethodCallMessage;
import be.kdg.distrib.communication.NetworkAddress;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;

import static be.kdg.distrib.util.InvocationHelper.*;

public class SkeletonInvocationHandler implements Skeleton{
    private final Object serverImplementation;
    private final NetworkAddress serverAddress;
    private final MessageManager messageManager;
    private final Map<String, Method> methodMap;

    public SkeletonInvocationHandler(Object implementation) {
        this.serverImplementation = implementation;
        this.messageManager = new MessageManager();
        this.serverAddress = this.messageManager.getMyAddress();
        this.methodMap = new HashMap<String, Method>(){{
            for (Method m :
                    implementation.getClass().getDeclaredMethods()) {
                put(m.getName(), m);
            }
        }};
    }


    @Override
    public void run() {
        Thread serverThread = new Thread(() -> {
            //basic listen method ( from distributed systems exercise )
            while(true){
                MethodCallMessage request = this.messageManager.wReceive();
                handleRequest(request);
            }
        });

        serverThread.start();
    }

    @Override
    public NetworkAddress getAddress() {
        return serverAddress;
    }

    @Override
    public void handleRequest(MethodCallMessage message) {
        try {
            System.out.printf("Method %s called", message.getMethodName());
            Method requestMethod = methodMap.get(message.getMethodName());
            MethodCallMessage response = new MethodCallMessage(this.serverAddress, "result");

            Object[] requestArgs = new Object[requestMethod.getParameters().length];
            int counter = 0;

            for (int i = 0; i < requestMethod.getParameters().length; i++) {
                Parameter p = requestMethod.getParameters()[i];

                Map<String, String> params = getParamsFromPrefix(message.getParameters(), p.getName());

                if (params.size() == 0) throw new IllegalArgumentException("could not recreate parameter");

                Object arg = createObjectFromType(params, p.getType());
                requestArgs[i] = arg;
                counter += params.size();
            }

            if (counter != message.getParameters().size()) throw new IllegalArgumentException("More parameters than the initial message parameters amount");

            Object responseObj = requestMethod.invoke(serverImplementation, requestArgs);

            //Void method test
            if (requestMethod.getReturnType().equals(Void.TYPE)) {
                response.setParameter("result", "Ok");
            } else {
                parseObjectToMap("result", responseObj).forEach(response::setParameter);
            }

            this.messageManager.send(response, message.getOriginator());
        } catch (IllegalAccessException | InvocationTargetException e) {
            System.err.println("couldn't handle req -> " + e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }
}
