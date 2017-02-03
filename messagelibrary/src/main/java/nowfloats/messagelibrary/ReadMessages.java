package nowfloats.messagelibrary;

import android.app.Service;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/**
 * Created by Admin on 01-02-2017.
 */

public class ReadMessages extends Service {

    private static final Uri MESSAGE_URI = Uri.parse("content://sms/");
    private String[] projections=new String[]{"_id","date","address","body","seen"};
    private String selection=" address Like \"%WAYSMS%\" or address Like \"%INDMRT%\" or address Like \"%JustDl%\" or address Like \"%VM-Quikrr%\"";
    private String order="date DESC";
    private static final String DATABASE_NAME="FpTag";

    MessageListModel messageListModel;
    private ArrayList<MessageListModel.SmsMessage> messageList;
    private PowerManager.WakeLock wakeLock;


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
                wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                        "MyWakelockTag");
                wakeLock.acquire();

                DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
                addListener(mDatabase);
                readMessage();
                sendDataToFirebase(mDatabase);
            }
        }).start();
        return Service.START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        messageListModel = MessageListModel.getInstance();
        messageList= new ArrayList<>();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

private void addListener(DatabaseReference mDatabase){
    mDatabase.addValueEventListener(new ValueEventListener() {

        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            Log.v("ggg","changed");
            MessageListModel model = dataSnapshot.child(DATABASE_NAME).getValue(MessageListModel.class);

            if (model==null){
                Log.v("ggg","messages are empty");
                return;
            }
            Log.v("ggg","stopping");
            wakeLock.release();
            stopSelf();
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            Log.v("ggg","error in access messages");
        }
    });
}
    private void sendDataToFirebase(DatabaseReference mDatabase ) {
        Log.v("ggg","running2");
        messageListModel.setDatabase(messageList.size());
        mDatabase.child(DATABASE_NAME).setValue(messageListModel);
        /*, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                Log.v("ggg", "error on completion" + databaseError);
            }
        }*/

        Log.v("ggg","running3");
    }

    private void readMessage(){
        ContentResolver resolver = getContentResolver();
        Cursor cursor = resolver.query(MESSAGE_URI,projections,selection,null,order);
            if(cursor!=null && cursor.moveToFirst()){

                messageList.clear();
                MessageListModel.SmsMessage message;
                do{

               /*for(int i=0;i<data.getColumnCount();i++){
                   Log.v("ggg"+i,data.getString(i)+" "+data.getColumnName(i));
               }*/
                    message = MessageListModel.SmsMessage.getInstance()
                            .setId(cursor.getLong(0))
                            .setDate(cursor.getLong(1))
                            .setSubject(cursor.getString(2))
                            .setBody(cursor.getString(3))
                            .setSeen(cursor.getString(4));
                    messageList.add(message);
                    Log.v("ggg",message.toString());
                }while(cursor.moveToNext());
                cursor.close();
                messageListModel.setMessageList(messageList);
        }
        Log.v("ggg","running1");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.v("ggg","destroy");
    }
}
