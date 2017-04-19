package kr.ac.hansung.maldives.android.activity;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import kr.ac.hansung.maldives.android.service.ScreenService;

public class MainAppActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(kr.ac.hansung.maldives.android.R.layout.activity_main);

        //위치사용허용 -> 다운받았을 때 처음화면에 띄울 것
        checkDangerousPermissions();

        //위치정보 사용
        chkGpsService();

        webViewConfig();
    }

    private void webViewConfig() {
        WebView webView = (WebView) findViewById(kr.ac.hansung.maldives.android.R.id.webView);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl("http://223.194.145.81:80/WhereYou");
        webView.setWebViewClient(new WebViewClient());
        webView.setWebChromeClient(new WebChromeClient());
    }

    //위치 사용 권한 설정
    public void checkDangerousPermissions() {
        String[] permissions = {
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        };

        int permissionCheck = PackageManager.PERMISSION_GRANTED;
        for (int i = 0; i < permissions.length; i++) {
            permissionCheck = ContextCompat.checkSelfPermission(this, permissions[i]);
            if (permissionCheck == PackageManager.PERMISSION_DENIED) {
                break;
            }
        }

//        if(permissionCheck == PackageManager.PERMISSION_GRANTED) {
//          Toast.makeText(this, "권한 있음", Toast.LENGTH_LONG).show();
//        } else {
//            Toast.makeText(this, "권한 없음", Toast.LENGTH_LONG).show();

        if (ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[0])) {
//                Toast.makeText(this, "권한 설명 필요함", Toast.LENGTH_LONG).show();
        } else {
            ActivityCompat.requestPermissions(this, permissions, 1);
        }
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

    //GPS 설정 체크
    public boolean chkGpsService() {

        String gps = android.provider.Settings.Secure.getString(getContentResolver(), android.provider.Settings.Secure.LOCATION_PROVIDERS_ALLOWED);

        Log.d(gps, "aaaa");

        if (!(gps.matches(".*gps.*") && gps.matches(".*network.*"))) {

            // GPS OFF 일때 Dialog 표시
            AlertDialog.Builder gsDialog = new AlertDialog.Builder(this);
            gsDialog.setTitle("위치 서비스 설정");
            gsDialog.setMessage("무선 네트워크 사용, GPS 위성 사용을 모두 체크하셔야 정확한 위치 서비스가 가능합니다.\n위치 서비스 기능을 설정하시겠습니까?");
            gsDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    // GPS설정 화면으로 이동
                    Intent intent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    intent.addCategory(Intent.CATEGORY_DEFAULT);
                    startActivity(intent);
                }
            })
                    .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            return;
                        }
                    }).create().show();
            return false;

        } else {
            return true;
        }
    }

    public void onStartButtonClicked(View v) {
        Intent intent = new Intent(this, ScreenService.class);
        startService(intent);
        Toast.makeText(this, "startService", Toast.LENGTH_SHORT).show();

        SharedPreferences sharedPref = getSharedPreferences("bootConfig.pref", Context.MODE_PRIVATE);
        ;
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean("isBoot", true);
        editor.commit();
    }


    public void onStopButtonClicked(View v) {
        Intent intent = new Intent(this, ScreenService.class);
        stopService(intent);
        Toast.makeText(this, "stopService", Toast.LENGTH_SHORT).show();

        SharedPreferences sharedPref = getSharedPreferences("bootConfig.pref", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean("isBoot", false);
        editor.commit();
    }
}
