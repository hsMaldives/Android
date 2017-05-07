package kr.ac.hansung.maldives.android.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jeeyoung on 2017-05-04.
 */

public class List_Store {

    private ArrayList<Store_Info> list_Store = new ArrayList<Store_Info>();

    public void setList_Store(List<Store_Info> list_Store) {
        this.list_Store = (ArrayList<Store_Info>) list_Store;
    }

    public List<Store_Info> getList_Store() {
        return list_Store;
    }
}
