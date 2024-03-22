package com.morziz.network.config;

import com.apollographql.apollo.ApolloClient;
import com.morziz.network.graphql.GraphQLClient;
import com.morziz.network.rest.RetrofitClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConfigManager {

    private static ConfigManager configManager;
    private Map<String, NetworkConfig> configMap;

    private ConfigManager() {
        if (configMap == null) {
            configMap = new HashMap<>();
        }
    }

    public static ConfigManager getInstance() {
        if (configManager == null) {
            configManager = new ConfigManager();
        }
        List<Integer> list = new ArrayList<>();
        return configManager;
    }

    public void addConfig(NetworkConfig networkConfig) {
        if (networkConfig.getBaseUrl() == null) {
            throw new IllegalArgumentException("Network config must have base Url");
        }
        configMap.put(networkConfig.getIdentity(), networkConfig);
        switch (networkConfig.getClientType()) {
            case GRAPHQL:
                GraphQLClient.getInstance().createClient(networkConfig);
                break;
            case RETROFIT:
                RetrofitClient.getInstance().createClient(networkConfig);
                break;
        }
    }

    public ApolloClient getApolloClient(String identity) {
        return GraphQLClient.getInstance().getClient(identity);
    }

    public <S> S getRetrofitServiceClient(String identity, Class<S> sClass) {
        return (S) RetrofitClient.getInstance().getClient(identity, sClass);
    }

}
