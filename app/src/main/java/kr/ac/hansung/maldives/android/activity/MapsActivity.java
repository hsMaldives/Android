package kr.ac.hansung.maldives.android.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import kr.ac.hansung.maldives.android.R;
import kr.ac.hansung.maldives.android.adapter.TextListAdapter;
import kr.ac.hansung.maldives.android.model.List_Store;
import kr.ac.hansung.maldives.android.model.Locations;
import kr.ac.hansung.maldives.android.model.Store_Info;

import static android.view.WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED;
import static kr.ac.hansung.maldives.android.R.id.map;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    GoogleMap mMap;
    //    Geocoder gcoder;
    MarkerOptions locationMarker;
    ListView listView;
    TextListAdapter textListAdapter;

    private Locations locations = new Locations();
    private Store_Info store_info = new Store_Info();
    private List_Store list_store = new List_Store();

    private Integer selectedNum;

    private Store_Info store1 = new Store_Info();
    private Store_Info store2 = new Store_Info();
    private Store_Info store3 = new Store_Info();
    private Store_Info store4 = new Store_Info();
    private Store_Info store5 = new Store_Info();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        //리스트뷰
        listView = (ListView) findViewById(R.id.listView);

        //리스트 어댑터
        textListAdapter = new TextListAdapter(this);

        store1.setStore_Idx(1);
        store1.setName("성북동 참치");
        store1.setCode(353);
        store1.setLatitude(37.590317);
        store1.setLongitude(127.004676);
        store1.setAddress("서울특별시 성북구 성북동 184-69");

        store2.setStore_Idx(2);
        store2.setName("맥도날드 삼선");
        store2.setCode(372);
        store2.setLatitude(37.589418);
        store2.setLongitude(127.007530);
        store2.setAddress("서울특별시 성북구 동소문동1가 32-3");

        store3.setStore_Idx(3);
        store3.setName("국시집");
        store3.setCode(235);
        store3.setLatitude(37.588846);
        store3.setLongitude(127.004407);
        store3.setAddress("서울특별시 성북구 성북동1가 9");

        store4.setStore_Idx(4);
        store4.setName("subway 대학로점");
        store4.setCode(302);
        store4.setLatitude(37.584627);
        store4.setLongitude(127.003548);
        store4.setAddress("서울특별시 종로구 동숭동 동숭길 101");

        store5.setStore_Idx(5);
        store5.setName("한성대학교");
        store5.setCode(492);
        store5.setLatitude(37.581865);
        store5.setLongitude(127.010311);
        store5.setAddress("서울특별시 성북구 삼선동2가 389");

        list_store.getList_Store().add(store1);
        list_store.getList_Store().add(store2);
        list_store.getList_Store().add(store3);
        list_store.getList_Store().add(store4);
        list_store.getList_Store().add(store5);

        for (int i = 0; i < list_store.getList_Store().size(); i++) {
            textListAdapter.addItem(list_store.getList_Store().get(i));
        }

        listView.setAdapter(textListAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Store_Info curStore = (Store_Info) textListAdapter.getItem(position);
                String storename = curStore.getName();
                String storeaddress = curStore.getAddress();
                selectedNum = position;
                showCurrentLocation(list_store.getList_Store().get(selectedNum).getLatitude(),list_store.getList_Store().get(selectedNum).getLongitude());

                Toast.makeText(getApplicationContext(), "Selected : " + storename + storeaddress, Toast.LENGTH_LONG).show();
            }
        });

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(map);
        mapFragment.getMapAsync(this);

//        gcoder = new Geocoder(this, Locale.KOREA);

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
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        startLocationService();
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

                new sendLocation().execute();
//                showCurrentLocation(lastLocation);
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
            //위도(가로선)
            locations.setLati(location.getLatitude());
            //경도(세로선)
            locations.setLongi(location.getLongitude());

            String msg = "Latitude : " + locations.getLati() + "\nLongitude:" + locations.getLongi();
            Log.i("LocationListener", msg);

            new sendLocation().execute();
//            showCurrentLocation(location);
        }

        public void onProviderDisabled(String provider) {
        }

        public void onProviderEnabled(String provider) {
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    }

    //현재 위치를 지도위에 표시
    private void showCurrentLocation(Double lati, Double longi) {
        LatLng cutPoint = new LatLng(lati, longi);

        if (mMap != null) {
            mMap.moveCamera(CameraUpdateFactory.newLatLng(cutPoint));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(cutPoint, 15));
            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

            showMyLocationMarker(lati, longi);
        } else {
            Toast.makeText(this, "map is null", Toast.LENGTH_LONG).show();
        }
    }

    //현재 위치에 marker표시
    private void showMyLocationMarker(Double lati, Double longi) {

        if (list_store != null) {
            if (locationMarker == null) {
                locationMarker = new MarkerOptions();
                locationMarker.position(new LatLng(lati, longi));
                locationMarker.title(list_store.getList_Store().get(0).getName());
                locationMarker.snippet(list_store.getList_Store().get(0).getAddress());
                locationMarker.icon(BitmapDescriptorFactory.fromResource(R.drawable.mylocation));
                mMap.addMarker(locationMarker);
            } else {
                mMap.clear();
                locationMarker.position(new LatLng(lati, longi));
                locationMarker.title(list_store.getList_Store().get(selectedNum).getName());
                locationMarker.snippet(list_store.getList_Store().get(selectedNum).getAddress());
                locationMarker.icon(BitmapDescriptorFactory.fromResource(R.drawable.mylocation));
                mMap.addMarker(locationMarker);
            }
        }
    }

    public class sendLocation extends AsyncTask<String, Void, String> {

        protected void onPreExecute() {
        }

        protected String doInBackground(String... args) {
            try {
                URL url = new URL("http://223.194.145.81:80/WhereYou/api/rating/locationInfo");
                CookieManager cookieManager = CookieManager.getInstance();

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

                conn.setRequestProperty("Cookie", cookieManager.getCookie(url.toString()));

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

//                    Type listType = new TypeToken<List_Store>(){}.getType();
                    list_store = gson.fromJson(sb.toString(), List_Store.class);

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
//            for(int i=0; i<list_store.getList_Store().size(); i++) {
//                        textListAdapter.addItem(list_store.getList_Store().get(i));
//                    }
//                    listView.setAdapter(textListAdapter);
            showCurrentLocation(list_store.getList_Store().get(0).getLatitude(),list_store.getList_Store().get(0).getLongitude());
        }

    }

    private void goToLockScreen2() {
        Intent i = new Intent(this, LockScreen2Activity.class);

        i.putExtra("store_idx", list_store.getList_Store().get(selectedNum).getStore_Idx());
        startActivity(i);

        //왼쪽에서 들어오고 오른쪽으로 나간다.(-> 슬라이드)
        overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);

        finish();
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mMap != null)
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
        mMap.setMyLocationEnabled(false);
    }

    //
    @Override
    protected void onResume() {
        super.onResume();

        if (mMap != null) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                return;
            }
            mMap.setMyLocationEnabled(true);
        }
    }

    public void onCheckButtonClicked(View v) {
        goToLockScreen2();
    }

    public void onNoCheckButtonClicked(View v) {
        startLocationService();
    }
}
