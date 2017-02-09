package nowfloats.messagelibrary;

import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by Admin on 01-02-2017.
 */

public class ReadMessages extends Service {

    private static final Uri MESSAGE_URI = Uri.parse("content://sms/");
    private String[] projections=new String[]{"_id","date","address","body","seen"};
    private String selection=" address Like \"%WAYSMS%\" or address Like \"%INDMRT%\" or address Like \"%JustDl%\" or address Like \"%VM-Quikrr%\"";
    private String order="date DESC";
    private static String DATABASE_NAME="FpId_",MOBILE_ID,MESSAGES="messages",PHONE_IDS="phoneIds",DETAILS="details";


    //private PowerManager.WakeLock wakeLock;


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent.getStringExtra(DATABASE_NAME)!=null){
            DATABASE_NAME=intent.getStringExtra(DATABASE_NAME);
        }

        //MOBILE_ID = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        MOBILE_ID = tm.getDeviceId();
        new Thread(new Runnable() {
            @Override
            public void run() {
               /* PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
                wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                        "MyWakelockTag");
                wakeLock.acquire();*/
                FirebaseOptions options = new FirebaseOptions.Builder()
                        .setApiKey("AIzaSyCZttwA-_904e5i5dt8B0ngzBdYBiZJ5Ek")
                        .setApplicationId("1:1062918210318:android:0b779b0abb2b1ef6")
                        .setDatabaseUrl("https://readmessage-37a5e.firebaseio.com")
                        .build();
                FirebaseApp secondApp = null;
                try {
                    secondApp = FirebaseApp.getInstance("second app");
                }catch(Exception e) {
                        secondApp = FirebaseApp.initializeApp(getApplicationContext(), options, "second app");
                }
                FirebaseDatabase secondDatabase = FirebaseDatabase.getInstance(secondApp);
                DatabaseReference mDatabase = secondDatabase.getReference();
                readMessage(mDatabase);
                stopSelf();
            }
        }).start();
        return Service.START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void readMessage(DatabaseReference mDatabase){
        ContentResolver resolver = getContentResolver();
        Cursor cursor = resolver.query(MESSAGE_URI,projections,selection,null,order);
            if(cursor!=null && cursor.moveToFirst()){

                MessageListModel.PhoneIds phoneIds=new MessageListModel.PhoneIds();
                phoneIds.setDate(String.valueOf(System.currentTimeMillis()));
                phoneIds.setPhoneId(MOBILE_ID);
                DatabaseReference phoneIdRef = mDatabase.child(DATABASE_NAME+DETAILS).child(PHONE_IDS);
                phoneIdRef.child(MOBILE_ID).setValue(phoneIds);

                MessageListModel.SmsMessage message;
                DatabaseReference MessageIdRef = mDatabase.child(DATABASE_NAME+MESSAGES).child(MOBILE_ID);
                MessageIdRef.removeValue();
                do{

                    message = MessageListModel.SmsMessage.getInstance()
                            .setId(cursor.getLong(0))
                            .setDate(cursor.getLong(1))
                            .setSubject(cursor.getString(2))
                            .setBody(cursor.getString(3))
                            .setSeen(cursor.getString(4));

                    String key = MessageIdRef.push().getKey();
                    MessageIdRef.child(key).setValue(message);

                    Log.v("ggg",message.toString());

                }while(cursor.moveToNext());
                cursor.close();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.v("ggg","destroy");
    }
}
