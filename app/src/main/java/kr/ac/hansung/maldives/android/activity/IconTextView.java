package kr.ac.hansung.maldives.android.activity;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;


import kr.ac.hansung.maldives.model.DaumStoreItem;
import kr.ac.hansung.maldives.android.R;
import kr.ac.hansung.maldives.android.model.Store_Info;

/**
 * Created by jeeyoung on 2017-05-06.
 */

public class IconTextView extends LinearLayout{

    private TextView storename;
    private TextView storeaddress;

    public IconTextView(Context context, DaumStoreItem store_info) {
        super(context);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.liststore, this, true);

        storename = (TextView) findViewById(R.id.storename);
        storename.setText(store_info.getTitle());

        storeaddress = (TextView) findViewById(R.id.storeaddress);
        storeaddress.setText(store_info.getAddress());
    }

    public void setText(int index, String data) {
        if(index == 0) {
            storename.setText(data);
        } else if(index==1) {
            storeaddress.setText(data);
        } else {
            throw new IllegalArgumentException();
        }
    }
}
