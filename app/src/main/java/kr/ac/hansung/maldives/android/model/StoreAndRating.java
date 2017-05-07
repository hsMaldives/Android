package kr.ac.hansung.maldives.android.model;

/**
 * Created by jeeyoung on 2017-05-04.
 */

public class StoreAndRating {

    private Integer store_idx;
    private Float rating[];

    public void setStore_idx(Integer store_idx) {
        this.store_idx = store_idx;
    }

    public Integer getStore_idx() {
        return store_idx;
    }

    public void setRating(Float[] rating) {
        this.rating = rating;
    }

    public Float[] getRating() {
        return rating;
    }
}
