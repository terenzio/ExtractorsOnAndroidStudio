package selab.csie.ntu.tw.personalcorpusextractor.keyboard_main.builder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Properties;

import javax.mail.BodyPart;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Store;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import selab.csie.ntu.tw.personalcorpusextractor.ExtractorSelector;
import selab.csie.ntu.tw.personalcorpusextractor.R;

/**
 * Created by CarsonWang on 2015/6/17.
 */
public class EmailPhrases_Builder extends AsyncTask<Object, Object, Object> implements Phrases_Builder {
//    private static EmailPhrases_Builder emailPhrases_Builder;

    private final String fileName = "BagOfWordEmail";

    private String address="redmine.selab.centos@gmail.com";
    private String password="Selab305!";

    private String[] urls= {"http","ftp","www"};
    private boolean hasSpace ;
    private boolean hasNext ;
    private boolean hasHiven;
    private boolean hasApostrophe ;
    private boolean hasAt ;
    private boolean testing;
    private boolean isUrl ;

    private Session session;
    private Store store;

    protected Object doInBackground(Object... args) {
        try {
            Properties props = new Properties();
            props.put("mail.imap.ssl.enable", "true"); // required for Gmail
            props.put("mail.imap.sasl.enable", "true");
            props.put("mail.imap.sasl.mechanisms", "XOAUTH2");
            props.put("mail.imap.auth.login.disable", "true");
            props.put("mail.imap.auth.plain.disable", "true");
            session = Session.getInstance(props);
            //session.setDebug(true);
            store = session.getStore("imaps");

            getResult();
        } catch (Exception mex) {
            mex.printStackTrace();
        }
        return null;
    }


//    private EmailPhrases_Builder(){
//        new RecMail().execute(this);
//    }
//
//    public static EmailPhrases_Builder getMultiInstance(){
//        emailPhrases_Builder = new EmailPhrases_Builder();
//        return emailPhrases_Builder;
//    }
//
//    private class RecMail extends AsyncTask<Object, Object, Object>{
//        protected Object doInBackground(Object... args) {
//            try {
//                Properties props = new Properties();
//                props.put("mail.imap.ssl.enable", "true"); // required for Gmail
//                props.put("mail.imap.sasl.enable", "true");
//                props.put("mail.imap.sasl.mechanisms", "XOAUTH2");
//                props.put("mail.imap.auth.login.disable", "true");
//                props.put("mail.imap.auth.plain.disable", "true");
//                Session session = Session.getInstance(props);
//                //session.setDebug(true);
//                Store store = session.getStore("imaps");
//                Log.v("sdCard",Environment.getExternalStorageDirectory().getPath());
//                String path = Environment.getExternalStorageDirectory().getPath();
//                File dir = new File(path + "/EmailExtractor/");
//                if (!dir.exists()){
//                    dir.mkdir();
//                }
//
//                dir.mkdir();
//                store.connect("imap.gmail.com", address, password);
//                Folder sentBox = store.getFolder("[Gmail]/Sent Mail");
//                sentBox.open(Folder.READ_ONLY);
//                Log.v("sentBox Num",""+sentBox.getMessageCount());
//                int i;
//                for (i = 1 ; i <=sentBox.getMessageCount() ; i++ ){
//                    File file = new File(dir,fileName+i+".txt");
//                    Message msg = sentBox.getMessage(i);
//                    Writer out = new OutputStreamWriter(new FileOutputStream(file));
//
//                    Multipart mp = (Multipart) msg.getContent();
//                    BodyPart bp = mp.getBodyPart(0);
//                    Log.v("Origin",bp.getContent().toString());
////                    String outputString = new StringCutter().cut(bp.getContent().toString());
//                    String outputString = cut(bp.getContent().toString());
//                    Log.v("Content",outputString);
//
//                    out.write(outputString);
//
//                    out.close();
//                }
//                getResult();
//            } catch (Exception mex) {
//                mex.printStackTrace();
//            }
//            return null;
//        }
//    }

    private String cut(String input){
        input=input.toLowerCase();
        init();
        String output= "";
        int templength = 0 ;
        String tempString ="";
        for (char tempchar : input.toCharArray() ){
            if(isUrl){
                if(tempchar == ' '  ) {
                    isUrl = false;
                    hasSpace = true;
                    hasHiven = false ;
                    hasApostrophe = false ;
                    hasAt = false ;
                }
                else if( tempchar=='\n' || tempchar =='\r' ){
                    isUrl = false;
                    hasNext = true ;
                    hasSpace = true;
                    hasHiven = false ;
                    hasApostrophe = false ;
                    hasAt = false ;
                }
                output = output+tempchar;
            }
            else if ( (int)tempchar<=122 &&  (int)tempchar>=97  ){
                if (hasSpace == true){
                    testing = true;
                    tempString = "";
                    templength = 0;
                }
                hasSpace = false;
                hasNext = false;
                if (hasHiven == true){
                    output= output+ '-';
                }
                else if(hasApostrophe ==true ){
                    output= output+ '\'';
                }
                else if(hasAt ==true ){
                    output= output+ '@';
                    isUrl= true;
                }
                output = output+tempchar;

            }
            else if(tempchar == '-' && hasSpace == false  ){
                hasHiven = true;
            }
            else if(tempchar == '@' && hasSpace == false  ){
                hasAt = true;
            }
            else if(tempchar == '\'' && hasSpace == false  ){
                hasApostrophe = true;
            }
            else if(tempchar == '\'' && hasSpace == false  ){
                hasApostrophe = true;
            }
            else if( (tempchar=='\n' || tempchar =='\r') && hasNext == false  ){
                hasNext = true ;
                hasSpace = true;
                hasHiven = false ;
                hasApostrophe= false;
                hasAt= false ;
                output = output+'\n';
            }
            else if( hasSpace == false  ){
                hasSpace = true ;
                hasHiven = false ;
                hasApostrophe= false;
                hasAt= false ;
                output = output+' ';
            }

            if (testing == true  ){
                tempString += tempchar;
                templength ++;
                for (String target : urls){
                    if(tempString.equals(target)){
                        isUrl = true;
                        testing = false ;
                    }
                }
                if (templength >=6) testing = false;
            }
        }

        return output;
    }
    private void init(){
        hasSpace = true;
        hasNext = false;
        hasHiven = false;
        hasApostrophe = false ;
        hasAt = false ;
        testing = true;
        isUrl = false;
    }

    public Phrases_Product getResult(){
        Log.v("sdCard",Environment.getExternalStorageDirectory().getPath());
        String path = Environment.getExternalStorageDirectory().getPath();
        File dir = new File(path + "/EmailExtractor/");
        if (!dir.exists()){
            dir.mkdir();
        }
        try{
            store.connect("imap.gmail.com", address, password);
            Folder sentBox = store.getFolder("[Gmail]/Sent Mail");
            sentBox.open(Folder.READ_ONLY);
            Log.v("sentBox Num",""+sentBox.getMessageCount());

            int count;
            for (count = 1 ; count <=sentBox.getMessageCount() ; count++ ){
                File file = new File(dir,fileName+count+".txt");
                Message msg = sentBox.getMessage(count);
                Writer out = new OutputStreamWriter(new FileOutputStream(file));

                Multipart mp = (Multipart) msg.getContent();
                BodyPart bp = mp.getBodyPart(0);
                Log.v("Origin",bp.getContent().toString());
//                    String outputString = new StringCutter().cut(bp.getContent().toString());
                String outputString = cut(bp.getContent().toString());
                Log.v("Content",outputString);

                out.write(outputString);
                out.close();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
}
