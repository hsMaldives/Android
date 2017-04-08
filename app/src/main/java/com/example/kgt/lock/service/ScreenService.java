package com.example.kgt.lock.service;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.os.SystemClock;

import com.example.kgt.lock.broadcast.PackageReceiver;
import com.example.kgt.lock.broadcast.RestartReceiver;
import com.example.kgt.lock.broadcast.ScreenReceiver;

public class ScreenService extends Service {

    private ScreenReceiver screenReceiver = null;
    private PackageReceiver packageReceiver = null;


    //screenReceiver를 등록한다.
    private void registerScreenRegister() {
        screenReceiver = new ScreenReceiver();
        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
        registerReceiver(screenReceiver, filter);
    }

    //packageReceiver를 등록한다.
    private void registerPackageReceiver() {
        packageReceiver = new PackageReceiver();
        IntentFilter pFilter = new IntentFilter(Intent.ACTION_PACKAGE_ADDED);
        pFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        pFilter.addAction(Intent.ACTION_PACKAGE_REPLACED);
        pFilter.addDataScheme("package");
        registerReceiver(packageReceiver, pFilter);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        registerScreenRegister();
        registerPackageReceiver();

        registerRestartAlarm(true);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        if (intent != null) {
            if (intent.getAction() == null) {
                if (screenReceiver == null) {
                    registerScreenRegister();
                }
            }
        }


        //NotificationManager notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
       // Notification notification = new Notification(R.drawable.circle, "서비스 실행됨", System.currentTimeMillis());
        //notification.setLatestEventInfo(getApplicationContext(), "Screen Service", "Foreground로 실행됨", null);

        //.......Notification Customzing?
        //..as.df
        //.asd.f.afsd
        startForeground(0, new Notification());


        return START_REDELIVER_INTENT;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (screenReceiver != null) {
            screenReceiver.reenableKeyguard();
            unregisterReceiver(screenReceiver);

            registerRestartAlarm(false);
        }

        if (packageReceiver != null) {
            unregisterReceiver(packageReceiver);
        }


    }

    //ScreenReceiver가 등록되서 화면이 꺼질 때 시스템이 보내주는 ACTION_SCREEN_OFF 를 받아오게 되고 그럼 잠금화면이 짠 나타납니다. 아 !! 그럼 이 서비스는 어디서 실행시켜주느냐 ~ 설정화면 등을 만들어서 켜기버튼 누르면 실행하고 끄기버튼 누르면 취소하고 하면됩니다.ㅋ


    public void registerRestartAlarm(boolean isOn) {
        Intent intent = new Intent(ScreenService.this, RestartReceiver.class);
        intent.setAction(RestartReceiver.ACTION_RESTART_SERVICE);
        PendingIntent sender = PendingIntent.getBroadcast(getApplicationContext(), 0, intent, 0);

        AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
        if (isOn) {
            //1초 뒤부터 30분마다 알람으로 되살림.
            am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + 1000, 1800000, sender);
            //첫번째 매개변수에는 기준 시간이 들어갑니다. ElAPSED_REALTIME_WAKEUP은 현재 시작되는 시간을 0으로 하는 상대적인 시간입니다. 바로 지금! 이라고 하는거죠. ElAPSED_REALTIME 과 다른점은 시스템이 휴면상태에 들어가면 깨워라! 가 추가됩니다. 절대시간을 사용하고 싶다면 RTC 또는 RTC_WAKEUP을 사용하면 됩니다. 두번째 매개변수는 언제 처음 시작할건가 입니다. SystemClock.elapsedRealtime() + 1000 이라는 것은 지금부터 1초(1000ms)후라는 의미입니다. 세번째 매개변수는 반복되는 시간간격입니다. 10000ms마다 이므로 10초에 한번씩 입니다. 마지막 매개변수는 알람이 전해줄 PendingIntent로 위에서 작성한 코드대로면 RestartReceiver에 ACTION_RESTART_SERVICE를 전달하겠습니다.


        } else {
            am.cancel(sender);
        }
    }


}
