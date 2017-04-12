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

import com.example.kgt.lock.model.LocationAndRating;
import com.example.kgt.lock.R;
import com.example.kgt.lock.adapter.RatingAdapter;
import com.example.kgt.lock.service.LocationService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

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

    private LocationAndRating locationAndRating = new LocationAndRating();
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

        locationAndRating.setRating(new float[ratingAdapter.getCount()]);
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

    private void startLocationService() {
        // 위치 관리자 객체 참조
        LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // 위치 정보를 받을 리스너 생성
        GPSListener gpsListener = new GPSListener();
        long minTime = 1000000000;
        float minDistance = 0;

        try {
            // GPS를 이용한 위치 요청
            manager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    minTime,
                    minDistance,
                    gpsListener);

            // 네트워크를 이용한 위치 요청
            manager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    minTime,
                    minDistance,
                    gpsListener);

            // 위치 확인이 안되는 경우에도 최근에 확인된 위치 정보 먼저 확인
            Location lastLocation = manager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (lastLocation != null) {
                locationAndRating.setLati(lastLocation.getLatitude());
                locationAndRating.setLongi(lastLocation.getLongitude());

                Toast.makeText(getApplicationContext(), "Last Known Location : " + "Latitude : " + locationAndRating.getLati() + "\nLongitude:" + locationAndRating.getLongi(), Toast.LENGTH_LONG).show();
            }
        } catch(SecurityException ex) {
            ex.printStackTrace();
        }

    }

    /**
     * 리스너 클래스 정의
     */
    private class GPSListener implements LocationListener {
        /**
         * 위치 정보가 확인될 때 자동 호출되는 메소드
         */
        public void onLocationChanged(Location location) {
            //위도(가로선)
            locationAndRating.setLati(location.getLatitude());
            //경도(세로선)
            locationAndRating.setLongi(location.getLongitude());

            String msg = "Latitude : "+ locationAndRating.getLati() + "\nLongitude:"+ locationAndRating.getLongi();
            Log.i("GPSListener", msg);

            Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
        }

        public void onProviderDisabled(String provider) {
        }

        public void onProviderEnabled(String provider) {
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

    }

    public class SendPost extends AsyncTask<String, Void, String> {
        protected void onPreExecute() {
        }

        protected String doInBackground(String... args) {
            try {
                URL url = new URL("http://192.168.0.53:8080/WhereYou/rating/test");

                //json 객체화
                Gson gson = new GsonBuilder().create();
                String loationAndRatingJson = gson.toJson(locationAndRating);

                //Http 연결 설정
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(15000);
                conn.setConnectTimeout(15000);
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type","application/json");


                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                writer.write(loationAndRatingJson);

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


    public void onBeforeButtonClicked(View v){
        Intent intent = new Intent(this, LockScreenActivity.class);
        startActivity(intent);

        overridePendingTransition(R.anim.in_from_right,R.anim.out_to_left);
        finish();
    }

    public void onFinishButtonClicked(View v) {
        locationService.startLocationService();

        for(int i=0;i<locationAndRating.getRating().length;i++) {
            RatingBar ratingBar = (RatingBar) ratingAdapter.getItem(i);
            locationAndRating.getRating()[i] = ratingBar.getRating();
            Log.i("infos", "위도(lati)=" + locationAndRating.getLati() + "," + "경도(longi)=" + locationAndRating.getLongi() + "," + "infos=" + locationAndRating.getRating()[i]);
        }

        new SendPost().execute();

        Intent intent = new Intent(this, PopUpActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);

        finish();
    }
}
