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
            Method requestMethod = methodMap.get(message.getMethodName());
            MethodCallMessage response = new MethodCallMessage(this.serverAddress, "result");

            Object[] requestArgs = new Object[requestMethod.getParameters().length];
            int counter = 0;

            // with this for loop i'll get the arguments needed to create the response object by invocation
            for (int i = 0; i < requestMethod.getParameters().length; i++) {
                Parameter p = requestMethod.getParameters()[i];

                // get the parameters from the requestMethod ( possible prefix will be result.age or result.name )
                //
                Map<String, String> params = getParamsFromPrefix(message.getParameters(), p.getName());

                if (params.size() == 0) throw new IllegalArgumentException("could not recreate parameter");

                Object arg = createObjectFromType(params, p.getType());
                requestArgs[i] = arg;
                counter += params.size();
            }

            if (counter != message.getParameters().size()) throw new IllegalArgumentException("More parameters than the initial message parameters amount");

            Object responseObj = requestMethod.invoke(serverImplementation, requestArgs);


            if (requestMethod.getReturnType().equals(Void.TYPE)) {
                //If our method is of type void there is no need to set any params ( send an empty reply )
                response.setParameter("result", "Ok");
            } else {
                //if our method is not of type void, we need to map the responseObj and set the response params for every key value pair in the map
                parseObjectToMap("result", responseObj).forEach(response::setParameter);
            }

            this.messageManager.send(response, message.getOriginator());
        } catch (IllegalAccessException | InvocationTargetException e) {
            System.err.println("couldn't handle req -> " + e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }
}
