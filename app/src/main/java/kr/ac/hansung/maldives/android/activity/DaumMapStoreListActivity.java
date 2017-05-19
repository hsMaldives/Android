package kr.ac.hansung.maldives.android.activity;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.daum.mf.map.api.CalloutBalloonAdapter;
import net.daum.mf.map.api.MapPOIItem;
import net.daum.mf.map.api.MapPoint;
import net.daum.mf.map.api.MapPointBounds;
import net.daum.mf.map.api.MapView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.CookieHandler;
import java.net.CookiePolicy;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import javax.net.ssl.HttpsURLConnection;

import kr.ac.hansung.maldives.android.R;
import kr.ac.hansung.maldives.android.adapter.TextListAdapter;
import kr.ac.hansung.maldives.android.daumMap.OnFinishSearchListener;
import kr.ac.hansung.maldives.android.daumMap.Searcher;
import kr.ac.hansung.maldives.android.model.List_Store;
import kr.ac.hansung.maldives.android.model.Locations;
import kr.ac.hansung.maldives.android.prop.DaumApiProp;
import kr.ac.hansung.maldives.android.proxy.WebkitCookieManagerProxy;
import kr.ac.hansung.maldives.model.DaumStoreItem;
import kr.ac.hansung.maldives.model.StoreAndRating;

import static android.view.WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED;

public class DaumMapStoreListActivity extends FragmentActivity implements MapView.MapViewEventListener, MapView.POIItemEventListener {

    private MapView mMapView;

    private ListView listView;
    private TextListAdapter textListAdapter;

    private Locations locations = new Locations();

    private DaumStoreItem selectedItem;

    private HashMap<Integer, DaumStoreItem> mTagItemMap = new HashMap<>();

