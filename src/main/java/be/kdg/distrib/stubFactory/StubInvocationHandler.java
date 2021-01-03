/* Dries created on 03/01/2021 inside the package - be.kdg.distrib.stubFactory */
package be.kdg.distrib.stubFactory;

import be.kdg.distrib.communication.NetworkAddress;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class StubInvocationHandler implements InvocationHandler {
    private final NetworkAddress receiveAddress;

    public StubInvocationHandler(NetworkAddress address) {
        this.receiveAddress = address;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        return null;
    }
}
