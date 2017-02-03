package nowfloats.messagelibrary;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.provider.Telephony;
import android.telephony.SmsMessage;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import static android.content.Context.POWER_SERVICE;

/**
 * Created by Admin on 01-02-2017.
 */

public class SmsReceiver extends BroadcastReceiver {

    private static final String DATABASE_NAME="FpTag";
    PowerManager.WakeLock wakeLock;
    @Override
    public void onReceive(Context context, Intent intent) {
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        PowerManager powerManager = (PowerManager) context.getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                "MyWakelockTag");
        wakeLock.acquire();
        SmsMessage[] sms = Telephony.Sms.Intents.getMessagesFromIntent(intent);
        MessageListModel.SmsMessage model;
        for (SmsMessage ms:sms) {
            model =  MessageListModel.SmsMessage.getInstance()
                    .setBody(ms.getMessageBody())
                    .setSubject(ms.getOriginatingAddress())
                    .setDate(System.currentTimeMillis());
            Log.v("ggg","\n"+ms.getOriginatingAddress()+"\n"+ms.getProtocolIdentifier()
            +"\n"+ms.getTimestampMillis());
            addListener(mDatabase,model);
        }

    }
    private void addListener(final DatabaseReference mDatabase, final MessageListModel.SmsMessage sms){
        mDatabase.addValueEventListener(new ValueEventListener() {
            boolean flag=false;
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(!flag) {
                    MessageListModel model = dataSnapshot.child(DATABASE_NAME).getValue(MessageListModel.class);

                    if (model==null){
                        Log.v("ggg","messages are empty");
                        return;
                    }

                    sms.setId(model.getDatabase());
                    mDatabase.child(DATABASE_NAME).child("messageList").child(String.valueOf(model.getDatabase())).setValue(sms);
                    mDatabase.child(DATABASE_NAME).child("database").setValue(model.getDatabase() + 1);
                    flag=true;
                    wakeLock.release();
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.v("ggg","error in access messages");
            }
        });
    }
}
