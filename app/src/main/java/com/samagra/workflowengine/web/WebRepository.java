package com.samagra.workflowengine.web;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.hasura1.model.QumlResponseQuery;
import com.samagra.network.HasuraAuthorizationInterceptor;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

import okhttp3.OkHttpClient;

//TODO Remove = @charanpreet
public class WebRepository {

    private final String token;
    private static WebRepository instance;
    private final ApolloClient apolloClient;

    private WebRepository() {
        token = "";
        apolloClient = ApolloClient.builder()
                .serverUrl("" + "")
                .okHttpClient(
                        new OkHttpClient.Builder()
                                .addInterceptor(new HasuraAuthorizationInterceptor())
                                .build()
                ).build();
    }

    public static WebRepository getInstance() {
        if (instance == null) {
            instance = new WebRepository();
        }
        return instance;
    }

    public void fetchResponse(String id, ApolloQueryResponseListener<QumlResponseQuery.Data> listener) {
        QumlResponseQuery fetchTeacherAttendanceQuery = QumlResponseQuery.builder().id(id).build();
        apolloClient
                .query(fetchTeacherAttendanceQuery)
                .enqueue(new ApolloCall.Callback<QumlResponseQuery.Data>() {
                    @Override
                    public void onResponse(@NotNull Response<QumlResponseQuery.Data> response) {
                        if (response.getData() != null && response.getErrors() == null) {
                            listener.onResponseReceived(response);
                        } else {
                            listener.onFailureReceived(new ApolloException(response.getErrors().get(0).getMessage()));
                        }
                    }

                    @Override
                    public void onFailure(@NotNull ApolloException e) {
                        if (e != null) {
                            e.getStackTrace();
                        }
                        String s = Arrays.toString(e.getStackTrace());
                        listener.onFailureReceived(e);
                    }
                });
    }


}
