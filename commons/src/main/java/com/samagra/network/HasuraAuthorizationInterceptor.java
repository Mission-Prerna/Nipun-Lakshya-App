package com.samagra.network;


import com.samagra.commons.AppPreferences;
import com.samagra.commons.constants.Constants;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class HasuraAuthorizationInterceptor implements Interceptor {
    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request().newBuilder()
                .addHeader("Authorization", Constants.BEARER_ + AppPreferences.INSTANCE.getUserAuth())
                .build();

        return chain.proceed(request);
    }
}