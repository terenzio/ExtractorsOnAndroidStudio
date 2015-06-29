package selab.csie.ntu.tw.personalcorpusextractor.keyboard_main.builder;

import java.util.ArrayList;
import java.util.List;

import android.app.ListActivity;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.content.Context;

import selab.csie.ntu.tw.personalcorpusextractor.ExtractorSelector;

/**
 * Created by CarsonWang on 2015/6/17.
 */
public class SMSPhrases_Builder implements Phrases_Builder {
    private static SMSPhrases_Builder smsPhrases_Builder;
    private final String fileName = "BagOfWordSMS";
    public static SMSPhrases_Builder getMultiInstance(){
        smsPhrases_Builder = new SMSPhrases_Builder();
        return smsPhrases_Builder;
    }
    private SMSPhrases_Builder(){
    }
//
//    private void handle(){
//        List<SMSData> smsList = new ArrayList<SMSData>();
//
//        Uri uri = Uri.parse("content://sms/inbox");
//        Cursor c= ExtractorSelector.getInstance().getContentResolver().query(uri, null, null ,null,null);
//        ExtractorSelector.getInstance().startManagingCursor(c);
//
//        // Read the sms data and store it in the list
//        if(c.moveToFirst()) {
//            for(int i=0; i < c.getCount(); i++) {
//                SMSData sms = new SMSData();
//                sms.setBody(c.getString(c.getColumnIndexOrThrow("body")).toString());
//                sms.setNumber(c.getString(c.getColumnIndexOrThrow("address")).toString());
//                smsList.add(sms);
//
//                c.moveToNext();
//            }
//        }
//        c.close();
//
//        // Set smsList in the ListAdapter
////        ExtractorSelector.getInstance().setListAdapter(new ListAdapter(this, smsList));
//    }

//    private class SMSData {
//
//        // Number from witch the sms was send
//        private String number;
//        // SMS text body
//        private String body;
//
//        public String getNumber() {
//            return number;
//        }
//
//        public void setNumber(String number) {
//            this.number = number;
//        }
//
//        public String getBody() {
//            return body;
//        }
//
//        public void setBody(String body) {
//            this.body = body;
//        }
//
//    }
//    private class ListAdapter extends ArrayAdapter<SMSData> {
//
//        // List context
//        private final Context context;
//        // List values
//        private final List<SMSData> smsList;
//
//        public ListAdapter(Context context, List<SMSData> smsList) {
//            super(context, R.layout.activity_main, smsList);
//            this.context = context;
//            this.smsList = smsList;
//        }
//
//        @Override
//        public View getView(int position, View convertView, ViewGroup parent) {
//            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//
//            View rowView = inflater.inflate(R.layout.activity_main, parent, false);
//
//            TextView senderNumber = (TextView) rowView.findViewById(R.id.smsNumberText);
//            senderNumber.setText(smsList.get(position).getNumber());
//
//            return rowView;
//        }
//
//    }

    public Phrases_Product getResult(){
        return null;
    }
}
