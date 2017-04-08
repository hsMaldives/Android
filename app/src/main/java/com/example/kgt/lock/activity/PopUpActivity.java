package com.example.kgt.lock.activity;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.kgt.lock.R;

public class PopUpActivity extends AppCompatActivity {

    int point = 10000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pop_up);

        final RelativeLayout relativeLayout = (RelativeLayout)findViewById(R.id.popupLayout);


        final Animation fadeIn, fadeOut;
        fadeIn = AnimationUtils.loadAnimation(this,R.anim.fade_in);
        fadeOut = AnimationUtils.loadAnimation(this,R.anim.fade_out);




        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        TextView textView = (TextView)findViewById(R.id.pointTextView);
        textView.setText(+point+ "points");


        relativeLayout.setAnimation(fadeIn);


        //총시간, 인터벌(간격)
        new CountDownTimer(5*1000,1000){
            @Override
            public void onTick(long l) {}

            @Override
            public void onFinish() {
                relativeLayout.setAnimation(fadeOut);

                finish();
            }
        }.start();

    }
}
