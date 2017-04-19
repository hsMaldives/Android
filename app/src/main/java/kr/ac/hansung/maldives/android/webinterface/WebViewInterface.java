package kr.ac.hansung.maldives.android.webinterface;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.Toast;

import kr.ac.hansung.maldives.android.activity.SettingActivity;

public class WebViewInterface {

    private WebView mWebView;
    private Activity mContext;
    private final Handler handler = new Handler();


    public WebViewInterface(Activity activity, WebView view) {
        mWebView = view;
        mContext = activity;

    }

    @JavascriptInterface
    public void callSettingsActivity() {
        Toast.makeText(mContext, "settings in ...", Toast.LENGTH_LONG).show();

        Intent intent = new Intent(mContext, SettingActivity.class);
        mContext.startActivity(intent);

    }
}
