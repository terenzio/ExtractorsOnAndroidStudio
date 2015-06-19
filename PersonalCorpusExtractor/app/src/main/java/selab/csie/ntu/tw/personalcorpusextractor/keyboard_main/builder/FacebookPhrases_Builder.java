package selab.csie.ntu.tw.personalcorpusextractor.keyboard_main.builder;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;

import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphObject;
import com.facebook.model.GraphUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;

import selab.csie.ntu.tw.personalcorpusextractor.FileUtils;
import selab.csie.ntu.tw.personalcorpusextractor.R;


/**
 * Created by CarsonWang on 2015/6/17.
 */
public class FacebookPhrases_Builder extends Fragment implements Phrases_Builder {

    private static FacebookPhrases_Builder uniqueInstance;

    private UiLifecycleHelper uiHelper;
    private static final String TAG = "MainFragment";
    private static String userID = null;
    private static String messageData = null;
    private static String fileName = "facebookFile";
    private static int count = 0;

    private FacebookPhrases_Builder(){
        doFacebookAuth();
    }
    public static FacebookPhrases_Builder getInstance(){
        if(uniqueInstance==null)
            uniqueInstance = new FacebookPhrases_Builder();
        return uniqueInstance;
    }

    //Do Button to display message
    private void doFacebookAuth() {
        openActiveSession(this.getActivity(), true, Arrays.asList(new String[]{"email", "read_mailbox"}), new Session.StatusCallback() {
            @Override
            public void call(Session session, SessionState state, Exception exception) {
                if (exception != null) {
                    Log.d("Facebook", exception.getMessage());
                }
                Log.d("Facebook", "Session State: " + session.getState());
                // you can make request to the /me API or do other stuff like post, etc. here
                //Request user data and show the results
                Request.executeMeRequestAsync(Session.getActiveSession(), new Request.GraphUserCallback() {
                    @Override
                    public void onCompleted(GraphUser user, Response response) {
                        if (user != null) {
                            // Fetch the parsed user ID
                            userID = buildUserID(user);
                            Log.i(TAG, "ID = " + userID);
                        }
                    }
                });

                //Request facebook message data and show the results
                new Request(
                        Session.getActiveSession(),
                        "/me/inbox",
                        null,
                        HttpMethod.GET,
                        new Request.Callback() {
                            public void onCompleted(Response response) {
                                GraphObject graphObject = response.getGraphObject();
                                getMessage(graphObject);
//                                getResult();
                            }
                        }
                ).executeAsync();
            }
        });
    }


    private static Session openActiveSession(Activity activity, boolean allowLoginUI, List permissions, Session.StatusCallback callback) {
        Session.OpenRequest openRequest = new Session.OpenRequest(activity).setPermissions(permissions).setCallback(callback);
        Session session = new Session.Builder(activity).build();
        if (SessionState.CREATED_TOKEN_LOADED.equals(session.getState()) || allowLoginUI) {
            Session.setActiveSession(session);
            session.openForRead(openRequest);
            return session;
        }
        return null;
    }

