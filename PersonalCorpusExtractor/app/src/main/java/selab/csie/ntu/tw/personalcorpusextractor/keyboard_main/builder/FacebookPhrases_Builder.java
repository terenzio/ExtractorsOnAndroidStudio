package selab.csie.ntu.tw.personalcorpusextractor.keyboard_main.builder;


/**
 * Created by CarsonWang on 2015/6/17.
 */
public class FacebookPhrases_Builder implements Phrases_Builder {

    /**
     * Created by FragmentActivity java.
     */
    //Do Button to display message
    private void doMessageRequest() {
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
                        messageInfoTextView.setText(buildMessageInfoDisplay(graphObject));
                    }
                }
        ).executeAsync();
    }


    //Show information in Display
    private String buildMessageInfoDisplay(GraphObject data) {
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
     * Created by FileUtils java.
     */

    //Checks if external storage is available for read and write
    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    //Write file to external storage
    public static void writeToFile(String fileName, String data){
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

    public Phrases_Product getResult(){
        return null;
    }
}
