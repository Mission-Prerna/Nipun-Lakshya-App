package com.samagra.commons.slack;

import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface SlackService {

    @POST("https://slack.com/api/chat.postMessage")
    Call<Response<Object>> postSlackMessage(@Header("Authorization") String token, @Body HashMap<String,String> body);
}
