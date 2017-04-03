package com.example.kgt.lock.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.example.kgt.lock.R;

public class LockScreen2Activity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lockscreen2);

        setListViewAdapter();
    }



    private String[] names = {"맛","친절","청결"};

    private void setListViewAdapter(){
        class RatingAdapter extends BaseAdapter {

            private Context context;
            private String[] names;

            public RatingAdapter(Context context,String[] names){
                this.context = context;
                this.names = names;
            }

            @Override
            public int getCount() {
                return names.length;
            }

            @Override
            public Object getItem(int i) {
                return names[i];
            }

            @Override
            public long getItemId(int i) {
                return i;
            }

            @Override
            public View getView(int i, View view, ViewGroup viewGroup) {
                if(view == null){
                    LayoutInflater inflater = LayoutInflater.from(context);
                    view = inflater.inflate(R.layout.item_list,viewGroup,false);
                }
                TextView textView = (TextView)view.findViewById(R.id.ratingTextView);
                RatingBar ratingBar = (RatingBar)view.findViewById(R.id.ratingBar);

                textView.setText((String)getItem(i));

                return view;
            }
        }

        ListView listView = (ListView)findViewById(R.id.listView);
        listView.setAdapter(new RatingAdapter(this,names));
    }


    public void onBeforeButtonClicked(View v){
        Intent intent = new Intent(this, LockScreenActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.in_from_right,R.anim.out_to_left);
        finish();
    }

    public void onFinishButtonClicked(View v){



        //gps정보 + ratingBar 점수들 {맛=5,청결=3.5, 서비스=2}



        finish();
    }
}