    //Show information in Display
    private String getMessage(GraphObject data) {
        StringBuilder messageInfo = new StringBuilder("");
        if (data != null) {
            JSONObject jsonObject = data.getInnerJSONObject();
            try {
                //Level 1 JSON loop
                JSONArray dataArray = jsonObject.getJSONArray("data");
                for (int dataSize = 0; dataSize < dataArray.length(); dataSize++) {
                    JSONObject dataObject = dataArray.getJSONObject(dataSize);

                    //Level 2 JSON loop
                    JSONObject messageObject = dataObject.getJSONObject("comments");

                    //Level 3 JSON loop
                    JSONArray messageArray = messageObject.getJSONArray("data");
                    for (int messageSize = messageArray.length() - 1; messageSize >= 0; messageSize--) {

                        //All message can be fetched
//						JSONObject message = messageArray.getJSONObject(messageSize);
//						messageInfo.append(message.getString("message") + "\n");

                        //Level 4 JSON loop fetch the message of sending
                        JSONObject message = messageArray.getJSONObject(messageSize);
                        JSONObject messageFromObject = message.getJSONObject("from");
                        String messageFromID = messageFromObject.getString("id");
                        if (messageFromID.matches(userID)) {
							/*Can use optString instead of getString which just returns null
							  if value doesn't exist, instead of throwing an exception.*/
                            if ((message.has("message") && !message.isNull("message"))){
                                //This line handle a message include white space ()
//                                messageInfo.append(message.getString("message") + "\n");

                                //Other way to implement a message which has no white space
                                String noSpaceMessage = message.getString("message").
                                        replaceAll("[^a-zA-Z0-9 \\s]+", "");

                                String totalMessage = "";
                                String [] mergeString = noSpaceMessage.split("\\s");
                                for(int spaceNumber = 0 ; spaceNumber < mergeString.length; spaceNumber++)
                                    totalMessage += mergeString[spaceNumber] + " ";
                                if(totalMessage.length()>=3)
                                    messageInfo.append(totalMessage + "\n");
                            }
                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        messageData = messageInfo.toString();
        return messageData;
    }

    // Accept User ID
    private String buildUserID(GraphUser user) {
        StringBuilder userInfo = new StringBuilder("");

        userInfo.append(user.getId());
        return userInfo.toString();
    }

    /**
     * creates the Facebook session and opens it automatically
     * if a cached token is available
     */

    //Passing in the callback variable
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        uiHelper = new UiLifecycleHelper(this.getActivity(), callback);
        uiHelper.onCreate(savedInstanceState);
    }

    //Respond to session state changes (handle login and logout)
    private void onSessionStateChange(Session session, SessionState state, Exception exception) {
        if (state.isOpened()) {
            Log.i(TAG, "Logged in...");
        } else if (state.isClosed()) {
            Log.i(TAG, "Logged out...");
        }
    }

    //add logic to listen for the changes
    private Session.StatusCallback callback = new Session.StatusCallback() {
        @Override
        public void call(Session session, SessionState state, Exception exception) {
            onSessionStateChange(session, state, exception);
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        Session session = Session.getActiveSession();
        if (session != null &&
                (session.isOpened() || session.isClosed())) {
            onSessionStateChange(session, session.getState(), null);
        }

        uiHelper.onResume();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        uiHelper.onActivityResult(requestCode, resultCode, data);
        Session.getActiveSession().onActivityResult(this.getActivity(), requestCode, resultCode, data);
    }

    @Override
    public void onPause() {
        super.onPause();
        uiHelper.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        uiHelper.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        uiHelper.onSaveInstanceState(outState);
    }

    public Phrases_Product getResult(){
        AlertDialog.Builder dialog = new AlertDialog.Builder(this.getActivity());
        dialog.setTitle("File Request");
        if (messageData != null) {
            if (FileUtils.isExternalStorageWritable()) {
                // "\\s" mean that white space
//                        String englishOnlyString = Normalizer.normalize(messageData, Normalizer.Form.NFD).
//                                replaceAll("[^a-zA-Z0-9 \\s]+", "");
                //Other way
//                        String englishOnlyString = messageData.replaceAll("[^a-zA-Z0-9 \\s]+", "");

//                        FileUtils.writeToFile(fileName, englishOnlyString);
                FileUtils.writeToFile(fileName+String.valueOf(count), messageData);
                count++;
                dialog.setMessage("Write successfully!");
            } else dialog.setMessage("Write fail!");
        } else dialog.setMessage("Write fail!");
        dialog.setPositiveButton(R.string.ok_label,
                new DialogInterface.OnClickListener() {
                    public void onClick(
                            DialogInterface dialoginterface, int i) {
                    }
                });
        dialog.show();
        return null;
    }


}
