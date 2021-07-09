package com.strangelet.sudokuchallenge.activities;

import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.app.AppCompatActivity;

import com.strangelet.sudokuchallenge.R;

public class ServiceActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service);

        WebView webView = findViewById(R.id.web_view);
        webView.setWebViewClient(new WebViewClient());
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        if(getIntent().getBooleanExtra("isPrivacyPolicy", false))
            webView.loadUrl("https://the-sudoku-challenge-0.flycricket.io/privacy.html");
        else
            webView.loadUrl("https://the-sudoku-challenge-0.flycricket.io/terms.html");

    }
}