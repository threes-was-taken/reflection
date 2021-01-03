/* Dries created on 03/01/2021 inside the package - be.kdg.distrib.skeletonFactory */
package be.kdg.distrib.skeletonFactory;

import be.kdg.distrib.communication.MessageManager;
import be.kdg.distrib.communication.MethodCallMessage;
import be.kdg.distrib.communication.NetworkAddress;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

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
        Method requestMethod = methodMap.get(message.getMethodName());
        MethodCallMessage response = new MethodCallMessage(this.serverAddress, "result");

        //Void method test
        if (requestMethod.getReturnType().equals(Void.TYPE)){
            response.setParameter("result", "Ok");
        }

        this.messageManager.send(response, message.getOriginator());
    }
}
