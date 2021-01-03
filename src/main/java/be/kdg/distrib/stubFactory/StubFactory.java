/* Dries created on 03/01/2021 inside the package - be.kdg.distrib.stubFactory */
package be.kdg.distrib.stubFactory;

import be.kdg.distrib.communication.NetworkAddress;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

public class StubFactory {

    public static Object createStub(Class<?> aClass, String ip, int port) {
        NetworkAddress address = new NetworkAddress(ip, port);

        InvocationHandler invocationHandler = new StubInvocationHandler(address);

        return Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),new Class[] {aClass}, invocationHandler);
    }
}
