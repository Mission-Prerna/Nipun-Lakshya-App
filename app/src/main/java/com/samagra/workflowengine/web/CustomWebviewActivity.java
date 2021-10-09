package com.samagra.workflowengine.web;//package com.morziz.sampleprojects.web;
//
//import android.graphics.Bitmap;
//import android.os.Bundle;
//import android.view.View;
//import android.webkit.WebView;
//import android.webkit.WebViewClient;
//import android.widget.ProgressBar;
//
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.appcompat.widget.Toolbar;
//
//import com.morziz.sampleprojects.R;
//
//
//public class CustomWebviewActivity extends AppCompatActivity {
//
//
//    private Toolbar toolbar;
//    private WebView mywebview;
//    private ProgressBar spinner;
//    String ShowOrHideWebViewInitialUse = "show";
//
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_web_view);
//
//        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close);
//        mywebview = (WebView) findViewById(R.id.webViewHome);
//        spinner = (ProgressBar) findViewById(R.id.progressBar1);
//        mywebview.setWebViewClient(new CustomWebViewClient());
//        mywebview.getSettings().setJavaScriptEnabled(true);
//        mywebview.getSettings().setDomStorageEnabled(true);
//        mywebview.setOverScrollMode(WebView.OVER_SCROLL_NEVER);
//        String link = (String) getIntent().getSerializableExtra("link");
//        mywebview.loadUrl(link.contains("http") ? link : "https://" + link);
//    }
//
//    private class CustomWebViewClient extends WebViewClient {
//
//        @Override
//        public void onPageStarted(WebView webview, String url, Bitmap favicon) {
//
//            // only make it invisible the FIRST time the app is run
//            if (ShowOrHideWebViewInitialUse.equals("show")) {
//                webview.setVisibility(webview.INVISIBLE);
//            }
//        }
//
//        @Override
//        public void onPageFinished(WebView view, String url) {
//
//            ShowOrHideWebViewInitialUse = "hide";
//            spinner.setVisibility(View.GONE);
//
//            view.setVisibility(mywebview.VISIBLE);
//            super.onPageFinished(view, url);
//
//
//        }
//    }
//}
