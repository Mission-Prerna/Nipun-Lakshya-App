package com.samagra.network;

import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;

public interface ApolloQueryResponseListener<T> {

    void onResponseReceived(Response<T> response);

    void onFailureReceived(ApolloException e);
}
