package kr.ac.hansung.maldives.android.activity;

import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.daum.mf.map.api.CalloutBalloonAdapter;
import net.daum.mf.map.api.CameraUpdateFactory;
import net.daum.mf.map.api.MapPOIItem;
import net.daum.mf.map.api.MapPoint;
import net.daum.mf.map.api.MapPointBounds;
import net.daum.mf.map.api.MapView;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

import kr.ac.hansung.maldives.android.R;
import kr.ac.hansung.maldives.android.adapter.TextListAdapter;
import kr.ac.hansung.maldives.android.daumMap.OnFinishSearchListener;
import kr.ac.hansung.maldives.android.daumMap.Searcher;
import kr.ac.hansung.maldives.android.model.DaumStoreItem;
import kr.ac.hansung.maldives.android.model.List_Store;
import kr.ac.hansung.maldives.android.model.Locations;
import kr.ac.hansung.maldives.android.model.Store_Info;
import kr.ac.hansung.maldives.android.prop.DaumApiProp;

import static android.view.WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED;

public class DaumMapStoreListActivity extends FragmentActivity implements MapView.MapViewEventListener, MapView.POIItemEventListener {

    private MapView mMapView;

    private ListView listView;
    private TextListAdapter textListAdapter;

    private Locations locations = new Locations();
    private Store_Info store_info = new Store_Info();
    private List_Store list_store = new List_Store();

    private Integer selectedNum;

    private HashMap<Integer, DaumStoreItem> mTagItemMap = new HashMap<>();

    Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            textListAdapter.notifyDataSetChanged();
            //@@gt 예상되는 버그 -> 버튼을 누를 때마다 알림은 큐에 쌓이므로 계속해서 notification을 발생시킬 것이다. (퍼포먼스문제)
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daum_map_store_list);

        //리스트뷰
        listView = (ListView) findViewById(R.id.listView);

        //리스트 어댑터
        textListAdapter = new TextListAdapter(this);

        listView.setAdapter(textListAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                DaumStoreItem curStore = (DaumStoreItem) textListAdapter.getItem(position);
                String storename = curStore.title;
                String storeaddress = curStore.address;
                selectedNum = position;

                mTagItemMap.get(curStore);
                mMapView.selectPOIItem(mMapView.getPOIItems()[position], true);

                Toast.makeText(getApplicationContext(), "Selected : " + storename + storeaddress, Toast.LENGTH_LONG).show();
            }
        });

        mMapView = (MapView)findViewById(R.id.map_view);
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

        public void startLocationService() {
        // 위치 관리자 객체 참조
        LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        long minTime = 1000;
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
            textViewTitle.setText(item.title);
            TextView textViewDesc = (TextView) mCalloutBalloon.findViewById(R.id.desc);
            textViewDesc.setText(item.address);
            imageViewBadge.setImageDrawable(createDrawableFromUrl(item.imageUrl));
            return mCalloutBalloon;
        }

        @Override
        public View getPressedCalloutBalloon(MapPOIItem poiItem) {
            return null;
        }

    }

    protected void findStoreList(Location location){
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

        for (int i = 0; i < itemList.size(); i++) {
            DaumStoreItem item = itemList.get(i);

            MapPOIItem poiItem = new MapPOIItem();
            poiItem.setItemName(item.title);
            poiItem.setTag(i);
            MapPoint mapPoint = MapPoint.mapPointWithGeoCoord(item.latitude, item.longitude);
            poiItem.setMapPoint(mapPoint);
            mapPointBounds.add(mapPoint);
            poiItem.setMarkerType(MapPOIItem.MarkerType.CustomImage);
            poiItem.setCustomImageResourceId(R.drawable.map_pin_blue);
            poiItem.setSelectedMarkerType(MapPOIItem.MarkerType.CustomImage);
            poiItem.setCustomSelectedImageResourceId(R.drawable.map_pin_red);
            poiItem.setCustomImageAutoscale(false);
            poiItem.setCustomImageAnchor(0.5f, 1.0f);

            mMapView.addPOIItem(poiItem);
            mTagItemMap.put(poiItem.getTag(), item);

            textListAdapter.addItem(item);
        }

        handler.sendMessage(new Message());

    }

    public void onCheckButtonClicked(View v) {
        goToLockScreen2();
    }

    private void goToLockScreen2() {
        Intent i = new Intent(this, LockScreen2Activity.class);

        i.putExtra("StoreInfo", list_store.getList_Store().get(selectedNum));
        startActivity(i);

        //왼쪽에서 들어오고 오른쪽으로 나간다.(-> 슬라이드)
        overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);

        finish();
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

    private Object fetch(String address) throws MalformedURLException,IOException {
        URL url = new URL(address);
        Object content = url.getContent();
        return content;
    }

    @Override
    public void onCalloutBalloonOfPOIItemTouched(MapView mapView, MapPOIItem mapPOIItem, MapPOIItem.CalloutBalloonButtonType calloutBalloonButtonType) {
        DaumStoreItem item = mTagItemMap.get(mapPOIItem.getTag());
        StringBuilder sb = new StringBuilder();
        sb.append("title=").append(item.title).append("\n");
        sb.append("imageUrl=").append(item.imageUrl).append("\n");
        sb.append("address=").append(item.address).append("\n");
        sb.append("newAddress=").append(item.newAddress).append("\n");
        sb.append("zipcode=").append(item.zipcode).append("\n");
        sb.append("phone=").append(item.phone).append("\n");
        sb.append("category=").append(item.category).append("\n");
        sb.append("longitude=").append(item.longitude).append("\n");
        sb.append("latitude=").append(item.latitude).append("\n");
        sb.append("distance=").append(item.distance).append("\n");
        sb.append("direction=").append(item.direction).append("\n");
        Toast.makeText(this, sb.toString(), Toast.LENGTH_SHORT).show();
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
    }

    @Override
    public void onMapViewInitialized(MapView mapView) {
        Log.i("DaumMap", "MapView had loaded. Now, MapView APIs could be called safely");

//        mMapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeading);
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

}
