package kr.ac.hansung.maldives.android.activity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.widget.ListView;
import android.widget.RatingBar;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.CookieHandler;
import java.net.CookiePolicy;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import kr.ac.hansung.maldives.android.adapter.RatingAdapter;
import kr.ac.hansung.maldives.android.model.DaumStoreItem;
import kr.ac.hansung.maldives.android.model.Locations;
import kr.ac.hansung.maldives.android.model.StoreAndRating;
import kr.ac.hansung.maldives.android.model.Store_Info;
import kr.ac.hansung.maldives.android.proxy.WebkitCookieManagerProxy;

public class LockScreen2Activity extends AppCompatActivity {

    private StoreAndRating storeAndRating = new StoreAndRating();
    private Locations locations = new Locations();
    private Store_Info store_info = new Store_Info();
    private RatingAdapter ratingAdapter;

    private String[] names = {"맛", "친절", "청결"};
    private DaumStoreItem storeInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(kr.ac.hansung.maldives.android.R.layout.lockscreen2);

        Intent i = getIntent();

        DaumStoreItem storeInfo = (DaumStoreItem) i.getSerializableExtra("StoerInfo");
        storeAndRating.setStoreInfo(storeInfo);

        setListViewAdapter();

        //FLAG_SHOW_WHEN_LOCKED - 기본잠금보다 위에 띄워라
        //FLAG_DISSMISS_KEYGUARD - 안드로이드 기본 잠금화면을 없애라. (말을 잘 안듣는다-나중에 수정)
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                //WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                //WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                ,
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                //WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON|
                // WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        );

    }

    private void setListViewAdapter() {
        ListView listView = (ListView) findViewById(kr.ac.hansung.maldives.android.R.id.listView);
        ratingAdapter = new RatingAdapter(this, names);
        listView.setAdapter(ratingAdapter);

        storeAndRating.setRating(new Float[ratingAdapter.getCount()]);
    }


    public class SendPost extends AsyncTask<String, Void, String> {
        protected void onPreExecute() {
        }

        protected String doInBackground(String... args) {
            try {
                CookieManager.getInstance().setAcceptCookie(true);
                WebkitCookieManagerProxy coreCookieManager = new WebkitCookieManagerProxy(null, CookiePolicy.ACCEPT_ALL);
                CookieHandler.setDefault(coreCookieManager);

                URL url = new URL("http://223.194.145.81:80/WhereYou/api/rating/storeAndRatingInfo");

                //json 객체화
                Gson gson = new GsonBuilder().create();

                String storeAndRatingJson = gson.toJson(storeAndRating);

                //Http 연결 설정
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(15000);
                conn.setConnectTimeout(15000);
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/json");

                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                writer.write(storeAndRatingJson);

                writer.flush();
                writer.close();
                os.close();

                int responseCode = conn.getResponseCode();

                if (responseCode == HttpsURLConnection.HTTP_OK) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                    StringBuffer sb = new StringBuffer("");
                    String line = "";

                    while ((line = in.readLine()) != null) {
                        sb.append(line);
                        break;
                    }
                    in.close();
                    return sb.toString();
                } else {
                    return new String("false : " + responseCode);
                }
            } catch (Exception e) {
                e.printStackTrace();
                return new String("Exception: " + e.getMessage());
            }
        }

        @Override
        protected void onPostExecute(String result) {
            //Toast.makeText(getApplicationContext(), result, Toast.LENGTH_LONG).show();
        }
    }


    public void onBeforeButtonClicked(View v) {
        Intent intent = new Intent(this, DaumMapStoreListActivity.class);
        startActivity(intent);

        overridePendingTransition(kr.ac.hansung.maldives.android.R.anim.in_from_right, kr.ac.hansung.maldives.android.R.anim.out_to_left);
        finish();
    }

    public void onFinishButtonClicked(View v) {
        for (int i = 0; i < storeAndRating.getRating().length; i++) {
            RatingBar ratingBar = (RatingBar) ratingAdapter.getItem(i);
            storeAndRating.getRating()[i] = ratingBar.getRating();
            Log.i("infos", "위도(lati)=" + locations.getLati() + "," + "경도(longi)=" + locations.getLongi() + "," + "infos=" + storeAndRating.getRating()[i]);
        }

        new SendPost().execute();

        Intent intent = new Intent(this, PopUpActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);

        finish();
    }
}
