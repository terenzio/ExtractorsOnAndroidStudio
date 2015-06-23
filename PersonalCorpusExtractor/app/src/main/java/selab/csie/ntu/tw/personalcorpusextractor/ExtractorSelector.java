package selab.csie.ntu.tw.personalcorpusextractor;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;


import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import selab.csie.ntu.tw.personalcorpusextractor.keyboard_main.builder.FacebookPhrases_Builder;


public class ExtractorSelector extends Activity{

    private CheckBox facebookCheckBox,emailCheckBox,SMSCheckBox;
    private CallbackManager callbackManager;

    //loads the layout in the main activity
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        FacebookSdk.sdkInitialize(getApplicationContext());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_extractor_selector);
        callbackManager = CallbackManager.Factory.create();
        printHashKey();
        //Check with single selection
        facebookCheckBox = (CheckBox) findViewById(R.id.facebook);
        facebookCheckBox.setOnCheckedChangeListener(listener);

        emailCheckBox = (CheckBox) findViewById(R.id.email);
        emailCheckBox.setOnCheckedChangeListener(listener);

        SMSCheckBox = (CheckBox) findViewById(R.id.sms);
        SMSCheckBox.setOnCheckedChangeListener(listener);

        Button authButton = (Button) findViewById(R.id.button);

        authButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                if(facebookCheckBox.isChecked()){
                    authFacebook();
                }
                else if(emailCheckBox.isChecked()){
                }
                else if(SMSCheckBox.isChecked()){
                }
            }
        });
    }

    private CompoundButton.OnCheckedChangeListener listener = new CompoundButton.OnCheckedChangeListener() {
        public void onCheckedChanged(CompoundButton arg0, boolean isChecked) {
            if(isChecked){
                switch(arg0.getId())
                {
                    case R.id.facebook:
                        facebookCheckBox.setChecked(true);
                        emailCheckBox.setChecked(false);
                        SMSCheckBox.setChecked(false);
                        break;
                    case R.id.email:
                        facebookCheckBox.setChecked(false);
                        emailCheckBox.setChecked(true);
                        SMSCheckBox.setChecked(false);
                        break;
                    case R.id.sms:
                        facebookCheckBox.setChecked(false);
                        emailCheckBox.setChecked(false);
                        SMSCheckBox.setChecked(true);
                        break;
                }
            }
        }
    };

    private void authFacebook(){
        LoginManager.getInstance().logInWithReadPermissions(ExtractorSelector.this, Arrays.asList("public_profile", "read_mailbox"));
        LoginManager.getInstance().registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        Log.d("FacebookTest","Success");
                        FacebookPhrases_Builder.getInstance(loginResult);
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

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    //Generates the hash key used for Facebook console to register app.
    public void printHashKey(){
        // Add code to print out the key hash
        try {
            PackageInfo info = getPackageManager().
                    getPackageInfo("selab.csie.ntu.tw.personalcorpusextractor",
                            PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("HashKey:",
                        Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {
        } catch (NoSuchAlgorithmException e) {
        }
    }


//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_extractor_selector, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }
}
