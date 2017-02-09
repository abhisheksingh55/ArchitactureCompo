package nowfloats.readmessage;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by Admin on 1/14/2017.
 */

public class MainActivity extends AppCompatActivity {
    private static String FP_ID="FpId_";
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(nowfloats.messagelibrary.R.layout.activity_show_message);
        Intent intent = new Intent(this,nowfloats.messagelibrary.ShowMessagesActivity.class);
        intent.putExtra(FP_ID,FP_ID);
        startActivity(intent);


    }
}
