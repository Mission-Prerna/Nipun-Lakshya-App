package com.samagra.parent.helper;

import com.samagra.commons.NetworkConnectionInterceptor;
import com.samagra.parent.MyApplication;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

public class OkHttpClientProvider {

    public static OkHttpClient provideOkHttpClient(NetworkConnectionInterceptor networkConnectionInterceptor) {
        OkHttpClient.Builder okhttpClientBuilder = new OkHttpClient.Builder();
        okhttpClientBuilder.connectTimeout(30, TimeUnit.SECONDS);
        okhttpClientBuilder.readTimeout(30, TimeUnit.SECONDS);
        okhttpClientBuilder.writeTimeout(30, TimeUnit.SECONDS);
        okhttpClientBuilder.addInterceptor(networkConnectionInterceptor);
        return okhttpClientBuilder.build();
    }

    public static NetworkConnectionInterceptor getInterceptor(MyApplication application){
        return new NetworkConnectionInterceptor() {
            @Override
            public boolean isInternetAvailable() {
                return application.isOnline();
            }

            @Override
            public void onInternetUnavailable() {
            }

            @Override
            public void onCacheUnavailable() {
            }
        };
    }
}
