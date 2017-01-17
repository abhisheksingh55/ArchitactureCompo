package nowfloats.messagelibrary;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Admin on 1/16/2017.
 */

public class MessageListModel {

    public ArrayList<SmsMessage> messageList;

    public static MessageListModel getInstance(){
        return new MessageListModel();
    }
    public MessageListModel(){
    }
    public ArrayList<SmsMessage> getArrayList(){
        return messageList;
    }

    public void setMessageList(ArrayList<SmsMessage> messageList) {
        this.messageList = messageList;
    }

    static class SmsMessage{
        public SmsMessage(){

        }
         public static SmsMessage getInstance(){
             return new SmsMessage();
         }
        private String body,subject,seen;
        private long date;
        private long id;
        private List<String> list;

        public List<String> getList() {
            return list;
        }

        public SmsMessage setList(List<String> list) {
           ArrayList<String>a= new ArrayList<String>();
            a.add("a");
            a.add("b");
            this.list = a;
            return this;
        }

        SmsMessage setBody(String body){
            this.body=body;
            return this;
        }
         SmsMessage setDate(long date){
            this.date=date;
            return this;
        }
         SmsMessage setSubject(String subject){
            this.subject=subject;
            return this;
        }
         SmsMessage setSeen(String seen){
            this.seen=seen;
            return this;
        }
         SmsMessage setId(long id){
            this.id=id;
            return this;
        }
        public String getBody(){
            return body;
        }

        public long getId() {
            return id;
        }

        public long getDate() {
            return date;
        }

        public String getSeen() {
            return seen;
        }

        public String getSubject() {
            return subject;
        }

        @Override
        public String toString() {
            return String.valueOf(id)+", "+date+", "+seen+", "+subject+", "+body;
        }
    }

}
