package com.example.kgt.lock.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.example.kgt.lock.R;
import com.example.kgt.lock.service.ScreenService;

public class MainAppActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onStartButtonClicked(View v){
        Intent intent = new Intent(this, ScreenService.class);
        startService(intent);
        Toast.makeText(this,"startService",Toast.LENGTH_SHORT).show();
    }


    public void onStopButtonClicked(View v){
        Intent intent = new Intent(this , ScreenService.class);
        stopService(intent);
        Toast.makeText(this,"stopService",Toast.LENGTH_SHORT).show();
    }
}
