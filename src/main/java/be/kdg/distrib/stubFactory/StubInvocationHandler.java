/* Dries created on 03/01/2021 inside the package - be.kdg.distrib.stubFactory */
package be.kdg.distrib.stubFactory;

import be.kdg.distrib.communication.MessageManager;
import be.kdg.distrib.communication.MethodCallMessage;
import be.kdg.distrib.communication.NetworkAddress;

import java.lang.reflect.*;
import java.util.Map;

import static be.kdg.distrib.util.InvocationHelper.*;

public class StubInvocationHandler implements InvocationHandler {
    private final NetworkAddress receiveAddress;
    private final MessageManager messageManager;

    public StubInvocationHandler(NetworkAddress address) {
        this.receiveAddress = address;
        this.messageManager = new MessageManager();
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        System.out.println(method.getName() + " invoked");
        System.out.printf(" -> %s as return type\n", method.getReturnType().getSimpleName());

        MethodCallMessage callMessage = initMethodCall(args, method, this.messageManager.getMyAddress());

        callMessage.getParameters().forEach((k,v) -> System.out.printf("\t%s PARAMS: %s = %s\n", callMessage.getMethodName().toUpperCase(), k, v));

        this.messageManager.send(callMessage, receiveAddress);

        MethodCallMessage response = this.messageManager.wReceive();

        Map<String, String> responseParams = getParamsFromPrefix(response.getParameters(), "result");

        return createObjectFromType(responseParams, method.getReturnType());
    }

    //Create MethodCallMessage from invoked method with parameters
    private MethodCallMessage initMethodCall(Object[] args, Method method, NetworkAddress networkAddress) throws IllegalAccessException {
        MethodCallMessage callMessage = new MethodCallMessage(networkAddress, method.getName());

        //get a map filled with the parsed arguments as parameters for the callMessage
        //set a callMessage parameter for every keyvalue pair in the returned map
        getParamsFromArgs(method.getParameters(), args).forEach(callMessage::setParameter);

        return callMessage;
    }
}
