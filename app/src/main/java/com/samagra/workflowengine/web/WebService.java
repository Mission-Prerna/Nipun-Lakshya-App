package com.samagra.workflowengine.web;


import com.samagra.workflowengine.web.model.questions.QuestionResponse;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface WebService {

    @GET("https://staging.sunbirded.org/api/question/v1/read/{doId}?fields=body,qType,answer,responseDeclaration,name,solutions,editorState,media,name,board,medium,gradeLevel,subject,topic,learningOutcome,marks")
    Observable<QuestionResponse> getQuestion(@Path("doId") String doId);

}

