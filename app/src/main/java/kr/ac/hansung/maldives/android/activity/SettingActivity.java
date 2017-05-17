package kr.ac.hansung.maldives.android.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import kr.ac.hansung.maldives.android.R;
import kr.ac.hansung.maldives.android.service.ScreenService;

public class SettingActivity extends AppCompatActivity {

    private Switch lockScreenSwitch;
    private Switch notificationSwitch;

    private boolean lockscreenflag;
    private boolean notificationflag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        SharedPreferences lockscreenPref = getSharedPreferences("bootConfig.pref", Context.MODE_PRIVATE);
        lockscreenflag = (Boolean) lockscreenPref.getBoolean("isBoot", false);

        lockScreenSwitch = (Switch) findViewById(R.id.lockScreenSwitch);
        lockScreenSwitch.setChecked(lockscreenflag);

        lockScreenSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    startLockScreenService();
                } else {
                    stopLockScreenServcie();
                }
            }
        });

        SharedPreferences notificationPref = getSharedPreferences("notificationConfig.pref", Context.MODE_PRIVATE);
        notificationflag = (Boolean) notificationPref.getBoolean("notificationflag", true);

        notificationSwitch = (Switch) findViewById(R.id.notificationSwitch);
        notificationSwitch.setChecked(notificationflag);

        notificationSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    onNotificationSwitch();
                } else {
                    offNotificationSwitch();
                }
            }
        });
    }

    public void startLockScreenService() {
        Intent intent = new Intent(this, ScreenService.class);
        startService(intent);
        Toast.makeText(this, "startService", Toast.LENGTH_SHORT).show();

        SharedPreferences sharedPref = getSharedPreferences("bootConfig.pref", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean("isBoot", true);
        editor.commit();
    }

    public void stopLockScreenServcie() {
        Intent intent = new Intent(this, ScreenService.class);
        stopService(intent);
        Toast.makeText(this, "stopService", Toast.LENGTH_SHORT).show();

        SharedPreferences sharedPref = getSharedPreferences("bootConfig.pref", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean("isBoot", false);
        editor.commit();
    }

    public void onNotificationSwitch() {
        Toast.makeText(this, "NotificationOn", Toast.LENGTH_SHORT).show();

        SharedPreferences sharedPref = getSharedPreferences("notificationConfig.pref", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean("notificationflag", true);
        editor.commit();
    }

    public void offNotificationSwitch() {
        Toast.makeText(this, "NotificationOff", Toast.LENGTH_SHORT).show();

        SharedPreferences sharedPref = getSharedPreferences("notificationConfig.pref", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean("notificationflag", false);
        editor.commit();
    }
}
