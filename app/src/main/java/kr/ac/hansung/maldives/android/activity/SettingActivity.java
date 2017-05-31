package kr.ac.hansung.maldives.android.activity;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.webkit.CookieManager;
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
    private Button deleteCookie;

    private ImageView imageView;


    private final int REQ_CAMERA_SELECT = 100;  // 아무값이나 무방.

    private boolean lockscreenflag;
    private boolean notificationflag;

    final Context context = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        checkDangerousPermissions();

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

//                onClickView(view);

                final Dialog dialog = new Dialog(context);
                dialog.setContentView(R.layout.galarysettingdialog);

                Button defaultSettingButton = (Button) dialog.findViewById(R.id.defaultsetting);
                Button galarySettingButton = (Button) dialog.findViewById(R.id.galarysetting);

                defaultSettingButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        SharedPreferences sharedPref = getSharedPreferences("backgroundImg.pref", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putString("imgUri", null);
                        editor.commit();
                        imageView.setImageResource(R.drawable.moldive);
                        dialog.dismiss();
                    }
                });

                galarySettingButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        startActivityForResult(intent, REQ_CAMERA_SELECT);
                        dialog.dismiss();
                    }
                });

                dialog.show();
            }
        });

        SharedPreferences sharedPref = getSharedPreferences("backgroundImg.pref", Context.MODE_PRIVATE);
        String uri = sharedPref.getString("imgUri", null);
        imageView = (ImageView) findViewById(R.id.imageView4);

        if (uri != null) {
            Uri imgUri = Uri.parse(uri);
            imageView.setImageURI(imgUri);
        } else {
            imageView.setImageResource(R.drawable.moldive);
        }

        deleteCookie = (Button) findViewById(R.id.deleteCookie);

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

    public void deleteCookie() {
        CookieManager cm = CookieManager.getInstance();
        cm.setAcceptCookie(true);
        cm.removeSessionCookies(null);
    }

    //갤러리 접근 권한 설정
    public void checkDangerousPermissions() {
        String[] permissions = {
                Manifest.permission.READ_EXTERNAL_STORAGE
        };

        int permissionCheck = PackageManager.PERMISSION_GRANTED;
        for (int i = 0; i < permissions.length; i++) {
            permissionCheck = ContextCompat.checkSelfPermission(this, permissions[i]);
            if (permissionCheck == PackageManager.PERMISSION_DENIED) {
                break;
            }
        }

        if (ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[0])) {
        } else {
            ActivityCompat.requestPermissions(this, permissions, 1);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQ_CAMERA_SELECT) {


            if (resultCode == Activity.RESULT_OK) {
                try {

                    Log.d("REAL path is : ", getImagePath(data.getData()));

                    SharedPreferences sharedPref = getSharedPreferences("backgroundImg.pref", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString("imgUri", data.getData().toString());
                    editor.commit();
                    imageView.setImageURI(data.getData());

                } catch (Exception e) {
                    Toast.makeText(getBaseContext(), "사진불러오기실패", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
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

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 1) {
            for (int i = 0; i < permissions.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
//                    Toast.makeText(this, permissions[i] + " 권한이 승인됨.", Toast.LENGTH_LONG).show();
                } else {
//                    Toast.makeText(this, permissions[i] + " 권한이 승인되지 않음.", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

}

