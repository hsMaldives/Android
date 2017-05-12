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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        lockScreenSwitch = (Switch) findViewById(R.id.lockScreenSwitch);
        lockScreenSwitch.setChecked(false);

        lockScreenSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    onStartButtonClicked();
                }
                else {
                    onStopButtonClicked();
                }
            }
        });
    }
    
    public void onStartButtonClicked() {
        Intent intent = new Intent(this, ScreenService.class);
        startService(intent);
        Toast.makeText(this, "startService", Toast.LENGTH_SHORT).show();

        SharedPreferences sharedPref = getSharedPreferences("bootConfig.pref", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean("isBoot", true);
        editor.commit();
    }

    public void onStopButtonClicked() {
        Intent intent = new Intent(this, ScreenService.class);
        stopService(intent);
        Toast.makeText(this, "stopService", Toast.LENGTH_SHORT).show();

        SharedPreferences sharedPref = getSharedPreferences("bootConfig.pref", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean("isBoot", false);
        editor.commit();
    }
}
