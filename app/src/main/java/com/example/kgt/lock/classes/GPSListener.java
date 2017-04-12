package com.example.kgt.lock.classs;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.example.kgt.lock.model.LocationAndRating;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;

/**
 * Created by jeeyoung on 2017-04-12.
 */

public class GPSListener extends AppCompatActivity implements LocationListener {

//    private double lati; //위도
//    private double longi; //경도

    private LocationAndRating locationAndRating = new LocationAndRating();

    public LocationAndRating getLocationAndRating(){
        return locationAndRating;
    }
    public GoogleMap map;

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

        showCurrentLocation(locationAndRating.getLati(), locationAndRating.getLongi(), map);

        //Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
    }

    public void onProviderDisabled(String provider) {
    }

    public void onProviderEnabled(String provider) {
    }

    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    public void startLocationService() {
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

//                Toast.makeText(getApplicationContext(), "Last Known Location : " + "Latitude : " + latitude + "\nLongitude:" + longitude, Toast.LENGTH_LONG).show();
            }
        } catch (SecurityException ex) {
            ex.printStackTrace();
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

    //현재 위치를 지도위에 표시
    private void showCurrentLocation(Double lati, Double longi, GoogleMap gMap) {

        map=gMap;
        LatLng cutPoint = new LatLng(lati, longi);

        map.animateCamera(CameraUpdateFactory.newLatLngZoom(cutPoint, 15));

        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        map.setMapType(GoogleMap.MAP_TYPE_SATELLITE);

    }
}