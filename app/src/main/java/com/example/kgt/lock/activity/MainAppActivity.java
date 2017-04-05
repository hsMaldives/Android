package com.example.kgt.lock.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.example.kgt.lock.R;
import com.example.kgt.lock.service.ScreenService;

public class MainAppActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        WebView webView = (WebView) findViewById(R.id.webView);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl("http://m.naver.com");
        webView.setWebViewClient(new WebViewClient());
        webView.setWebChromeClient(new WebChromeClient());
    }

    public void onStartButtonClicked(View v){
        Intent intent = new Intent(this, ScreenService.class);
        startService(intent);
        Toast.makeText(this,"startService",Toast.LENGTH_SHORT).show();

        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean("isBoot",true);
        editor.commit();
    }


    public void onStopButtonClicked(View v){
        Intent intent = new Intent(this , ScreenService.class);
        stopService(intent);
        Toast.makeText(this,"stopService",Toast.LENGTH_SHORT).show();

        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean("isBoot",false);
        editor.commit();
    }
}
