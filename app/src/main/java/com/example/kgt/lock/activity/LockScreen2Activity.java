package com.example.kgt.lock.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.Toast;

import com.example.kgt.lock.R;
import com.example.kgt.lock.adapter.RatingAdapter;
import com.example.kgt.lock.service.LocationService;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Iterator;

import javax.net.ssl.HttpsURLConnection;

public class LockScreen2Activity extends AppCompatActivity {


    private float[] rating;//점수들
    private RatingAdapter ratingAdapter;
    private LocationService locationService;

    private String[] names = {"맛","친절","청결"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lockscreen2);

        setListViewAdapter();
        //checkOverlayPermissions();

        //위치사용허용 -> 다운받았을 때 처음화면에 띄울 것
        checkDangerousPermissions();

        //위치정보 사용
       locationService.chkGpsService();

        //FLAG_SHOW_WHEN_LOCKED - 기본잠금보다 위에 띄워라
        //FLAG_DISSMISS_KEYGUARD - 안드로이드 기본 잠금화면을 없애라. (말을 잘 안듣는다-나중에 수정)
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                //WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                //WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                ,
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                //WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON|
                // WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        );

    }

    private void setListViewAdapter(){
        ListView listView = (ListView)findViewById(R.id.listView);
        ratingAdapter = new RatingAdapter(this, names);
        listView.setAdapter(ratingAdapter);

        rating = new float[ratingAdapter.getCount()];
    }

    public void checkDangerousPermissions() {
        String[] permissions = {
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        };

        int permissionCheck = PackageManager.PERMISSION_GRANTED;
        for(int i=0;i<permissions.length; i++) {
            permissionCheck = ContextCompat.checkSelfPermission(this, permissions[i]);
            if(permissionCheck == PackageManager.PERMISSION_DENIED) {
                break;
            }
        }

//        if(permissionCheck == PackageManager.PERMISSION_GRANTED) {
//            Toast.makeText(this, "권한 있음", Toast.LENGTH_LONG).show();
//        } else {
//            Toast.makeText(this, "권한 없음", Toast.LENGTH_LONG).show();
//
//            if(ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[0])) {
//                Toast.makeText(this, "권한 설명 필요함.", Toast.LENGTH_LONG).show();
//            } else {
//                ActivityCompat.requestPermissions(this, permissions, 1);
//            }
//        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 1) {
            for (int i = 0; i < permissions.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, permissions[i] + " 권한이 승인됨.", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, permissions[i] + " 권한이 승인되지 않음.", Toast.LENGTH_LONG).show();
                }
            }
        }
    }


    public class SendPost extends AsyncTask<String, Void, String> {
        protected void onPreExecute() {
        }

        protected String doInBackground(String... args) {
            try {
                URL url = new URL("http://192.168.0.56:8080/moldives");

                //json 객체화
                JSONObject info = new JSONObject();
                info.put("lati", locationService.getLati()); //위도
                info.put("longi", locationService.getLongi()); //경도
                for (int i = 0; i < rating.length; i++) {
                    info.put(Integer.toString(i), rating[i]); //별점들
                }

                Log.e("data", info.toString());

                //Http 연결 설정
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(15000);
                conn.setConnectTimeout(15000);
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);


                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                writer.write(getInfo(info));

                writer.flush();
                writer.close();
                os.close();

                int responseCode = conn.getResponseCode();

                if (responseCode == HttpsURLConnection.HTTP_OK) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                    StringBuffer sb = new StringBuffer("");
                    String line = "";

                    while ((line = in.readLine()) != null) {
                        sb.append(line);
                        break;
                    }
                    in.close();
                    return sb.toString();
                } else {
                    return new String("false : " + responseCode);
                }
            } catch (Exception e) {
                e.printStackTrace();
                return new String("Exception: " + e.getMessage());
            }
        }

        @Override
        protected void onPostExecute(String result) {
        //Toast.makeText(getApplicationContext(), result, Toast.LENGTH_LONG).show();
        }
    }

    public String getInfo(JSONObject info) throws Exception {
        StringBuilder result = new StringBuilder();
        boolean first = true;

        Iterator<String> itr = info.keys();

        while(itr.hasNext()) {
            String key = itr.next();
            Object value = info.get(key);

            if(first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(key, "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(value.toString(), "UTF-8"));
        }
        return result.toString();
    }


    public void onBeforeButtonClicked(View v){
        Intent intent = new Intent(this, LockScreenActivity.class);
        startActivity(intent);

        overridePendingTransition(R.anim.in_from_right,R.anim.out_to_left);
        finish();
    }

    public void onFinishButtonClicked(View v) {
        locationService.startLocationService();

        for (int i = 0; i < rating.length; i++) {
            RatingBar ratingBar = (RatingBar) ratingAdapter.getItem(i);
            rating[i] = ratingBar.getRating();
            Log.i("infos", "위도(lati)=" + locationService.getLati() + "," + "경도(longi)=" + locationService.getLongi() + "," + "infos=" + rating[i]);
        }

        new SendPost().execute();

        Intent intent = new Intent(this, PopUpActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);

        finish();
    }
}
