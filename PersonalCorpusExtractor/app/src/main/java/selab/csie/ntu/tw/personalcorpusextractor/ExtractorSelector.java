package selab.csie.ntu.tw.personalcorpusextractor;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import selab.csie.ntu.tw.personalcorpusextractor.keyboard_main.builder.EmailPhrases_Builder;
import selab.csie.ntu.tw.personalcorpusextractor.keyboard_main.builder.FacebookPhrases_Builder;
import selab.csie.ntu.tw.personalcorpusextractor.keyboard_main.builder.SMSPhrases_Builder;
import tw.edu.ntu.selab.query_refinement_system.AssetManager;
import tw.edu.ntu.selab.query_refinement_system.PersonalOntologyBuilder;
import tw.edu.ntu.selab.query_refinement_system.QueryRefiner;
import tw.edu.ntu.selab.query_refinement_system.Settings;
import tw.edu.ntu.selab.query_refinement_system.exceptions.OntologyUpdateException;
import tw.edu.ntu.selab.query_refinement_system.exceptions.StringProcessingException;
import tw.edu.ntu.selab.query_refinement_system.personal_ontology.PersonalOntology;


public class ExtractorSelector extends Activity{

    private CheckBox facebookCheckBox,emailCheckBox,SMSCheckBox;
    public static CallbackManager callbackManager;
    private static ExtractorSelector extractorSelector;

    //Ontology Files
    private PersonalOntology ontology;
    private QueryRefiner refiner;
    private PersonalOntologyBuilder builder;

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

        //Ontology Initialization
        try {
            initialize();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        //Ontology Initialization End



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
                    FacebookPhrases_Builder.getMultiInstance();
                }
                else if(emailCheckBox.isChecked()){
                    EmailPhrases_Builder.getMultiInstance();
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

    public void updateOntology(View view) {
        TextView updateView = (TextView) findViewById(R.id.textViewUpdate);
        Toast toast1 = Toast.makeText(getApplicationContext(), "Ontology has been updated!", Toast.LENGTH_SHORT);
        Toast toast2= Toast.makeText(getApplicationContext(), "Ontology was NOT updated (duplicate document)!", Toast.LENGTH_SHORT);
        try {
            String text = ((EditText) findViewById(R.id.editTextUpdate)).getText().toString();
            System.out.println("Text: " + text);
            boolean updated = builder.updateOntology(Collections.singletonList(text), "Facebook", null);
            if(updated)
                toast1.show();
                //updateView.setText("Ontology has been updated!");
            else
                //updateView.setText("Ontology was NOT updated (duplicate document)!");
                toast2.show();
        } catch (OntologyUpdateException e) {
            e.printStackTrace();
            updateView.setText(e.getMessage());
        }
    }

    public void findExtensions(View view) {
        final String query = ((EditText) findViewById(R.id.editTextQuery)).getText().toString();
        AsyncTask<Void, Void, Set<String>> t = new AsyncTask<Void, Void, Set<String>>() {
            ProgressDialog dialog;

            @Override
            protected void onPreExecute() {
                dialog = ProgressDialog.show(ExtractorSelector.this, "Search", "Searching extensions for \"" + query + "\".", true, false);
            }

            @Override
            protected void onPostExecute(Set<String> result) {
                dialog.dismiss();
                ((TextView) findViewById(R.id.textViewRefinement)).setText(result == null ? "No extensions found." : TextUtils.join("\n", result));
            }

            @Override
            protected Set<String> doInBackground(Void... params) {
                try {
                    long start = System.currentTimeMillis();
                    Map<String,Double> detailedExtensions = refiner.getQueryExtensionsAndSimilarities(ontology, query, "Facebook", 0.1, 10);
                    long end = System.currentTimeMillis();
                    if(detailedExtensions == null || detailedExtensions.size() == 0) {
                        return null;
                    }
                    final Map<String,Double> detailedExtensionsCopy = new HashMap(detailedExtensions);
                    Comparator<String> c = new Comparator<String>() {

                        @Override
                        public int compare(String lhs, String rhs) {
                            double a = detailedExtensionsCopy.get(lhs);
                            double b = detailedExtensionsCopy.get(rhs);
                            return a > b ? -1 : 1;
                        }
                    };

                    TreeMap<String, Double> sortedDetailedExtensions = new TreeMap(c);
                    sortedDetailedExtensions.putAll(detailedExtensions);

//					System.out.println("Result size: " + detailedExtensions.size() + ", sorted result size: " + sortedDetailedExtensions.size());

                    Set<String> extensions = new LinkedHashSet();
                    for(Map.Entry<String, Double> extension : sortedDetailedExtensions.entrySet()) {
                        extensions.add(String.format("%-15s%.2f", extension.getKey(), extension.getValue()));
                    }
                    extensions.add("Total time: " + String.format("%.2f seconds", (end-start)/1000.0));
//					Set<String> extensions = refiner.getQueryExtensions(Collections.singletonList(query), 10);
                    return extensions;
                } catch (StringProcessingException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                return null;
            }

//			private void setText(final String text) {
//				runOnUiThread(new Runnable() {
//					public void run() {
//						((TextView) findViewById(R.id.textViewRefinement)).setText(text);
//					}
//				});
//			}
        };
        t.execute();
    }

    private void initialize() throws Exception {
        AsyncTask<Void, Void, Void> t = new AsyncTask<Void, Void, Void>() {
            ProgressDialog dialog;

            @Override
            protected void onPreExecute() {
                dialog = ProgressDialog.show(ExtractorSelector.this, "Initialization", "Initializing", true, false);
            }

            @Override
            protected void onPostExecute(Void result) {
                dialog.dismiss();
            }

            @Override
            protected Void doInBackground(Void... params) {
                try {
                    PersonalOntology.assetManager = new AssetManager() {

                        @Override
                        public InputStream getResourceAsStream(String fileName) {
                            try {
                                InputStream is = getResources().getAssets().open(fileName);
//								System.out.println("Android manager, is for " + fileName + " is == null: " + (is == null));
                                return is;
                            } catch (IOException e) {
                                return null;
                            }
                        }
                    };
                    Settings.verbose = false;
//					Settings.
                    //Log.d("keyboard", "initialize()");
                    SharedPreferences p = getPreferences(0);
                    String propertyName = "initialized6";
                    boolean initialized = p.getBoolean(propertyName, false);
//					boolean initialized = false;
                    if(!initialized) {
//						emptyFilesDir();
                        setMessage("Copying WordNet file ...");
                        Log.d("keyboard", "copying file");
//						String[] files = new String[] {"wnjpn.db", "default_personal_ontology.owl", "word_list.txt"};
                        String[] files = new String[] {"wnjpn.db"};
                        for(String file : files) {
                            PersonalOntology.copy(getAssets().open(file), new FileOutputStream(new File(getFilesDir(), file)));
                        }
                        setMessage("Creating new ontology ...");
                        PersonalOntology.createNewOntologyFromDefault(getFilesDir());
                    } else {
                        Log.d("keyboard", "Already initialized.");
                    }

                    setMessage("Loading ontology ...");
                    ontology = PersonalOntology.getOntology(1, getFilesDir());
                    refiner = new QueryRefiner(ontology);
                    builder = new PersonalOntologyBuilder(ontology);
//					builder.updateOntology(Collections.singletonList("Hello, how are you?"), null);
                    p.edit().putBoolean(propertyName, true).commit();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }

            private void setMessage(final String message) {
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        dialog.setMessage(message);
                    }
                });
            }
        };

        t.execute();
    }


}
