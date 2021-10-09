package com.samagra.grove.contracts;

public class LoggingComponentManager {
    public static IGroveLoggingComponent iGroveLoggingComponent;
    public static void registerGroveLoggingComponent(IGroveLoggingComponent groveLoggingComponentImpl) {
        iGroveLoggingComponent = groveLoggingComponentImpl;
    }

}