package com.samagra.workflowengine.web;

import static com.samagra.commons.utils.RemoteConfigUtils.getFirebaseRemoteConfigInstance;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import androidx.core.text.HtmlCompat;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hasura1.model.QumlResponseQuery;
import com.morziz.network.config.ClientType;
import com.morziz.network.network.Network;
import com.samagra.commons.utils.RemoteConfigUtils;
import com.samagra.workflowengine.web.model.questions.Option;
import com.samagra.workflowengine.web.model.questions.QuestionResponse;
import com.samagra.workflowengine.web.model.quml.Child;
import com.samagra.workflowengine.web.model.quml.QumlResponse;
import com.samagra.workflowengine.web.model.ui.QuestionResult;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class WebViewModel extends ViewModel {
    public MutableLiveData<AttendanceEventAction> eventLiveData;
    public MutableLiveData<List<Child>> attendanceHistoryLive = new MutableLiveData<>();
    private Handler handler;

    public WebViewModel() {
        attendanceHistoryLive.setValue(new ArrayList<>());
        handler = new Handler(Looper.getMainLooper());
        init();
    }

    private void init() {
        eventLiveData = new MutableLiveData<>();
    }

    public void fetchResults(Context context, String id) {
        handler.post(() -> eventLiveData.setValue(new AttendanceEventAction(WebEvent.START_LOADER)));
        WebService service = Network.Companion.getClient(
                ClientType.RETROFIT,
                WebService.class, getFirebaseRemoteConfigInstance().getString(
                RemoteConfigUtils.LOGIN_SERVICE_BASE_URL));
        WebRepository.getInstance().fetchResponse(id, new ApolloQueryResponseListener<QumlResponseQuery.Data>() {
            @Override
            public void onResponseReceived(Response<QumlResponseQuery.Data> response) {
                List<Child> children = new ArrayList<>();
                List<String> doIds = new ArrayList<>();
                if (response.getData() != null) {
                    List<QumlResponseQuery.QumlResponse> qumlResponses = response.getData().qumlResponse();
                    if (!qumlResponses.isEmpty()) {
                        String body = qumlResponses.get(0).body();
                        List<QumlResponse> qumlSubmissions = new Gson().fromJson(body, new TypeToken<List<QumlResponse>>() {
                        }.getType());
                        for (QumlResponse qumlResponse : qumlSubmissions) {
                            children.addAll(qumlResponse.getChildren());
                        }
                    }
                }
                for (Child child : children) {
                    String identifier = child.getIdentifier();
                    doIds.add(identifier);
                }

                Observable.fromIterable(doIds).flatMap(new Function<String, ObservableSource<QuestionResponse>>() {
                    @Override
                    public ObservableSource<QuestionResponse> apply(String s) throws Exception {
                        return Observable.zip(Observable.just(s),
                                service.getQuestion(s),
                                new BiFunction<String, QuestionResponse, QuestionResponse>() {
                                    @Override
                                    public QuestionResponse apply(String s, QuestionResponse questionResponse) throws Exception {
                                        return questionResponse;
                                    }
                                });
                    }
                })
                        .toList()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(pairList -> {
                            List<QuestionResult> uiResponse = getUiResponse(pairList, children);
                            handler.post(() -> eventLiveData.setValue(new AttendanceEventAction(WebEvent.RESPONSE_SUCCESS, uiResponse)));

                        }, error -> {
                            String message = error.getMessage();
                            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                            handler.post(() -> eventLiveData.setValue(new AttendanceEventAction(WebEvent.RESPONSE_FAILURE)));
                        });
            }

            @Override
            public void onFailureReceived(ApolloException e) {
                handler.post(() -> eventLiveData.setValue(new AttendanceEventAction(WebEvent.STOP_LOADER)));
            }
        });
    }

    public List<QuestionResult> getUiResponse(List<QuestionResponse> questionResponses, List<Child> children) {
        List<QuestionResult> questionResults = new ArrayList<>();
        for (Child child : children) {
            for (QuestionResponse questionResponse : questionResponses) {
                if (child.getIdentifier().equalsIgnoreCase(questionResponse.getResult().getQuestion().getIdentifier())) {
                    QuestionResult questionResult = new QuestionResult();
                    String question = questionResponse.getResult().getQuestion().getEditorState().getQuestion();
                    questionResult.setQuestion(HtmlCompat.fromHtml(question, HtmlCompat.FROM_HTML_MODE_LEGACY).toString());
                    questionResult.setDoID(child.getIdentifier());
                    String userAnswer;
                    if (child.getOption() != null && child.getOption().getLabel() != null) {
                        userAnswer = HtmlCompat.fromHtml(child.getOption().getLabel(), HtmlCompat.FROM_HTML_MODE_LEGACY).toString();
                    } else {
                        userAnswer = "-";
                    }
                    questionResult.setUserAnswer(userAnswer);
                    questionResult.setCorrectAnswer(child.getClass_().equalsIgnoreCase("correct"));
                    questionResult.setAnswerStatus(child.getClass_().substring(0, 1).toUpperCase() + child.getClass_().substring(1));
                    List<String> optionsText = new ArrayList<>();
                    for (Option option : questionResponse.getResult().getQuestion().getEditorState().getOptions()) {
                        String s = HtmlCompat.fromHtml(option.getValue().getBody(), HtmlCompat.FROM_HTML_MODE_LEGACY).toString();
                        optionsText.add(questionResult.sanitize(s));
                        if (option.getAnswer()) {
                            questionResult.setCorrectAnswer(s);
                        }
                    }
                    questionResult.setOptions(optionsText);
                    questionResults.add(questionResult);
                    break;
                }
            }
        }
        return questionResults;
    }

}
