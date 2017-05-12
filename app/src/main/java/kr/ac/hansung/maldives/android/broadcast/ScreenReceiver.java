package kr.ac.hansung.maldives.android.broadcast;

import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

import kr.ac.hansung.maldives.android.activity.LockScreenActivity;

public class ScreenReceiver extends BroadcastReceiver {

    //바로 화면이 꺼졌을 때 ACTION_SCREEN_OFF-Intent를 받음, 참고로 화면이 켜질 때는 ACTION_SCREEN_ON 이라는 Intent가 broadcast 됩니다. ACTION_SCREEN_OFF 를 받으면 위에서 만든 LockScreenActivity를 띄우면 됩니다.

    private KeyguardManager km = null;
//    private KeyguardManager.KeyguardLock keyLock = null;

    private TelephonyManager telephonyManager = null;
    private  boolean isPhoneIdle = true;

    @Override
    public void onReceive(Context context, Intent intent) {

        if(intent.getAction().equals(Intent.ACTION_SCREEN_OFF)){
            if(km == null)
                km = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);

//            if(keyLock == null)
//                keyLock = km.newKeyguardLock(Context.KEYGUARD_SERVICE);

            if(telephonyManager == null){
                telephonyManager = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
                telephonyManager.listen(phoneListener, PhoneStateListener.LISTEN_CALL_STATE);
            }

            if(isPhoneIdle) {
                disableKeyguard();

                Intent i = new Intent(context, LockScreenActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // Activity에서 startActivity하는게 아니므로 넣어야 에러 안남.
                context.startActivity(i);//그런데 !! 이렇게 한다고 화면을 껏다 키면 만든 Activity가 나타나질 않습니다. . . ㅎㄷㄷ 이유인 즉슨 ACTION_SCREEN_OFF 요녀석은 .. Manifest에 등록한다고 그냥 받을 수 있는 놈이 아닙니다. ㅠㅠ 따로 서비스를 구현해서 동적으로 BroadcastReceiver를 등록해줘야 합니다.
            }
        }
    }

    //기본 잠금화면 나타내기
    public void reenableKeyguard() {
//        keyLock.reenableKeyguard();
    }

    //기본 잠금화면 없애기
    public void disableKeyguard(){
//        keyLock.disableKeyguard();
    }


    private PhoneStateListener phoneListener = new PhoneStateListener(){
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            super.onCallStateChanged(state, incomingNumber);

            switch (state){
                case TelephonyManager.CALL_STATE_IDLE:
                    isPhoneIdle = true;
                    break;
                case TelephonyManager.CALL_STATE_RINGING:
                    isPhoneIdle = false;
                    break;
                case TelephonyManager.CALL_STATE_OFFHOOK:
                    isPhoneIdle = false;
                    break;
            }

        }
    };


}
