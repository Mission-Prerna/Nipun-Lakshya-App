package com.morziz.network.network;

import android.content.Context;

public class HttpServiceGenerator {

    //Timeout, specify in SECONDS
    private static final long TIMEOUT_RESPONSE = 120;

    /**
     * Generates the instance for provided service class
     *
     * @param context      in which context service will be fired
     * @param serviceClass which service is required
     * @return instance of generated service class
     */
    @Deprecated
    public static <S> S generate(final Context context, Class<S> serviceClass, ModuleDependency moduleDependency) {
        return HttpServiceGeneratorKt.generate(context, KeyType.normal, serviceClass, TIMEOUT_RESPONSE, moduleDependency);
    }

    public static <S> S generateResultService(final Context context, Class<S> serviceClass, ModuleDependency moduleDependency) {
        return HttpServiceGeneratorKt.generate(context, KeyType.simple, serviceClass, TIMEOUT_RESPONSE, moduleDependency);
    }

    /**
     * Generates a reactive instance of a provided service class with the headers and interceptors
     * added. The service instance is created using the response timeout {@value TIMEOUT_RESPONSE}.
     *
     * @param context      the context {@link Context} in which the service is to be created
     * @param serviceClass the service class
     * @param <S>          the type of service class
     * @return a reactive instance of the service class {@param <S>}.
     */
    @Deprecated
    public static <S> S generateReactive(final Context context, Class<S> serviceClass, ModuleDependency moduleDependency) {
        return HttpServiceGeneratorKt.generate(context, KeyType.reactive, serviceClass, TIMEOUT_RESPONSE, moduleDependency);
    }
}
