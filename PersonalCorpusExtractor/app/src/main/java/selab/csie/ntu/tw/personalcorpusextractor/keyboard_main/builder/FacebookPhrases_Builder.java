package selab.csie.ntu.tw.personalcorpusextractor.keyboard_main.builder;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
//import com.facebook.Request;
//import com.facebook.Response;
//import com.facebook.Session;
//import com.facebook.SessionState;
//import com.facebook.UiLifecycleHelper;
//import com.facebook.model.GraphObject;
//import com.facebook.model.GraphUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;

import selab.csie.ntu.tw.personalcorpusextractor.ExtractorSelector;
import selab.csie.ntu.tw.personalcorpusextractor.FileUtils;
import selab.csie.ntu.tw.personalcorpusextractor.R;


/**
 * Created by CarsonWang on 2015/6/17.
 */
public class FacebookPhrases_Builder implements Phrases_Builder {
    private static FacebookPhrases_Builder facebookPhrases_Builder;

    private static LoginResult getLoginResult;

    private static String myID = null;
    private static String messageData = null;

    private static String fileName = "facebookFile";
    private static int count = 0;


    public static FacebookPhrases_Builder getInstance(){
        if(facebookPhrases_Builder == null)
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
                        getMyID();
                        getMessages();
                        Log.d("FacebookTest","Success");
                    }

                    @Override
                    public void onCancel() {
                        Log.d("FacebookTest","Cancel");
                    }

                    @Override
                    public void onError(FacebookException e) {
                        Log.d("FacebookTest","Error");
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
                            Log.d("FacebookTest",myID);
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

    private void handleMessage(GraphResponse response) {
        JSONObject data = response.getJSONObject();
        StringBuffer messageAll = new StringBuffer();
        if (data != null) {
            try {
                //Level 1 JSON loop
                JSONArray dataArray = data.getJSONArray("data");
                //data paging
                if(!data.getString("paging").isEmpty()){
                    JSONObject nextPaging = data.getJSONObject("paging");
//                    handlePaging(response);
                    Log.d("FacebookTest","Data link = "+nextPaging.toString());
                }
                for (int dataSize = 0; dataSize < dataArray.length(); dataSize++) {
                    JSONObject dataObject = dataArray.getJSONObject(dataSize);

                    //Level 2 JSON loop
                    JSONObject messageObject = dataObject.getJSONObject("comments");
                    //Level 3 JSON loop
                    JSONArray messageArray = messageObject.getJSONArray("data");
                    //Comments paging
                    if(!messageObject.getString("paging").isEmpty()){
                        JSONObject nextPaging = messageObject.getJSONObject("paging");
                        handlePaging(response);
                        Log.d("FacebookTest","Comments link = "+nextPaging.toString());
                    }

                    for (int messageSize = messageArray.length()-1 ; messageSize >= 0; messageSize--) {

//                        //All message including sendind and receiving can be fetched
//						JSONObject message = messageArray.getJSONObject(messageSize);
//                        messageAll.append(message.getString("message")+"\n");

                        //Level 4 JSON loop fetch the message of sending
                        JSONObject message = messageArray.getJSONObject(messageSize);
                        JSONObject messageFromObject = message.getJSONObject("from");
                        String messageFromID = messageFromObject.getString("id");
                        if (messageFromID.matches(myID)) {
							/*Can use optString instead of getString which just returns null
							  if value doesn't exist, instead of throwing an exception.*/
                            if ((message.has("message") && !message.isNull("message"))){
                                //This line handle a message include white space ()
                                messageAll.append(message.getString("message") + "\n");

//                                //Other way to implement a message which has no white space
//                                String noSpaceMessage = message.getString("message").
//                                        replaceAll("[^a-zA-Z0-9 \\s]+", "");
//
//                                String totalMessage = "";
//                                String [] mergeString = noSpaceMessage.split("\\s");
//                                for(int spaceNumber = 0 ; spaceNumber < mergeString.length; spaceNumber++)
//                                    totalMessage += mergeString[spaceNumber] + " ";
//                                if(totalMessage.length()>=3)
//                                    messageInfo.append(totalMessage + "\n");
                            }
                        }
                    }
//                    if(!messageObject.getString("paging").isEmpty()){
//                        JSONObject nextPaging = messageObject.getJSONObject("paging");
//                        Log.d("FacebookNextLink","Link="+nextPaging.toString());
//                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            messageData = messageAll.toString();
        }
    }

    private void handlePaging(GraphResponse response){
        GraphRequest paging = response.getRequestForPagedResults(GraphResponse.PagingDirection.NEXT);
        if(paging!=null){
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
    }


//    public Phrases_Product getResult(){
//        dialog.setTitle("File Request");
//        if (messageData != null) {
//            if (FileUtils.isExternalStorageWritable()) {
//                // "\\s" mean that white space
////                        String englishOnlyString = Normalizer.normalize(messageData, Normalizer.Form.NFD).
////                                replaceAll("[^a-zA-Z0-9 \\s]+", "");
//                //Other way
////                        String englishOnlyString = messageData.replaceAll("[^a-zA-Z0-9 \\s]+", "");
//
////                        FileUtils.writeToFile(fileName, englishOnlyString);
//                FileUtils.writeToFile(fileName+String.valueOf(count), messageData);
//                count++;
//                dialog.setMessage("Write successfully!");
//            } else dialog.setMessage("Write fail!");
//        } else dialog.setMessage("Write fail!");
//        dialog.setPositiveButton(R.string.ok_label,
//                new DialogInterface.OnClickListener() {
//                    public void onClick(
//                            DialogInterface dialoginterface, int i) {
//                    }
//                });
//        dialog.show();
//        return null;
//    }
    public Phrases_Product getResult(){
        return null;
    }
}
