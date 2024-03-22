package com.morziz.network.rest;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.morziz.network.config.ClientType;
import com.morziz.network.config.NetworkConfig;
import com.morziz.network.graphql.ApiClient;
import com.morziz.network.network.HttpServiceGenerator;
import com.morziz.network.network.ModuleDependency;
import com.morziz.network.network.Network;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Interceptor;

public class RetrofitClient<S> implements ApiClient<S> {

    private static RetrofitClient retrofitClient;
    private Map<String, ModuleDependency> clientMap;

    private RetrofitClient() {
        if (clientMap == null) {
            clientMap = new HashMap<>();
        }
    }

    public static RetrofitClient getInstance() {
        if (retrofitClient == null) {
            retrofitClient = new RetrofitClient<>();
        }
        return retrofitClient;
    }

    public void createClient(NetworkConfig networkConfig) {
        if (!clientMap.containsKey(networkConfig.getIdentity())) {
            clientMap.put(networkConfig.getIdentity(), new ModuleDependency() {
                @Nullable
                @Override
                public String getGoogleKeys() {
                    return "";
                }

                @NonNull
                @Override
                public String getBaseUrl(@NonNull String type) {
                    return networkConfig.getBaseUrl();
                }

                @Nullable
                @Override
                public HashMap<String, String> getHeaders() {
                    return networkConfig.getHeaders();
                }

                @Override
                public void reValidateUer(int code) {

                }

                @NonNull
                @Override
                public Context getAppContext() {
                    return Network.Companion.getContext();
                }

                @Nullable
                @Override
                public List<Interceptor> getExtraInterceptors() {
                    return networkConfig.getInterceptors();
                }
            });
        }
    }

    public ModuleDependency getModuleDependency(String identity) {
        return clientMap.get(identity);
    }

    @Override
    public ClientType getApiClient() {
        return ClientType.RETROFIT;
    }

    @Nullable
    @Override
    public S getClient(String identity) {
        throw new UnsupportedOperationException("Client cannot be provided without service class, Use method getClient(String identity, Class<S> sClass) to proceed");
    }

    @Override
    public S getClient(String identity, Class<S> sClass) {
        ModuleDependency moduleDependency = getModuleDependency(identity);
        if (moduleDependency == null) {
            if(Network.Companion.getModuleDependency() == null){
                throw new RuntimeException("No Network Config found for the Identity ' %s ' , Please create one using Network.addNetworkConfig()");
            }
            moduleDependency = Network.Companion.getModuleDependency();
        }
        return HttpServiceGenerator.generateResultService(Network.Companion.getContext(), sClass, moduleDependency);
    }


}
