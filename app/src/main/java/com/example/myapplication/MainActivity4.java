package com.example.myapplication;


import android.annotation.SuppressLint;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity4 extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main4);

        WebView webView = findViewById(R.id.mjpegWebView);
        webView.setWebViewClient(new WebViewClient()); // Important pour rester dans l'app
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true); // Active JavaScript si besoin

        webView.loadUrl("http://192.168.1.21:8081/video"); // Remplace par l'adresse IP de ton Orange Pi
    }
}
