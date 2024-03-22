package com.morziz.network.graphql;

import com.morziz.network.config.ClientType;

public interface ApiClient<T> {
    ClientType getApiClient();
    T getClient(String identity);
    T getClient(String identity, Class<T> sClass);

}
