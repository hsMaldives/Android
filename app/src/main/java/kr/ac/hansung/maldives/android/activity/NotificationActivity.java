package kr.ac.hansung.maldives.android.activity;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import kr.ac.hansung.maldives.android.R;

/**
 * Created by jeeyoung on 2017-05-07.
 */

public class NotificationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            showBasicNotification();

            //@@gt 예상되는 버그 -> 버튼을 누를 때마다 알림은 큐에 쌓이므로 계속해서 notification을 발생시킬 것이다. (퍼포먼스문제)
        }
    };

    public void onButtonClicked(View v){
        handler.sendEmptyMessageDelayed(0,10000);   //일정시간(ms) 지연시킨다. (1000ms = 1s)
        //animation 추가
        //
        //
        finish();
    }






    //안드4.1?부터 새로운 노티피케이션 형식(일반적인 형식)
    public void showBasicNotification(){
        NotificationCompat.Builder mBuilder = createNotification();
        mBuilder.setContentIntent(createPendingIntent());

        NotificationManager mNotificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(1,mBuilder.build());

    }

    private NotificationCompat.Builder createNotification(){
        Bitmap icon = BitmapFactory.decodeResource(getResources(), R.mipmap.placeholder);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.placeholder)
                .setLargeIcon(icon)
                .setContentTitle("미평가된 장소가 있네요?")
                .setContentText("평가할수록 정확도는 향상됩니다!!!")
                .setSmallIcon(R.mipmap.placeholder)
                .setAutoCancel(true)
                .setWhen(System.currentTimeMillis())
                .setDefaults(Notification.DEFAULT_ALL);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            builder.setCategory(Notification.CATEGORY_MESSAGE)
                    .setPriority(Notification.PRIORITY_HIGH)
                    .setVisibility(Notification.VISIBILITY_PUBLIC);
        }
        return builder;
    }

    /**
     * 노티피케이션을 누르면 실행되는 기능을 가져오는 노티피케이션
     *
     * 실제 기능을 추가하는 것
     * @return
     */
    private PendingIntent createPendingIntent(){
        Intent resultIntent = new Intent(this, MainAppActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainAppActivity.class);
        stackBuilder.addNextIntent(resultIntent);

        return stackBuilder.getPendingIntent(
                0,
                PendingIntent.FLAG_UPDATE_CURRENT
        );
    }
}