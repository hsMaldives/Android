package kr.ac.hansung.maldives.android.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jeeyoung on 2017-05-04.
 */

public class List_Store {

    private ArrayList<DaumStoreItem> list_Store = new ArrayList<DaumStoreItem>();

    public void setList_Store(List<DaumStoreItem> list_Store) {
        this.list_Store = (ArrayList<DaumStoreItem>) list_Store;
    }

    public List<DaumStoreItem> getList_Store() {
        return list_Store;
    }
}
