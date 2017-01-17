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
    private String[] projections=new String[]{"_id","date","address","body","seen"};//\"VM-INDMRT\", \"TM-JustDl\", \"VM-Quikrr\"

    private String selection=" address Like \"%INDMRT%\" or address Like \"%JustDl%\" or address Like \"%VM-Quikrr%\"";
    RecyclerView recyclerView;
    LinearLayout linearLayout;
    private String order="date DESC";
    private static final String DATABASE_NAME="USER_MESSAGES";
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
                Snackbar.make(linearLayout, R.string.required_permission_to_show, Snackbar.LENGTH_INDEFINITE)
                        .setAction(R.string.enable, this)  // action text on the right side
                        .setActionTextColor(getResources().getColor(android.R.color.holo_green_light))
                        .show();
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
        return new CursorLoader(this,MESSAGE_URI,projections,selection,null,order);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.v("ggg","start");
        if(data!=null && data.moveToFirst()){

            messageList.clear();
            MessageListModel.SmsMessage message;
           do{

               /*for(int i=0;i<data.getColumnCount();i++){
                   Log.v("ggg"+i,data.getString(i)+" "+data.getColumnName(i));
               }*/
                message = MessageListModel.SmsMessage.getInstance()
                       .setId(data.getLong(0))
                       .setDate(data.getLong(1))
                       .setSubject(data.getString(2))
                       .setBody(data.getString(3))
                       .setSeen(data.getString(4));
               messageList.add(message);
               Log.v("ggg",message.toString());
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

        mDatabase.child(DATABASE_NAME).setValue(messageListModel);/*, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                Log.v("ggg", "error on completion" + databaseError);
            }
        }*/
    }
    private void addListener(){
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                MessageListModel model = dataSnapshot.child(DATABASE_NAME).getValue(MessageListModel.class);

                if (model==null || model.getDatabase()==null || model.getDatabase().isEmpty()){
                    Snackbar.make(linearLayout,R.string.contact_empty,Snackbar.LENGTH_LONG).show();
                    return;
                }
                ArrayList<MessageListModel.SmsMessage> modelList = model.getMessageList();
                if(modelList!=null) {
                    adapter = new MessageAdapter(modelList);
                    recyclerView.setAdapter(adapter);
                }else{
                    Snackbar.make(linearLayout,R.string.contact_empty,Snackbar.LENGTH_LONG).show();
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Snackbar.make(linearLayout,"error in access messages",Snackbar.LENGTH_LONG).show();
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
