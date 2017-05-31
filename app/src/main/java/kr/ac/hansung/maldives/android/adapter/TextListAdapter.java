package kr.ac.hansung.maldives.android.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import kr.ac.hansung.maldives.android.activity.IconTextView;
import kr.ac.hansung.maldives.android.model.List_Store;
import kr.ac.hansung.maldives.model.DaumStoreItem;

/**
 * Created by jeeyoung on 2017-05-05.
 */

public class TextListAdapter extends BaseAdapter {

    private Context mContext;
    private List_Store list_store = new List_Store();

    public TextListAdapter(Context context) {
        mContext = context;
    }

    public void addItem(DaumStoreItem store_info) {
        list_store.getList_Store().add(store_info);
    }

    public void setList_store(List_Store list_store) {
        this.list_store = list_store;
    }

    @Override
    public int getCount() {
        return list_store.getList_Store().size();
    }

    public Object getItem(int position) {
        return list_store.getList_Store().get(position);
    }

    public boolean areAllItemSelectable() {
        return false;
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        IconTextView itemView;
        if(convertView==null) {
            itemView = new IconTextView(mContext, list_store.getList_Store().get(position));
        } else {
            itemView = (IconTextView) convertView;

            itemView.setText(0, list_store.getList_Store().get(position).getTitle());
            itemView.setText(1, list_store.getList_Store().get(position).getAddress());
        }

        return itemView;
    }
}
