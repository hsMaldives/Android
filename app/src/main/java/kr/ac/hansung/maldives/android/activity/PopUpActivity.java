package kr.ac.hansung.maldives.android.activity;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class PopUpActivity extends AppCompatActivity {

    int point = 10000;
    RelativeLayout relativeLayout;
    Animation fadeIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(kr.ac.hansung.maldives.android.R.layout.activity_pop_up);

        relativeLayout = (RelativeLayout)findViewById(kr.ac.hansung.maldives.android.R.id.popupLayout);
        fadeIn = AnimationUtils.loadAnimation(this, kr.ac.hansung.maldives.android.R.anim.fade_in);

        relativeLayout.setAnimation(fadeIn);

        //포인트 받아와서 띄워주기
        TextView textView = (TextView)findViewById(kr.ac.hansung.maldives.android.R.id.pointTextView);
        textView.setText(+point+ "points");

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        //총시간, 인터벌(간격)
        new CountDownTimer(4*1000,1000){
            @Override
            public void onTick(long l) {}

            @Override
            public void onFinish() {
                finish();
            }
        }.start();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, kr.ac.hansung.maldives.android.R.anim.fade_out);
    }
}
