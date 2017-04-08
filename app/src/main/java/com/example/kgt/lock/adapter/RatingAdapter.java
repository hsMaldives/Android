package com.example.kgt.lock.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RatingBar;
import android.widget.TextView;

import com.example.kgt.lock.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by KGT on 2017-04-05.
 */

public class RatingAdapter extends BaseAdapter{

        private Context context;
        private String[] names;
        private List<RatingBar> ratingBarList;

        public RatingAdapter(Context context, String[] names){
            this.context = context;
            this.names = names;
            ratingBarList = new ArrayList<>();
        }

        @Override
        public int getCount() {
            return names.length;
        }

        @Override
        public Object getItem(int i) {
            return ratingBarList.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {

            if(view == null) {
                LayoutInflater inflater = LayoutInflater.from(context);
                view = inflater.inflate(R.layout.item_list, viewGroup, false);

                TextView textView = (TextView)view.findViewById(R.id.ratingTextView);
                textView.setText(names[i]);
                RatingBar ratingBar = (RatingBar)view.findViewById(R.id.ratingBar);
                ratingBarList.add(ratingBar);
            }

            return view;
        }

}
