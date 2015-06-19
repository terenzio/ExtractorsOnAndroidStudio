package selab.csie.ntu.tw.personalcorpusextractor;

import android.app.Activity;
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

import com.facebook.UiLifecycleHelper;
import com.facebook.android.Facebook;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import selab.csie.ntu.tw.personalcorpusextractor.keyboard_main.builder.FacebookPhrases_Builder;


public class ExtractorSelector extends FragmentActivity {

    private CheckBox facebookCheckBox,emailCheckBox,SMSCheckBox;


    //loads the layout in the main activity
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_extractor_selector);
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
                    FacebookPhrases_Builder.getInstance();
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
