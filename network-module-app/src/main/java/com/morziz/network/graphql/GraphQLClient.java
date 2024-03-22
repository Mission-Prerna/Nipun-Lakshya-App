package com.morziz.network.graphql;

import androidx.annotation.Nullable;

import com.apollographql.apollo.ApolloClient;
import com.morziz.network.config.ClientType;
import com.morziz.network.config.NetworkConfig;

import java.util.HashMap;
import java.util.Map;

import okhttp3.OkHttpClient;

public class GraphQLClient implements ApiClient<ApolloClient> {

    private static GraphQLClient graphQLClient;
    private Map<String, ApolloClient> clientMap;

    private GraphQLClient() {
        if (clientMap == null) {
            clientMap = new HashMap<>();
        }
    }

    public static GraphQLClient getInstance() {
        if (graphQLClient == null) {
            graphQLClient = new GraphQLClient();
        }
        return graphQLClient;
    }

    public void createClient(NetworkConfig networkConfig) {
        if (!clientMap.containsKey(networkConfig.getIdentity())) {
            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            if (networkConfig.getInterceptors() != null) {
                builder.interceptors().addAll(networkConfig.getInterceptors());
            }
            ApolloClient apolloClient = ApolloClient.builder()
                    .serverUrl(networkConfig.getBaseUrl())
                    .okHttpClient(builder.build()).build();

            clientMap.put(networkConfig.getIdentity(), apolloClient);
        }
    }

    @Override
    public ClientType getApiClient() {
        return ClientType.GRAPHQL;
    }

    @Nullable
    @Override
    public ApolloClient getClient(String identity) {
        return clientMap.get(identity);
    }

    @Override
    public ApolloClient getClient(String identity, Class<ApolloClient> sClass) {
        return getClient(identity);
    }


}