    //매장 위치만 보내는지 저장
    private boolean locationOnlyflag = false;
    //알림사용여부
    private boolean notificationflag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daum_map_store_list);

        Button checkbtn = (Button) findViewById(R.id.check);
        Intent i = getIntent();
        //위치만 보내는지
        locationOnlyflag = i.getBooleanExtra("locationOnlyflag", locationOnlyflag);
        if (locationOnlyflag == true) {
            checkbtn.setText("완료");
        }

        SharedPreferences sharedPref = getSharedPreferences("notificationConfig.pref", Context.MODE_PRIVATE);
        notificationflag = (Boolean) sharedPref.getBoolean("notificationflag", true);

        //리스트뷰
        listView = (ListView) findViewById(R.id.listView);

        //리스트 어댑터
        textListAdapter = new TextListAdapter(this);

        listView.setAdapter(textListAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                DaumStoreItem curStore = (DaumStoreItem) textListAdapter.getItem(position);
                String storename = curStore.getTitle();
                String storeaddress = curStore.getAddress();

                mTagItemMap.get(curStore);
                mMapView.selectPOIItem(mMapView.getPOIItems()[position], true);
                selectedItem = curStore;

                Toast.makeText(getApplicationContext(), "Selected : " + storename + storeaddress, Toast.LENGTH_LONG).show();
            }
        });

        mMapView = (MapView) findViewById(R.id.map_view);
        mMapView.setDaumMapApiKey(DaumApiProp.DAUM_MAPS_ANDROID_APP_API_KEY);
        mMapView.setMapViewEventListener(this);
        mMapView.setPOIItemEventListener(this);
        mMapView.setCalloutBalloonAdapter(new CustomCalloutBalloonAdapter());

        startLocationService();

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                        FLAG_SHOW_WHEN_LOCKED
                //WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                //WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                ,
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                        FLAG_SHOW_WHEN_LOCKED
                //WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON|check
        );
    }

    Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            textListAdapter.notifyDataSetChanged();
        }
    };

    public void startLocationService() {
        // 위치 관리자 객체 참조
        LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        long minTime = 10000;
        float minDistance = 10;

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
    }

    private class GPSListener implements LocationListener {
        /**
         * 위치 정보가 확인될 때 자동 호출되는 메소드
         */
        public void onLocationChanged(Location location) {
            mMapView.setMapCenterPoint(MapPoint.mapPointWithGeoCoord(location.getLatitude(), location.getLongitude()), true);

//            Random random = new Random();
//            Location randomLocation = new Location("");
//            randomLocation.setLatitude(37.0 + (double)(random.nextInt(168630)+505168)/(double)1000000);
//            randomLocation.setLongitude(126 + (double)(random.nextInt(269000)+824849)/(double)1000000);
//            findStoreList(randomLocation);
            findStoreList(location);
        }

        public void onProviderDisabled(String provider) {
        }

        public void onProviderEnabled(String provider) {
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    }

    class CustomCalloutBalloonAdapter implements CalloutBalloonAdapter {

        private final View mCalloutBalloon;

        public CustomCalloutBalloonAdapter() {
            mCalloutBalloon = getLayoutInflater().inflate(R.layout.daum_map_callout_balloon, null);
        }

        @Override
        public View getCalloutBalloon(MapPOIItem poiItem) {
            if (poiItem == null) return null;
            DaumStoreItem item = mTagItemMap.get(poiItem.getTag());
            if (item == null) return null;
            ImageView imageViewBadge = (ImageView) mCalloutBalloon.findViewById(R.id.badge);
            TextView textViewTitle = (TextView) mCalloutBalloon.findViewById(R.id.title);
            textViewTitle.setText(item.getTitle());
            TextView textViewDesc = (TextView) mCalloutBalloon.findViewById(R.id.desc);
            textViewDesc.setText(item.getAddress());
            imageViewBadge.setImageDrawable(createDrawableFromUrl(item.getImageUrl()));
            return mCalloutBalloon;
        }

        @Override
        public View getPressedCalloutBalloon(MapPOIItem poiItem) {
            return null;
        }
    }

    protected void findStoreList(Location location) {
        double latitude = location.getLatitude(); // 위도
        double longitude = location.getLongitude(); // 경도
        int radius = 100; // 중심 좌표부터의 반경거리. 특정 지역을 중심으로 검색하려고 할 경우 사용. meter 단위 (0 ~ 10000)
        int page = 1; // 페이지 번호 (1 ~ 3). 한페이지에 15개
        String apikey = DaumApiProp.DAUM_MAPS_ANDROID_APP_API_KEY;

        Searcher searcher = new Searcher(); // net.daum.android.map.openapi.search.Searcher
        searcher.searchCategory(getApplicationContext(), "FD6", latitude, longitude, radius, page, apikey, new OnFinishSearchListener() {
            @Override
            public void onSuccess(List<DaumStoreItem> itemList) {
                mMapView.removeAllPOIItems(); // 기존 검색 결과 삭제
                showResult(itemList); // 검색 결과 보여줌
            }

            @Override
            public void onFail() {
                Toast.makeText(getApplicationContext(), "API_KEY의 제한 트래픽이 초과되었습니다.", Toast.LENGTH_SHORT);
            }
        });
    }

    private void showResult(List<DaumStoreItem> itemList) {
        MapPointBounds mapPointBounds = new MapPointBounds();

        textListAdapter.setList_store(new List_Store());

        for (int i = 0; i < itemList.size(); i++) {
            DaumStoreItem item = itemList.get(i);

            MapPOIItem poiItem = new MapPOIItem();
            poiItem.setItemName(item.getTitle());
            poiItem.setTag(i);
            MapPoint mapPoint = MapPoint.mapPointWithGeoCoord(item.getLatitude(), item.getLongitude());
            poiItem.setMapPoint(mapPoint);
            mapPointBounds.add(mapPoint);
            poiItem.setMarkerType(MapPOIItem.MarkerType.CustomImage);
            poiItem.setCustomImageResourceId(R.drawable.pin_blue2);
            poiItem.setSelectedMarkerType(MapPOIItem.MarkerType.CustomImage);
            poiItem.setCustomSelectedImageResourceId(R.drawable.pin_red2);
            poiItem.setCustomImageAutoscale(false);
            poiItem.setCustomImageAnchor(0.5f, 1.0f);
            poiItem.setUserObject(item);

            mMapView.addPOIItem(poiItem);
            mTagItemMap.put(poiItem.getTag(), item);

            textListAdapter.addItem(item);
        }

        handler.sendMessage(new Message());

    }

    private Drawable createDrawableFromUrl(String url) {
        try {
            InputStream is = (InputStream) this.fetch(url);
            Drawable d = Drawable.createFromStream(is, "src");
            return d;
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private Object fetch(String address) throws MalformedURLException, IOException {
        URL url = new URL(address);
        Object content = url.getContent();
        return content;
    }

    @Override
    public void onCalloutBalloonOfPOIItemTouched(MapView mapView, MapPOIItem mapPOIItem, MapPOIItem.CalloutBalloonButtonType calloutBalloonButtonType) {
    }

    @Override
    @Deprecated
    public void onCalloutBalloonOfPOIItemTouched(MapView mapView, MapPOIItem mapPOIItem) {
    }

    @Override
    public void onDraggablePOIItemMoved(MapView mapView, MapPOIItem mapPOIItem, MapPoint mapPoint) {
    }

    @Override
    public void onPOIItemSelected(MapView mapView, MapPOIItem mapPOIItem) {
        selectedItem = (DaumStoreItem) mapPOIItem.getUserObject();
    }

    @Override
    public void onMapViewInitialized(MapView mapView) {
        Log.i("DaumMap", "MapView had loaded. Now, MapView APIs could be called safely");

        mMapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeadingWithoutMapMoving);
    }

    @Override
    public void onMapViewCenterPointMoved(MapView mapView, MapPoint mapCenterPoint) {
    }

    @Override
    public void onMapViewDoubleTapped(MapView mapView, MapPoint mapPoint) {
    }

    @Override
    public void onMapViewLongPressed(MapView mapView, MapPoint mapPoint) {
    }

    @Override
    public void onMapViewSingleTapped(MapView mapView, MapPoint mapPoint) {
    }

    @Override
    public void onMapViewDragStarted(MapView mapView, MapPoint mapPoint) {
    }

    @Override
    public void onMapViewDragEnded(MapView mapView, MapPoint mapPoint) {
    }

    @Override
    public void onMapViewMoveFinished(MapView mapView, MapPoint mapPoint) {
//        findStoreList();
    }

    @Override
    public void onMapViewZoomLevelChanged(MapView mapView, int zoomLevel) {
    }


    private NotificationCompat.Builder createNotification() {
        Bitmap icon = BitmapFactory.decodeResource(getResources(), R.mipmap.whereyou);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.whereyou)
                .setLargeIcon(icon)
                .setContentTitle("미평가된 장소가 있네요?")
                .setContentText("평가할수록 정확도는 향상됩니다!!!")
                .setSmallIcon(R.mipmap.whereyou)
                .setAutoCancel(true)
                .setWhen(System.currentTimeMillis())
                .setDefaults(Notification.DEFAULT_ALL);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder.setCategory(Notification.CATEGORY_MESSAGE)
                    .setPriority(Notification.PRIORITY_HIGH)
                    .setVisibility(Notification.VISIBILITY_PUBLIC);
        }
        return builder;
    }
    //안드4.1?부터 새로운 노티피케이션 형식(일반적인 형식)
    public void showBasicNotification() {
        NotificationCompat.Builder mBuilder = createNotification();
        mBuilder.setContentIntent(createPendingIntent());

        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(1, mBuilder.build());
    }

    /**
     * 노티피케이션을 누르면 실행되는 기능을 가져오는 노티피케이션
     * <p>
     * 실제 기능을 추가하는 것
     *
     * @return
     */
    private PendingIntent createPendingIntent() {
        Intent resultIntent = new Intent(this, MainAppActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainAppActivity.class);
        stackBuilder.addNextIntent(resultIntent);

        return stackBuilder.getPendingIntent(
                0,
                PendingIntent.FLAG_UPDATE_CURRENT
        );
    }

    private void goToLockScreen2() {
        Intent i = new Intent(this, LockScreen2Activity.class);

        i.putExtra("StoreInfo", selectedItem);
        startActivity(i);

        //왼쪽에서 들어오고 오른쪽으로 나간다.(-> 슬라이드)
        overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);

        finish();
    }

    public void onCheckButtonClicked(View v) {
        if (selectedItem == null) {
            Toast.makeText(this, "매장을 선택해 주세요", Toast.LENGTH_SHORT).show();
            return;
        }
        if (locationOnlyflag == true) {
            handler = new Handler() {
                public void handleMessage(Message msg) {
                    textListAdapter.notifyDataSetChanged();

                    if (notificationflag)
                        showBasicNotification();
                    //@@gt 예상되는 버그 -> 버튼을 누를 때마다 알림은 큐에 쌓이므로 계속해서 notification을 발생시킬 것이다. (퍼포먼스문제)
                }
            };
            handler.sendEmptyMessageDelayed(0, 5000);
            finish();
        } else {
            goToLockScreen2();
        }
    }


    public void onTempButtonClicked(View v){
        Random random = new Random();

        for(int i=0;i<textListAdapter.getCount();i++) {
            StoreAndRating storeAndRating= new StoreAndRating();

            storeAndRating.setStoreInfo((DaumStoreItem)textListAdapter.getItem(i));
            storeAndRating.setRating(new Float[]{random.nextInt(11)/(float)2,random.nextInt(11)/(float)2,random.nextInt(11)/(float)2,random.nextInt(11)/(float)2,random.nextInt(11)/(float)2});

            new SendPost(storeAndRating).execute();
        }
    }

    public class SendPost extends AsyncTask<String, Void, String> {

        private StoreAndRating storeAndRating;

        public SendPost(StoreAndRating storeAndRating){
            this.storeAndRating = storeAndRating;
        }
        protected void onPreExecute() {
        }

        protected String doInBackground(String... args) {
            try {
                CookieManager.getInstance().setAcceptCookie(true);
                WebkitCookieManagerProxy coreCookieManager = new WebkitCookieManagerProxy(null, CookiePolicy.ACCEPT_ALL);
                CookieHandler.setDefault(coreCookieManager);

                URL url = new URL("http://223.194.145.81/WhereYou/api/rating/storeAndRatingInfo");
//                URL url = new URL("http://192.168.0.170:8080/WhereYou/api/rating/storeAndRatingInfo");
                //json 객체화
                Gson gson = new GsonBuilder().create();

                String storeAndRatingJson = gson.toJson(storeAndRating);

                //Http 연결 설정
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(15000);
                conn.setConnectTimeout(15000);
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/json");

                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                writer.write(storeAndRatingJson);

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
}