package kr.ac.hansung.maldives.android.model;

/**
 * Created by KGT on 2017-04-12.
 */


public class LocationAndRating {
    private double lati;
    private double longi;

    private float rating[];

    public double getLati() {
        return lati;
    }

    public void setLati(double lati) {
        this.lati = lati;
    }

    public double getLongi() {
        return longi;
    }

    public void setLongi(double longi) {
        this.longi = longi;
    }

    public float[] getRating() {
        return rating;
    }

    public void setRating(float[] rating) {
        this.rating = rating;
    }
}
