package com.samagra.commons;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public abstract class NetworkConnectionInterceptor implements Interceptor {

    public abstract boolean isInternetAvailable();

    public abstract void onInternetUnavailable();

    public abstract void onCacheUnavailable();

    @Override
    public Response intercept(Interceptor.Chain chain) throws IOException {
        Request request = chain.request();
        if (!isInternetAvailable()) {
            onInternetUnavailable();
            return null;
        }
        return chain.proceed(request);
    }
}