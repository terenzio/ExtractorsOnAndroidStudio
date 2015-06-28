package selab.csie.ntu.tw.personalcorpusextractor.keyboard_main.builder;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Environment;
import android.util.Log;

import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import selab.csie.ntu.tw.personalcorpusextractor.ExtractorSelector;
import selab.csie.ntu.tw.personalcorpusextractor.R;


/**
 * Created by CarsonWang on 2015/6/17.
 */
public class FacebookPhrases_Builder implements Phrases_Builder {
    private static FacebookPhrases_Builder facebookPhrases_Builder;

    private final String TAG = "FacebookTest";
    private final String fileName = "BagOfWordFacebook";
    private static String finalFileName = "";

    private static LoginResult getLoginResult = null;
    private static String myID = null;
    private static String messageData = null;
    private ArrayList<String> allMessage = new ArrayList<>();

    private static int count = 0;

    public static String getFileName() {
        return finalFileName;
    }

    public static FacebookPhrases_Builder getMultiInstance(){
        facebookPhrases_Builder = new FacebookPhrases_Builder();
        return facebookPhrases_Builder;
    }
    private FacebookPhrases_Builder(){
        authFacebook();
    }
    private void authFacebook(){
        LoginManager.getInstance().logInWithReadPermissions(ExtractorSelector.getInstance(), Arrays.asList("public_profile", "read_mailbox"));
        LoginManager.getInstance().registerCallback(ExtractorSelector.callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        getLoginResult = loginResult;
                        Log.d(TAG,"Success");
                        messageData = null;
                        getMyID();
                        getMessages();
                    }

                    @Override
                    public void onCancel() {
                        Log.d(TAG,"Cancel");
                    }

                    @Override
                    public void onError(FacebookException e) {
                        Log.d(TAG,"Error");
                    }
                });
    }

    private void getMyID(){
        GraphRequest request = GraphRequest.newMeRequest(
                getLoginResult.getAccessToken(),
                new GraphRequest.GraphJSONObjectCallback(){
                    @Override
                    public void onCompleted(JSONObject object,GraphResponse response){

                        JSONObject jsonObject = response.getJSONObject();
                        try {
                            myID = jsonObject.getString("id");
                            Log.d(TAG,myID);
                        }catch (JSONException e){
                            e.printStackTrace();
                        }
                    }

                }
        );
        request.executeAsync();
    }

    private void getMessages(){
        GraphRequest request = new GraphRequest(
                getLoginResult.getAccessToken(),
                "/me/inbox",
                null,
                HttpMethod.GET,
                new GraphRequest.Callback() {
                    @Override
                    public void onCompleted(GraphResponse response) {
                        handleMessage(response);
                    }
                });

        request.executeAsync();
    }

    //Catch all dialog with 25 message at the moment if catch all, must be add coments paging
    private void handleMessage(GraphResponse response) {
        JSONObject data = response.getJSONObject();
        StringBuffer messageAll = new StringBuffer();
        try {
            if (data!=null){
                //Level 1 JSON loop
                Log.v(TAG,data.toString());
                JSONArray dataArray = data.getJSONArray("data");
                for (int dataSize = 0; dataSize < dataArray.length(); dataSize++) {
                    JSONObject dataObject = dataArray.getJSONObject(dataSize);

                    if (!dataObject.isNull("comments")) {
                        //Level 2 JSON loop
                        JSONObject messageObject = dataObject.getJSONObject("comments");
                        if (!messageObject.isNull("data")) {
                            //Level 3 JSON loop
                            JSONArray messageArray = messageObject.getJSONArray("data");
                            //Comments paging  if(!messageObject.getString("paging").isEmpty())
                            for (int messageSize = messageArray.length() - 1; messageSize >= 0; messageSize--) {
                                //All message including sendind and receiving can be fetched
//                                JSONObject message = messageArray.getJSONObject(messageSize);
//                                messageAll.append(message.getString("message")+"\n");

                                //Level 4 JSON loop fetch the message of sending
                                JSONObject message = messageArray.getJSONObject(messageSize);
                                if (!message.isNull("from")) {
                                    JSONObject messageFromObject = message.getJSONObject("from");

                                    if (!messageFromObject.isNull("id")) {
                                        String messageFromID = messageFromObject.getString("id");
                                        if (messageFromID.matches(myID)) {
                                            /*Can use optString instead of getString which just returns null
                                              if value doesn't exist, instead of throwing an exception.*/
                                            if ((message.has("message") && !message.isNull("message"))) {
//                                                //This line handle a message include white space ()
//                                                messageAll.append(message.getString("message") + "\n");

                                                //Other way to implement a message which has no white space
                                                String noSpaceMessage = message.getString("message").
                                                        replaceAll("[^a-zA-Z0-9 \\s]+", "");

                                                String totalMessage = "";
                                                String[] mergeString = noSpaceMessage.split("\\s");
                                                for (int spaceNumber = 0; spaceNumber < mergeString.length; spaceNumber++)
                                                    totalMessage += mergeString[spaceNumber] + " ";
                                                if (totalMessage.length() >= 3)
                                                    messageAll.append(totalMessage + "\n");
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }//end for data array
                allMessage.add(messageAll.toString());
                //data paging
                for(String message : allMessage)
                    messageData += message;
                getResult();
//                if(data.has("paging")){
//                    JSONObject nextPaging = data.getJSONObject("paging");
//                    handlePaging(response);
//                    Log.d(TAG,"Data link = "+nextPaging.toString());
//                }
//                else{
//                    for(String message : allMessage)
//                        messageData += message;
//                    getResult();
//                }
            }//end data
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void handlePaging(GraphResponse response){
        GraphRequest paging = response.getRequestForPagedResults(GraphResponse.PagingDirection.NEXT);
        paging.setGraphPath(response.getRequest().getGraphPath());
        paging.setCallback(
                new GraphRequest.Callback() {
                    @Override
                    public void onCompleted(GraphResponse response) {
                        handleMessage(response);
                    }
                }
        );
        GraphRequest.executeBatchAsync(paging);
    }

    public Phrases_Product getResult(){
        AlertDialog.Builder dialog = new AlertDialog.Builder(ExtractorSelector.getInstance());
        dialog.setTitle("File Request");
        if (messageData!=null) {
            if (isExternalStorageWritable()) {
                // "\\s" mean that white space
//                        String englishOnlyString = Normalizer.normalize(messageData, Normalizer.Form.NFD).
//                                replaceAll("[^a-zA-Z0-9 \\s]+", "");
                //Other way
//                        String englishOnlyString = messageData.replaceAll("[^a-zA-Z0-9 \\s]+", "");

//                        FileUtils.writeToFile(fileName, englishOnlyString);
                finalFileName = fileName+String.valueOf(count)+".txt";
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

//    public Phrases_Product getResult(){
//        return null;
//    }

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
//	    File dir = new File(path + "/facebookOutboxextractor");
        File dir = new File(path + "/");
        if (!dir.exists()){
            dir.mkdir();
        }
        try {
//	    	File file = new File(path + "/facebookOutboxextractor/" + fileName);
            File file = new File(path + "/" + fileName);
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
