/* Dries created on 03/01/2021 inside the package - be.kdg.distrib.skeletonFactory */
package be.kdg.distrib.skeletonFactory;

public class SkeletonFactory {

    public static Object createSkeleton(Object implementation) {
        return new SkeletonInvocationHandler(implementation);
    }
}
