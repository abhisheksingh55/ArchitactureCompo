package nowfloats.messagelibrary;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/**
 * Created by Admin on 1/14/2017.
 */

public class ShowMessagesActivity extends AppCompatActivity implements View.OnClickListener {


    private static final int MEAASGE_LOADER_ID = 221;
    private static final Uri MESSAGE_URI = Uri.parse("content://sms/");
    private static final int READ_MESSAGES_ID = 221 ;
    private String[] projections=new String[]{"_id","date","address","body","seen"};

    private String selection=" address Like \"%WAYSMS%\" or address Like \"%INDMRT%\" or address Like \"%JustDl%\" or address Like \"%VM-Quikrr%\"";
    RecyclerView recyclerView;
    LinearLayout linearLayout;
    private static String DATABASE_NAME="FpId_",MOBILE_ID,MESSAGES="messages";
    private String order="date DESC";
    MessageListModel messageListModel;
    MessageAdapter adapter;
    private ArrayList<MessageListModel.SmsMessage> messageList;


    // this is the first method called
    //here we have to initialized the widget before use
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

        // for offine storing data
        //always it be the first line before use firebase database reference
        /*FirebaseDatabase.getInstance().setPersistenceEnabled(true);*/

        MOBILE_ID = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        addListener();
        getPermission();
    }

    private void getPermission(){
        // check read sms permission

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS)== PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS)== PackageManager.PERMISSION_GRANTED){
            /*getSupportLoaderManager().initLoader(MEAASGE_LOADER_ID,null,this);*/

            // start the service to send data to firebase
            Intent intent = new Intent(this, ReadMessages.class);
            intent.putExtra(DATABASE_NAME,DATABASE_NAME);
            startService(intent);
        }
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            // if user deny the permissions
            if(shouldShowRequestPermissionRationale(Manifest.permission.READ_SMS)){
                Snackbar.make(linearLayout, R.string.required_permission_to_show, Snackbar.LENGTH_INDEFINITE)
                        .setAction(R.string.enable, this)  // action text on the right side of snackbar
                        .setActionTextColor(ContextCompat.getColor(this,android.R.color.holo_green_light))
                        .show();
            }
            else {
                // Requesting permissions by user
                requestPermissions(new String[]{Manifest.permission.READ_SMS,Manifest.permission.RECEIVE_SMS},READ_MESSAGES_ID);
            }
        }
    }

    // this method called when user react on permissions
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode==READ_MESSAGES_ID && grantResults.length>0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED){
            /*getSupportLoaderManager().initLoader(MEAASGE_LOADER_ID,null,this);*/

            // if he grant the permissions
            Intent intent = new Intent(this, ReadMessages.class);
            intent.putExtra(DATABASE_NAME,DATABASE_NAME);
            startService(intent);
        }
    }


    @Override
    public void onClick(View v) {
        // after click on action button of snackbar
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.READ_SMS,Manifest.permission.RECEIVE_SMS},READ_MESSAGES_ID);
        }
    }
    // add the listener with firebase database
    private void addListener(){
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
        mDatabase.child(DATABASE_NAME+MESSAGES).child(MOBILE_ID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                ArrayList<MessageListModel.SmsMessage> modelList = new ArrayList<MessageListModel.SmsMessage>();
                for(DataSnapshot dataSnapshot1:dataSnapshot.getChildren()){
                    Log.v("ggg","hey");
                    modelList.add(dataSnapshot1.getValue(MessageListModel.SmsMessage.class));
                }

                Log.v("ggg","size "+modelList.size());
                if (modelList.size()==0){
                    Snackbar.make(linearLayout,R.string.contact_empty,Snackbar.LENGTH_LONG).show();
                }
                adapter = new MessageAdapter(modelList);
                recyclerView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Snackbar.make(linearLayout,databaseError.getMessage(),Snackbar.LENGTH_LONG).show();
            }
        });
    }
}