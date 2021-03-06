package kr.ac.hansung.maldives.android.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import kr.ac.hansung.maldives.android.service.ScreenService;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        if(intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)){
            Intent i = new Intent(context, ScreenService.class);

            SharedPreferences sharedPref = context.getSharedPreferences("bootConfig.pref",Context.MODE_PRIVATE);
            boolean isBoot = sharedPref.getBoolean("isBoot", true);

            if(isBoot) {
                context.startService(i);
            }
        }

    }
}
