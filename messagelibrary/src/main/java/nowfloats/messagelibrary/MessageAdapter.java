package nowfloats.messagelibrary;

import android.icu.text.SimpleDateFormat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by Admin on 1/16/2017.
 */
public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MyViewHolder>{

    private final List<MessageListModel.SmsMessage> messageList;

    MessageAdapter(List<MessageListModel.SmsMessage> messageList){
        this.messageList=messageList;
    }
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_message_item,parent,false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        if(holder==null) return;
        String pattern ="MM-dd-yyyy, HH:mm";
        holder.body.setText(messageList.get(position).getBody());
        holder.subject.setText(messageList.get(position).getSubject());
      /*  Calendar calendar= Calendar.getInstance();
        calendar.setTimeZone(TimeZone.getDefault());
        calendar.setTimeInMillis(messageList.get(position).getDate());*/
        //Date date=calendar.getTime();
        SimpleDateFormat formatter = new SimpleDateFormat(pattern, Locale.getDefault());
        formatter.setTimeZone(android.icu.util.TimeZone.getDefault());
        Date date=new Date(messageList.get(position).getDate());
        holder.date.setText(formatter.format(date));
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder{

        TextView body,subject,date;
        MyViewHolder(View itemView) {
            super(itemView);
            body = (TextView) itemView.findViewById(R.id.tv_body);
            subject = (TextView) itemView.findViewById(R.id.tv_subject);
            date = (TextView) itemView.findViewById(R.id.tv_date);
        }
    }
}
