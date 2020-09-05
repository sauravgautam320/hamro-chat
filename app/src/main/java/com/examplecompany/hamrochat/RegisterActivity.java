package com.examplecompany.hamrochat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;


public class RegisterActivity extends AppCompatActivity {

    //constants
    static final String CHAT_PREFS = "ChatPrefs";
    static final String DISPLAY_NAME_KEY = "username";

    //widgets
    private EditText
            mUsernameView,
            mPasswordView,
            mEmailView,
            mConfirmPasswordView;

    private Button signUpBtn;

    //firebase
    private FirebaseAuth mAuth;

    //for viewing ad
    private AdView mAdView;
    private InterstitialAd mInterstitialAd;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //loads banner ad
        MobileAds.initialize(this, "PUT YOUR UNIQUE APP ID HERE FOR ADMOB");
        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        //loads interstitial ad
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId("PUT YOUR INTERSTITIAL AD UNIT ID HERE");
        mInterstitialAd.loadAd(new AdRequest.Builder().build());

        //initializing widgets:
        mUsernameView = findViewById(R.id.register_username);
        mPasswordView = findViewById(R.id.register_password);
        mConfirmPasswordView = findViewById(R.id.register_confirm_password);
        mEmailView = findViewById(R.id.register_email);
        signUpBtn = findViewById(R.id.register_sign_up_button);

        mConfirmPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if(id == EditorInfo.IME_NULL)
                {
                    attemptRegistration();
                    return true;
                }
                return false;
            }
        });

        //get hold of an instance of fireBaseAuth
        mAuth = FirebaseAuth.getInstance();

    }

    //executed when sign up button is pressed
    public void signUp(View v){
        attemptRegistration();

    }

    //to attempt registration process
    private void attemptRegistration(){

        //resets the errors displayed in the form
        mEmailView.setError(null);
        mPasswordView.setError(null);

        //stores values at the time of login attempt
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        //check for a valid password, if the password is blank
        if(!TextUtils.isEmpty(password) && !isPasswordValid(password)){
            mPasswordView.setError("Password too short or does not match");
            focusView = mPasswordView;
            cancel = true;
        }

        //check for valid email
        if(TextUtils.isEmpty(email)){
            mEmailView.setError("This field cannot be empty");
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)){
            mEmailView.setError("This Email Address is invalid");
            focusView = mEmailView;
            cancel = true;
        }

        if(cancel){
            //there was an error, dont attempt login and focus the first
            //form field with an error
            focusView.requestFocus();
        } else {
            //call firebaseuser
            createFirebaseUser();
        }
    }

    //to check if provided email is valid, checks for presence of '@' in the email address
    private boolean isEmailValid(String email){

        return email.contains("@");
    }

    //to check if the password is valid, checks if the password and confirm password field match & if password is >= to 6 digits
    private boolean isPasswordValid(String password){

        String confirmPassword = mConfirmPasswordView.getText().toString();
        return confirmPassword.equals(password) && password.length() >= 6;
    }

    //Create a firebase user
    private void createFirebaseUser(){

        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                Log.d("Hamro Chat", "createUser onComplete: " + task.isSuccessful());


                if(!task.isSuccessful()){
                    Log.d("Hamro Chat", "user creation failed");
                    showErrorDialog("Registration attempt Failed!");
                } else {
                    saveDisplayName();
                    if (mInterstitialAd.isLoaded()) {
                        mInterstitialAd.show();
                    } else {
                        Log.d("TAG", "The interstitial wasn't loaded yet.");
                    }
                    Intent intent = new Intent(RegisterActivity.this, activity_login.class);
                    finish();
                    startActivity(intent);
                }

            }
        });
    }

    //save display name to shared preferences
    private void saveDisplayName(){
        String displayName = mUsernameView.getText().toString();
        SharedPreferences prefs = getSharedPreferences(CHAT_PREFS, 0);
        prefs.edit().putString(DISPLAY_NAME_KEY, displayName).apply();

    }

    //create error dialog
    private void showErrorDialog(String message){

        new  AlertDialog.Builder(this)
                .setTitle("Oops")
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

}