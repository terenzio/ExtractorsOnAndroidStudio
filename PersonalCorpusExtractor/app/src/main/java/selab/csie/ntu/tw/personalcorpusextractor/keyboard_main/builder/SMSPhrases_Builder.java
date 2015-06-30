package selab.csie.ntu.tw.personalcorpusextractor.keyboard_main.builder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;

import selab.csie.ntu.tw.personalcorpusextractor.ExtractorSelector;
import selab.csie.ntu.tw.personalcorpusextractor.R;

/**
 * Created by CarsonWang on 2015/6/17.
 */
public class SMSPhrases_Builder implements Phrases_Builder {
    private static SMSPhrases_Builder smsPhrases_Builder;
    private final String fileName = "BagOfWordSMS";


    private List<SMSData> smsList;
    private static String messageData;
    private static int count = 0;


    public static SMSPhrases_Builder getMultiInstance(){
        smsPhrases_Builder = new SMSPhrases_Builder();
        return smsPhrases_Builder;
    }
    private SMSPhrases_Builder(){
        handle();
    }

    private void handle(){
        smsList = new ArrayList<>();

        Uri uri = Uri.parse("content://sms/inbox");
        Cursor c= ExtractorSelector.getInstance().getContentResolver().query(uri, null, null ,null,null);
        ExtractorSelector.getInstance().startManagingCursor(c);

        // Read the sms data and store it in the list
        if(c.moveToFirst()) {
            for(int i=0; i < c.getCount(); i++) {
                SMSData sms = new SMSData();
                sms.setBody(c.getString(c.getColumnIndexOrThrow("body")).toString());
                sms.setNumber(c.getString(c.getColumnIndexOrThrow("address")).toString());
                smsList.add(sms);

                c.moveToNext();
            }
        }
        c.close();

        // Set smsList in the ListAdapter
//        setListAdapter(new ListAdapter(this, smsList));
        getResult();
    }

    private class SMSData {

        // Number from witch the sms was send
        private String number;
        // SMS text body
        private String body;

        public String getNumber() {
            return number;
        }

        public void setNumber(String number) {
            this.number = number;
        }

        public String getBody() {
            return body;
        }

        public void setBody(String body) {
            this.body = body;
        }

    }


    public Phrases_Product getResult(){
        AlertDialog.Builder dialog = new AlertDialog.Builder(ExtractorSelector.getInstance());
        dialog.setTitle("File Request");
        if (!smsList.isEmpty()) {
            if (isExternalStorageWritable()) {
                for(SMSData sms : smsList)
                    messageData += sms.getBody() + "\n";
                writeToFile(fileName+String.valueOf(count)+".txt", messageData);
                count++;
                dialog.setMessage("Write successfully!");
            } else dialog.setMessage("Write fail!");
        } else  dialog.setMessage("Write fail!");
        dialog.setPositiveButton(R.string.ok_label,
                new DialogInterface.OnClickListener() {
                    public void onClick(
                            DialogInterface dialoginterface, int i) {
                    }
                });
        dialog.show();
        return null;
    }
    //Checks if external storage is available for read and write
    private boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    //Write file to external storage
    private void writeToFile(String fileName, String data){
        //Create the directory for the user's public pictures directory
        String path = Environment.getExternalStorageDirectory().getPath();
        File dir = new File(path + "/SMSExtractor");
//        File dir = new File(path + "/");
        if (!dir.exists()){
            dir.mkdir();
        }
        try {
            File file = new File(path + "/SMSExtractor/" + fileName);
//            File file = new File(path + "/" + fileName);
            FileOutputStream fout = new FileOutputStream(file);
            fout.write(data.getBytes());
            fout.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
