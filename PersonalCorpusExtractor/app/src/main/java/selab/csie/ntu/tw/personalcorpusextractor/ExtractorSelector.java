package selab.csie.ntu.tw.personalcorpusextractor;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;


import com.facebook.CallbackManager;
import com.facebook.FacebookSdk;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import selab.csie.ntu.tw.personalcorpusextractor.keyboard_main.builder.EmailPhrases_Builder;
import selab.csie.ntu.tw.personalcorpusextractor.keyboard_main.builder.FacebookPhrases_Builder;
import selab.csie.ntu.tw.personalcorpusextractor.keyboard_main.builder.SMSPhrases_Builder;


public class ExtractorSelector extends Activity{

    private CheckBox facebookCheckBox,emailCheckBox,SMSCheckBox;
    public static CallbackManager callbackManager;
    private static ExtractorSelector extractorSelector;

    public static ExtractorSelector getInstance(){
        return extractorSelector;
    }

    //loads the layout in the main activity
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        FacebookSdk.sdkInitialize(getApplicationContext());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_extractor_selector);
        callbackManager = CallbackManager.Factory.create();
        printHashKey();
        this.extractorSelector = ExtractorSelector.this;

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
//                    processingDialog = new AlertDialog.Builder(ExtractorSelector.this);
//                    processingDialog.setTitle("Waiting");
//                    processingDialog.setMessage("Please wait for a moment");
//                    processingDialog.show();
                    FacebookPhrases_Builder.getMultiInstance();
                }
                else if(emailCheckBox.isChecked()){
                    new EmailPhrases_Builder().execute(this);
//                    EmailPhrases_Builder.getMultiInstance();
                }
                else if(SMSCheckBox.isChecked()){
                    SMSPhrases_Builder.getMultiInstance();
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
}
