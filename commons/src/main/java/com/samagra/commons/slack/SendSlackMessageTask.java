package com.samagra.commons.slack;

import android.os.AsyncTask;


import com.samagra.commons.BuildConfig;
import com.samagra.grove.logging.Grove;

import java.io.IOException;
import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SendSlackMessageTask extends AsyncTask<String, Void, Void> {

    private String subject;
    private String body;
    private SlackListener slackListener;

    public SendSlackMessageTask(String subject, String body, SlackListener slackListener) {
        this.subject = subject;
        this.body = body;
        this.slackListener = slackListener;
    }

    @Override
    protected Void doInBackground(String... strings) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://slack.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        SlackService slackService = retrofit.create(SlackService.class);
        String token = "Bearer "+ BuildConfig.SLACK_BEARER;
        String channel = BuildConfig.SLACK_CHANNEL;
        HashMap<String,String> map = new HashMap<>();
        map.put("channel", channel);
        map.put("text", subject + "\n\n" + body);
        Call<Response<Object>> responseCall = slackService.postSlackMessage(token, map);
        try {
            Response<Response<Object>> execute = responseCall.execute();
            if(execute.isSuccessful()){
                Grove.e("Custom report to slack :: Success");
            }else {
                Grove.e("Custom report to slack :: Failure");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    protected void onPostExecute(Void c) {
        Grove.e("Crash report sending request has been triggered");
    }
}
