package nowfloats.messagelibrary;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/**
 * Created by Admin on 1/14/2017.
 */

public class ShowMessagesActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>,View.OnClickListener {

    private static final int MEAASGE_LOADER_ID = 221;
    private static final Uri MESSAGE_URI = Uri.parse("content://sms/");
    private static final int READ_MESSAGES_ID = 221 ;
    private String[] projections=new String[]{"_id","date","address","body","seen"};
    RecyclerView recyclerView;
    LinearLayout linearLayout;
    private String order="date DESC";
    MessageListModel messageListModel;
    MessageAdapter adapter;
    private ArrayList<MessageListModel.SmsMessage> messageList;

    DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_message);
        recyclerView = (RecyclerView) findViewById(R.id.recycle_view);
        linearLayout = (LinearLayout) findViewById(R.id.parent_layout);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        messageListModel = MessageListModel.getInstance();
        messageList= new ArrayList<>();
        /*adapter =new MessageAdapter(messageList);
        recyclerView.setAdapter(adapter);*/
        getPermission();
        addListener();
    }
    private void getPermission(){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS)== PackageManager.PERMISSION_GRANTED){
            getSupportLoaderManager().initLoader(MEAASGE_LOADER_ID,null,this);
        }
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            if(shouldShowRequestPermissionRationale(Manifest.permission.READ_SMS)){
                Snackbar.make(linearLayout, R.string.required_permission_to_show, Snackbar.LENGTH_LONG)
                        .setAction(R.string.enable, this)  // action text on the right side
                        .setActionTextColor(getResources().getColor(android.R.color.holo_green_light))
                        .setDuration(10000).show();
            }
            else {
                requestPermissions(new String[]{Manifest.permission.READ_SMS},READ_MESSAGES_ID);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode==READ_MESSAGES_ID && grantResults.length>0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED){
            getSupportLoaderManager().initLoader(MEAASGE_LOADER_ID,null,this);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this,MESSAGE_URI,projections,null,null,order);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if(data!=null && data.moveToFirst()){
            messageList.clear();
         /*   int i=-1;
            for (String s:data.getColumnNames()){
                i++;
                Log.v("ggg",data.getColumnName(i)+" "+data.getString(i));
            }*/
            MessageListModel.SmsMessage message;
           do{
                message = MessageListModel.SmsMessage.getInstance()
                       .setId(data.getLong(0))
                       .setDate(data.getLong(1))
                       .setSubject(data.getString(2))
                       .setBody(data.getString(3))
                       .setSeen(data.getString(4));
               messageList.add(message);
           }while(data.moveToNext());

            messageListModel.setMessageList(messageList);
            //adapter.notifyDataSetChanged();
            sendDataToFirebase();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    private void sendDataToFirebase() {

        mDatabase.child("MessageList").setValue(messageListModel); /*new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                Log.v("ggg", "error on completion" + databaseError);
            }
        });*/
    }
    private void addListener(){
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<MessageListModel.SmsMessage> modelList = (ArrayList<MessageListModel.SmsMessage>) dataSnapshot.child("MessageList").child("arrayList").getValue();

                if (messageListModel==null) return;
                // messageList = messageListModel.getArrayList();
                if(modelList!=null) {

                    Log.v("ggg",modelList.size()+" "+modelList.get(0).getDate());
                   /* adapter = new MessageAdapter(modelList);
                    recyclerView.setAdapter(adapter);*/
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.v("ggg","error database firebase"+databaseError);
            }
        });
    }
    @Override
    public void onClick(View v) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.READ_SMS},READ_MESSAGES_ID);
        }
    }
}
