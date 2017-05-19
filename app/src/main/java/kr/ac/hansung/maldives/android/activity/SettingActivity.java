package kr.ac.hansung.maldives.android.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.content.CursorLoader;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Toast;

import kr.ac.hansung.maldives.android.R;
import kr.ac.hansung.maldives.android.service.ScreenService;

public class SettingActivity extends AppCompatActivity {

    private Switch lockScreenSwitch;
    private Switch notificationSwitch;
    private Button button;

    private ImageView imageView;

    private final int REQ_CAMERA_SELECT = 100;  // 아무값이나 무방.

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

        button = (Button) findViewById(R.id.backgroundChangeButton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, REQ_CAMERA_SELECT);
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 100) {

        }

        if (resultCode == Activity.RESULT_OK) {
            try {

                Log.d("REAL path is : ", getImagePath(data.getData()));
                //Bitmap image_bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), data.getData());
                imageView = (ImageView) findViewById(R.id.imageView4);
                //imageView.setImageBitmap(image_bitmap);

                SharedPreferences sharedPref = getSharedPreferences("backgroundImg.pref", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString("imgUri", data.getData().toString());
                editor.commit();



                sharedPref = getSharedPreferences("backgroundImg.pref", Context.MODE_PRIVATE);
                String uri = sharedPref.getString("imgUri", null);


                if (uri != null) {
                    Uri imgUri = Uri.parse(uri);
                    imageView.setImageURI(imgUri);
                } else {
                }

            } catch (Exception e) {
                Toast.makeText(getBaseContext(), "사진불러오기실패", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
    }

    private String getImagePath(Uri data) {
        String[] proj = {MediaStore.Images.Media.DATA};
        CursorLoader loader = new CursorLoader(this, data, proj, null, null, null);
        Cursor cursor = loader.loadInBackground();
        int column_idx = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String result = cursor.getString(column_idx);
        cursor.close();


        return result;

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

