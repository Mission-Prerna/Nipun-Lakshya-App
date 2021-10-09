/*
 * Copyright (C) 2018 Lakshya
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.samagra.workflowengine.web;

import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Bundle;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.ClientCertRequest;
import android.webkit.HttpAuthHandler;
import android.webkit.RenderProcessGoneDetail;
import android.webkit.SafeBrowsingResponse;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.Observer;

import com.samagra.commons.BuildConfig;
import com.samagra.parent.R;
import com.samagra.commons.travel.BroadcastAction;
import com.samagra.commons.travel.BroadcastActionSingleton;
import com.samagra.commons.travel.BroadcastEvents;
import com.samagra.workflowengine.web.model.ui.QuestionResult;
import com.samagra.workflowengine.workflow.WorkflowModuleCallback;
import com.samagra.workflowengine.workflow.model.stateresult.StateResult;

import java.util.List;

//TODO remove @charanpreet
public class WebViewActivity extends AppCompatActivity {

    private WebView webView;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.s_web_view);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        WebViewModel webViewModel = new WebViewModel();
        String openUrl = getIntent().getStringExtra("OPEN_URL");
        openUrl = "";
        webView = (WebView) findViewById(R.id.webView);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        webViewModel.eventLiveData.observe(this, new Observer<AttendanceEventAction>() {
            @Override
            public void onChanged(AttendanceEventAction attendanceEventAction) {
                switch (attendanceEventAction.getEvent()) {
                    case START_LOADER:
                        progressBar.setVisibility(View.VISIBLE);
                        break;
                    case STOP_LOADER:
                        progressBar.setVisibility(View.GONE);
                        break;
                    case RESPONSE_SUCCESS:
                        progressBar.setVisibility(View.GONE);
                        ResultsFragment resultsFragment = new ResultsFragment();
                        resultsFragment.setData((List<QuestionResult>) attendanceEventAction.getData());
                        resultsFragment.setCallback(new WorkflowModuleCallback() {
                            @Override
                            public void onSuccess(StateResult s) {
                                BroadcastActionSingleton.getInstance().getLiveAppAction().setValue(new BroadcastAction(BroadcastEvents.QUML_MODULE_SUCCESS));
//                                workflow.callback.onSuccess();
                                finish();
                            }

                            @Override
                            public void onFailure(StateResult s) {
                                BroadcastActionSingleton.getInstance().getLiveAppAction().setValue(new BroadcastAction(BroadcastEvents.QUML_MODULE_FAILURE));
//                                workflow.callback.onFailure();
                                finish();
                            }
                        });
                        getSupportFragmentManager().beginTransaction().add(R.id.container, resultsFragment, ResultsFragment.class.getSimpleName()).commit();
                        findViewById(R.id.container).setVisibility(View.VISIBLE);
                        webView.setVisibility(View.GONE);
                        break;
                    case RESPONSE_FAILURE:
                        progressBar.setVisibility(View.GONE);
//                        workflow.callback.onFailure();
                        BroadcastActionSingleton.getInstance().getLiveAppAction().setValue(new BroadcastAction(BroadcastEvents.QUML_MODULE_FAILURE));
                        finish();
                        break;
                }
            }
        });

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
//                getSupportActionBar().setTitle(url);
                progressBar.setVisibility(View.VISIBLE);
                if (url.contains("/finish")) {
                    String id = url.substring(url.lastIndexOf("finish/") + 7);
                    webViewModel.fetchResults(WebViewActivity.this, id);
                }
                invalidateOptionsMenu();
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                webView.loadUrl(url);
                return true;
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return super.shouldOverrideUrlLoading(view, request);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                progressBar.setVisibility(View.GONE);
//                getSupportActionBar().setTitle(view.getTitle());
            }

            @Override
            public void onLoadResource(WebView view, String url) {
                super.onLoadResource(view, url);
            }

            @Override
            public void onPageCommitVisible(WebView view, String url) {
                super.onPageCommitVisible(view, url);
            }

            @Nullable
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
                return super.shouldInterceptRequest(view, url);
            }

            @Nullable
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                return super.shouldInterceptRequest(view, request);
            }

            @Override
            public void onTooManyRedirects(WebView view, Message cancelMsg, Message continueMsg) {
                super.onTooManyRedirects(view, cancelMsg, continueMsg);
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                super.onReceivedError(view, errorCode, description, failingUrl);
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
                super.onReceivedHttpError(view, request, errorResponse);
            }

            @Override
            public void onFormResubmission(WebView view, Message dontResend, Message resend) {
                super.onFormResubmission(view, dontResend, resend);
            }

            @Override
            public void doUpdateVisitedHistory(WebView view, String url, boolean isReload) {
                super.doUpdateVisitedHistory(view, url, isReload);
                if (url.contains("/finish")) {
                    String id = url.substring(url.lastIndexOf("finish/") + 7);
                    webViewModel.fetchResults(WebViewActivity.this, id);
                }
            }

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                super.onReceivedSslError(view, handler, error);
            }

            @Override
            public void onReceivedClientCertRequest(WebView view, ClientCertRequest request) {
                super.onReceivedClientCertRequest(view, request);
            }

            @Override
            public void onReceivedHttpAuthRequest(WebView view, HttpAuthHandler handler, String host, String realm) {
                super.onReceivedHttpAuthRequest(view, handler, host, realm);
            }

            @Override
            public boolean shouldOverrideKeyEvent(WebView view, KeyEvent event) {
                return super.shouldOverrideKeyEvent(view, event);
            }

            @Override
            public void onUnhandledKeyEvent(WebView view, KeyEvent event) {
                super.onUnhandledKeyEvent(view, event);
            }

            @Override
            public void onScaleChanged(WebView view, float oldScale, float newScale) {
                super.onScaleChanged(view, oldScale, newScale);
            }

            @Override
            public void onReceivedLoginRequest(WebView view, String realm, @Nullable String account, String args) {
                super.onReceivedLoginRequest(view, realm, account, args);
            }

            @Override
            public boolean onRenderProcessGone(WebView view, RenderProcessGoneDetail detail) {
                return super.onRenderProcessGone(view, detail);
            }

            @Override
            public void onSafeBrowsingHit(WebView view, WebResourceRequest request, int threatType, SafeBrowsingResponse callback) {
                super.onSafeBrowsingHit(view, request, threatType, callback);
            }
        });
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW | WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE);
//        webView.setFocusable(true);
//        webView.setFocusableInTouchMode(true);
//        webView.getSettings().setRenderPriority(WebSettings.RenderPriority.HIGH);
//        webView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
//        webView.getSettings().setDatabaseEnabled(true);
//        webView.getSettings().setAppCacheEnabled(true);
////        webView.getSettings().setAllowContentAccess(true);
//        CookieManager.getInstance().setAcceptCookie(true);
//        CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true);
//        webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        webView.loadUrl(openUrl);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return false;
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            finish();
        }
    }
}
