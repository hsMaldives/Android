package com.example.kgt.lock.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;

import com.example.kgt.lock.R;
import com.example.kgt.lock.service.LocationService;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;

/**
 * Created by jeeyoung on 2017-04-12.
 */

public class MapViewActivity extends FragmentActivity implements OnMapReadyCallback {

    private LocationService locationService;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mapview);

        SupportMapFragment mapFragment = (SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        locationService.startLocationService();

    }


    @Override
    public void onMapReady(GoogleMap googleMap) {

    }

    private void goToLockScreen2() {
        Intent i = new Intent(this, LockScreen2Activity.class);
        //intent.addFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(i);

        //왼쪽에서 들어오고 오른쪽으로 나간다.(-> 슬라이드)
        overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);

        finish();

    }

    public void onCheckButtonClicked(View v) {
        goToLockScreen2();
    }
}