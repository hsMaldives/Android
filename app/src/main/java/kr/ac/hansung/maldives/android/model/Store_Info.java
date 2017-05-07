package kr.ac.hansung.maldives.android.model;

/**
 * Created by jeeyoung on 2017-04-30.
 */

public class Store_Info {

    private int store_Idx;
    private String name;
    private int code;
    private double latitude;
    private double longitude;
    private String address;
    private boolean mSelectable = true;

    public void setStore_Idx(int store_Idx) {
        this.store_Idx = store_Idx;
    }

    public int getStore_Idx() {
        return store_Idx;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getAddress() {
        return address;
    }

    public boolean isSelectable() {
        return mSelectable;
    }

    public void setSelectable(boolean mSelectable) {
        this.mSelectable = mSelectable;
    }
}
