package com.example.kgt.lock.activity;

import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.kgt.lock.R;
import com.example.kgt.lock.model.LocationAndRating;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import static com.example.kgt.lock.R.id.map;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    Geocoder gcoder;
    MarkerOptions locationMarker;
    TextView textView;

    private LocationAndRating locationAndRating = new LocationAndRating();

    public LocationAndRating getLocationAndRating() {
        return locationAndRating;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        textView = (TextView) findViewById(R.id.address);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(map);
        mapFragment.getMapAsync(this);

        gcoder = new Geocoder(this, Locale.KOREA);
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
                locationAndRating.setLati(lastLocation.getLatitude());
                locationAndRating.setLongi(lastLocation.getLongitude());
                showCurrentLocation(lastLocation);
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
            locationAndRating.setLati(location.getLatitude());
            //경도(세로선)
            locationAndRating.setLongi(location.getLongitude());

            String msg = "Latitude : " + locationAndRating.getLati() + "\nLongitude:" + locationAndRating.getLongi();
            Log.i("LocationListener", msg);

            showCurrentLocation(location);
        }

        public void onProviderDisabled(String provider) {
        }

        public void onProviderEnabled(String provider) {
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    }

    //현재 위치를 지도위에 표시
    private void showCurrentLocation(Location location) {
        LatLng cutPoint = new LatLng(location.getLatitude(), location.getLongitude());

        if (mMap != null) {
            mMap.moveCamera(CameraUpdateFactory.newLatLng(cutPoint));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(cutPoint, 15));
            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

            showMyLocationMarker(location);
//            searchLocation(location);
        } else {
            Toast.makeText(this, "map is null", Toast.LENGTH_LONG).show();
        }
    }

    //현재 위치에 marker표시
    private void showMyLocationMarker(Location location) {

        List<Address> addressList = null;

        try {
            addressList = gcoder.getFromLocation(location.getLatitude(), location.getLongitude(), 3);

            if (addressList != null) {
                for (int i = 0; i < addressList.size(); i++) {
                    Address outAddr = addressList.get(i);
                    int addrCount = outAddr.getMaxAddressLineIndex() + 1;
                    StringBuffer outAddrStr = new StringBuffer();
                    for (int k = 0; k < addrCount; k++) {
                        outAddrStr.append(outAddr.getAddressLine(k));
                    }

                    if (locationMarker == null) {
                        locationMarker = new MarkerOptions();
                        locationMarker.position(new LatLng(location.getLatitude(), location.getLongitude()));
                        locationMarker.title("내 위치\n");
                        locationMarker.snippet(outAddrStr.toString());
                        locationMarker.icon(BitmapDescriptorFactory.fromResource(R.drawable.mylocation));
                        mMap.addMarker(locationMarker);
                    } else {
                        locationMarker.position(new LatLng(location.getLatitude(), location.getLongitude()));
                    }
                }
            }
        } catch (IOException e) {
            Log.d("excep", "예외 : " + e.toString());
        }
    }

//    private void searchLocation(Location location) {
//        List<Address> addressList = null;
//
//        try {
//            addressList = gcoder.getFromLocation(location.getLatitude(), location.getLongitude(), 3);
//
//            if(addressList != null) {
//                for (int i = 0; i < addressList.size(); i++) {
//                    Address outAddr = addressList.get(i);
//                    int addrCount = outAddr.getMaxAddressLineIndex() + 1;
//                    StringBuffer outAddrStr = new StringBuffer();
//                    for (int k = 0; k < addrCount; k++) {
//                        outAddrStr.append(outAddr.getAddressLine(k));
//                    }
//
//                    textView.append("\n\tAddress #" + i + " : " + outAddrStr.toString());
//                }
//            }
//        } catch (IOException e) {
//            Log.d("excep", "예외 : " + e.toString());
//        }
//    }

    private void goToLockScreen2() {
        Intent i = new Intent(this, LockScreen2Activity.class);
        //intent.addFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(i);

        //왼쪽에서 들어오고 오른쪽으로 나간다.(-> 슬라이드)
        overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);

        finish();

    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mMap != null)
            mMap.setMyLocationEnabled(false);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mMap != null)
            mMap.setMyLocationEnabled(true);
    }

    public void onCheckButtonClicked(View v) {
        goToLockScreen2();
    }

    public void onNoCheckButtonClicked(View v) {

    }
}
