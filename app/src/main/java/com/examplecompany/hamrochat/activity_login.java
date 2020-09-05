package com.examplecompany.hamrochat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class activity_login extends AppCompatActivity {

    //constants
    static final String CHAT_PREFS = "ChatPrefs";
    static final String DISPLAY_EMAIL_KEY = "email";
    static final String DISPLAY_PASSWORD_KEY = "password";

    private EditText mEmailView, mPasswordView;

    private String mLoginEmail;
    private String mLoginPassword;

    //for firebase auth
    private FirebaseAuth mAuth;

    //for ad
    private AdView mAdView;
    private InterstitialAd mInterstitialAd;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        setupLoginInfo();

        //LOADS BANNER AD
        MobileAds.initialize(this, "ENER YOUR UNIQUE APP ID HERE FOR ADMOB");
        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        //LOADS INTERSTITIAL AD
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId("ENTER YOUR INTERSTITIAL AD UNIT ID HERE");
        mInterstitialAd.loadAd(new AdRequest.Builder().build());

        mEmailView = findViewById(R.id.login_email);
        mPasswordView = findViewById(R.id.login_password);


        //grab an instance for firebaseAuth
        mAuth = FirebaseAuth.getInstance();
    }

    //executed when signin button pressed
    public void signInExistingUser(View v) {
        attemptLogin();
    }

    //executed when signup button is pressed
    public void registerNewUser(View v){
        //takes user to registration form
        if (mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
        } else {
            Log.d("TAG", "The interstitial wasn't loaded yet.");
        }
        Intent intent = new Intent(this, com.examplecompany.hamrochat.RegisterActivity.class);
        finish();
        startActivity(intent);
    }

    private void attemptLogin(){

        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        //checks if email or password is blank ifso not  to login
        if(email.equals("") || password.equals("")){
            return;
        } else {
            Toast.makeText(this, "Login in Progress...", Toast.LENGTH_SHORT).show();
        }

        //use firebaseAUth to sign in with email n password
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                Log.d("Hamro Chat", "signInWithEmail() onComplete: " + task.isSuccessful());

                if(!task.isSuccessful()){
                    Log.d("Hamro Chat", "Problem Signing in: " + task.getException());
                    showErrorDialog("Email or Password Incorrect!");

                } else {
                    saveLoginInfo();
                    if (mInterstitialAd.isLoaded()) {
                        mInterstitialAd.show();
                    } else {
                        Log.d("TAG", "The interstitial wasn't loaded yet.");
                    }
                    Intent intent = new Intent(activity_login.this, activity_main_chat.class);
                    finish();
                    startActivity(intent);
                }
            }
        });
    }



    //saves login info after successful login so that user doesnt have to relogin every time the app is started
    private void saveLoginInfo(){
        String loginEmail = mEmailView.getText().toString();
        String loginPassword = mPasswordView.getText().toString();
        SharedPreferences sharedPrefs = getSharedPreferences(CHAT_PREFS, 0);
        sharedPrefs.edit().putString(DISPLAY_EMAIL_KEY, loginEmail).apply();
        sharedPrefs.edit().putString(DISPLAY_PASSWORD_KEY, loginPassword).apply();
    }

    //setups login info in users personal device
    private void setupLoginInfo(){

        SharedPreferences sharedPreferences = getSharedPreferences(activity_login.CHAT_PREFS, MODE_PRIVATE);
        mLoginEmail = sharedPreferences.getString(activity_login.DISPLAY_EMAIL_KEY, null);
        mLoginPassword = sharedPreferences.getString(activity_login.DISPLAY_PASSWORD_KEY, null);
        if(mLoginPassword == null && mLoginPassword == null){
            mLoginPassword = "";
            mLoginPassword = "";
        } else {
            Intent intent = new Intent(activity_login.this, activity_main_chat.class);
            finish();
            startActivity(intent);
        }


    }

    //show error on screen with alert dialog
    private void showErrorDialog(String message){
        new AlertDialog.Builder(this)
                .setTitle("Oops")
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }
}