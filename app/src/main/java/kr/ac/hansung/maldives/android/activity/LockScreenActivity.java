package kr.ac.hansung.maldives.android.activity;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.widget.SeekBar;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.CookieHandler;
import java.net.CookiePolicy;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import kr.ac.hansung.maldives.android.R;
import kr.ac.hansung.maldives.android.model.Locations;
import kr.ac.hansung.maldives.android.proxy.WebkitCookieManagerProxy;

import static android.view.WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED;

public class LockScreenActivity extends AppCompatActivity {

    //    private HomeKeyLocker homeKeyLoader;
    private Locations locations = new Locations();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lockscreen);

        customSeekBar();
        //customDate();

        //FLAG_SHOW_WHEN_LOCKED - 기본잠금보다 위에 띄워라
        //FLAG_DISSMISS_KEYGUARD - 안드로이드 기본 잠금화면을 없애라. (말을 잘 안듣는다-나중에 수정)
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                        FLAG_SHOW_WHEN_LOCKED
                //WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                //WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                ,
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                        FLAG_SHOW_WHEN_LOCKED
                //WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON|
                // WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        );

//        homeKeyLoader = new HomeKeyLocker();
//        homeKeyLoader.lock(this);
    }


    Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            showBasicNotification();
            //@@gt 예상되는 버그 -> 버튼을 누를 때마다 알림은 큐에 쌓이므로 계속해서 notification을 발생시킬 것이다. (퍼포먼스문제)
        }
    };

    private void goToMapView() {
        Intent i = new Intent(this, MapsActivity.class);
        //intent.addFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
        //i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);

        //왼쪽에서 들어오고 오른쪽으로 나간다.(-> 슬라이드)
        overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);

        finish();
    }


    private void customSeekBar() {
        final SeekBar seekBar = (SeekBar) findViewById(R.id.seekBar);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (seekBar.getProgress() >= 80) {
//                    homeKeyLoader.unlock();
                    Log.d("seekbar", "80이상");
                    goToMapView();
                } else {
                    seekBar.setProgress(0);
                }
            }
        });

        seekBar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    seekBar.setProgress(0);
                }
                return false;
            }
        });

        final Resources res = getResources();

        seekBar.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                if (seekBar.getHeight() > 0) {
                    Drawable thumb = res.getDrawable(R.drawable.ic_next);
                    int h = seekBar.getMeasuredHeight();
                    int w = h;
                    Bitmap bmpOrg = ((BitmapDrawable) thumb).getBitmap();
                    Bitmap bmpScaled = Bitmap.createScaledBitmap(bmpOrg, w, h, true);
                    Drawable newThumb = new BitmapDrawable(res, bmpScaled);
                    newThumb.setBounds(0, 0, newThumb.getIntrinsicWidth(), newThumb.getIntrinsicHeight());
                    seekBar.setThumb(newThumb);
                    seekBar.getViewTreeObserver().removeOnPreDrawListener(this);
                }
                return true;
            }
        });
    }

    public void startLocationService() {
        // 위치 관리자 객체 참조
        LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        long minTime = 1000000000;
        float minDistance = 0;

        try {
            // GPS를 이용한 위치 요청
            manager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    minTime,
                    minDistance,
                    new GPSListener());

            // 위치 확인이 안되는 경우에도 최근에 확인된 위치 정보 먼저 확인
            Location lastLocation = manager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (lastLocation != null) {
                locations.setLati(lastLocation.getLatitude());
                locations.setLongi(lastLocation.getLongitude());
            }

            // 네트워크를 이용한 위치 요청
            manager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    minTime,
                    minDistance,
                    new GPSListener());

        } catch (SecurityException ex) {
            ex.printStackTrace();
        }

        new sendLocation().execute();
    }

    private class GPSListener implements LocationListener {
        /**
         * 위치 정보가 확인될 때 자동 호출되는 메소드
         */
        public void onLocationChanged(Location location) {
            //위도(가로선)
            locations.setLati(location.getLatitude());
            //경도(세로선)
            locations.setLongi(location.getLongitude());

            String msg = "Latitude : " + locations.getLati() + "\nLongitude:" + locations.getLongi();
            Log.i("LocationListener", msg);
        }

        public void onProviderDisabled(String provider) {
        }

        public void onProviderEnabled(String provider) {
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    }

    public class sendLocation extends AsyncTask<String, Void, String> {

        protected void onPreExecute() {
        }

        protected String doInBackground(String... args) {
            try {
                CookieManager.getInstance().setAcceptCookie(true);
                WebkitCookieManagerProxy coreCookieManager = new WebkitCookieManagerProxy(null, CookiePolicy.ACCEPT_ALL);
                CookieHandler.setDefault(coreCookieManager);

                URL url = new URL("http://223.194.145.81:80/WhereYou/api/rating/onlyLocationInfo");

                //json 객체화
                Gson gson = new GsonBuilder().create();
                String locationJson = gson.toJson(locations);

                //Http 연결 설정
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(15000);
                conn.setConnectTimeout(15000);
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/json");

                String cookie;

                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                writer.write(locationJson);

                writer.flush();
                writer.close();
                os.close();

                int responseCode = conn.getResponseCode();
                BufferedInputStream bis = null;

                if (responseCode == HttpsURLConnection.HTTP_OK) {
                    bis = new BufferedInputStream(conn.getInputStream());
                    BufferedReader in = new BufferedReader(new InputStreamReader(bis, "UTF-8"));

                    StringBuffer sb = new StringBuffer("");
                    String line = "";

                    while ((line = in.readLine()) != null) {
                        sb.append(line).append("\n");
                    }
                    bis.close();
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
        }
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

    public void onSkipButtonClicked(View v) {
        finish();
    }


    public void onNextButtonClicked(View v) {
        startLocationService();
        handler.sendEmptyMessageDelayed(0,10000);   //일정시간(ms) 지연시킨다. (1000ms = 1s)
        //animation 추가
        //
        //
        finish();
    }

}