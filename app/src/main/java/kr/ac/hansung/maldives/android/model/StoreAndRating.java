package kr.ac.hansung.maldives.android.model;

/**
 * Created by jeeyoung on 2017-05-04.
 */

public class StoreAndRating {

    private DaumStoreItem storeInfo;
    private Float rating[];

    public void setStoreInfo(DaumStoreItem storeInfo){
        this.storeInfo = storeInfo;
    }

    public DaumStoreItem getStoreInfo(){
        return storeInfo;
    }

    public void setRating(Float[] rating) {
        this.rating = rating;
    }

    public Float[] getRating() {
        return rating;
    }
}
