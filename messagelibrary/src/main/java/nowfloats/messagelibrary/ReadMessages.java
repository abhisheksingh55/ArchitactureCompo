package nowfloats.messagelibrary;

import android.app.IntentService;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.LinearLayout;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

/**
 * Created by Admin on 01-02-2017.
 */

public class ReadMessages extends IntentService {
    private static final int MEAASGE_LOADER_ID = 221;
    private static final Uri MESSAGE_URI = Uri.parse("content://sms/");
    private static final int READ_MESSAGES_ID = 221 ;
    private String[] projections=new String[]{"_id","date","address","body","seen"};//\"VM-INDMRT\", \"TM-JustDl\", \"VM-Quikrr\"

    private String selection=" address Like \"%INDMRT%\" or address Like \"%JustDl%\" or address Like \"%VM-Quikrr%\"";
    RecyclerView recyclerView;
    LinearLayout linearLayout;
    private String order="date DESC";
    private static final String DATABASE_NAME="FpTag";
    MessageListModel messageListModel;
    MessageAdapter adapter;
    private ArrayList<MessageListModel.SmsMessage> messageList;



    public ReadMessages() {
        super("");
        messageListModel = MessageListModel.getInstance();
        messageList= new ArrayList<>();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        readMessage();
        sendDataToFirebase(mDatabase);
    }
    private void sendDataToFirebase(DatabaseReference mDatabase ) {
        Log.v("ggg","running2");
        messageListModel.setDatabase(messageList.size());
        mDatabase.child(DATABASE_NAME).setValue(messageListModel);/*, new DatabaseReference.CompletionListener() {
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
}
