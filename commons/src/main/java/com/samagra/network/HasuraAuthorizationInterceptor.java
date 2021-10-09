package com.samagra.network;


import com.samagra.commons.constants.Constants;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class HasuraAuthorizationInterceptor implements Interceptor {
    private final String token;

    public HasuraAuthorizationInterceptor(String token) {
        this.token = token;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request().newBuilder()
                .addHeader("Authorization", Constants.BEARER_  + token)
                .build();

        return chain.proceed(request);
    }
}
