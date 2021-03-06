package com.tourism.hesham.rentapp;

import android.animation.Animator;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class LoginActivity extends AppCompatActivity {


    private LoginButton login_btn;
    CallbackManager callbackManager;
    AccessTokenTracker accessTokenTracker;
    ProfileTracker profileTracker;
    private Button facebook_btn;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private FirebaseDatabase database;

    //Login Views declaration
    private EditText emailL , passwordL;
    private Button login_emailPass , registerAct;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_login);

        initiallizeControls();

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Initialize firebase Listener to check the state of firebase auth for login
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if(firebaseAuth.getCurrentUser() != null){

                    Intent intent = new Intent(getApplicationContext() , MapsActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        };
//        if (login_btn.getText().equals("Log out")) {
//            Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
//            startActivity(intent);
//        }
        loginWithFB();
        facebook_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                database = FirebaseDatabase.getInstance();
                Profile profile = Profile.getCurrentProfile();

//                FirebaseDatabase database = FirebaseDatabase.getInstance();
//
//                DatabaseReference users = database.getReference("users");
//                users.child("egypt/"+"alex/"+profile.getId()+"/status/").setValue("online");
//                users.child("egypt/"+"alex/"+profile.getId()+"/owns/"+"flat/"+"flatId").setValue("");
                login_btn.performClick();
            }
        });

        login_emailPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LoginWithEmailPassword();
            }
        });

        registerAct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               startActivity(new Intent(getApplicationContext() , RegisterActivity.class ));
            }
        });



    }



    private void initiallizeControls(){
        callbackManager = CallbackManager.Factory.create();
        login_btn = (LoginButton) findViewById(R.id.login_button);
        facebook_btn = (Button)findViewById(R.id.myfacebook);

        //Login Views with email and password
        emailL = (EditText)findViewById(R.id.email_login);
        passwordL = (EditText)findViewById(R.id.password_login);
        login_emailPass = (Button)findViewById(R.id.login);
        registerAct = (Button)findViewById(R.id.register);

    }

        private void loginWithFB(){

        accessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken currentAccessToken) {

            }
        };

        profileTracker = new ProfileTracker() {
            @Override
            protected void onCurrentProfileChanged(Profile oldProfile, Profile currentProfile) {
//                nextActivity(currentProfile);
            }
        };

        accessTokenTracker.startTracking();
        profileTracker.startTracking();

        LoginManager.getInstance().registerCallback(callbackManager , new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {

                anime();
                ProgressDialog progressDialog = new ProgressDialog(LoginActivity.this);
                progressDialog.setMessage("Logging in please wait ...");
                progressDialog.show();


                FirebaseUser currentUser = mAuth.getCurrentUser();
                mAuth.addAuthStateListener(mAuthListener);
                Profile profile = Profile.getCurrentProfile();
//                nextActivity(profile);
                // I'm sending the profile data to the navigation drawer info
                handleFacebookAccessToken(loginResult.getAccessToken());




            }

            @Override
            public void onCancel() {
                Toast.makeText(LoginActivity.this, "Login Cancelled !!", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onError(FacebookException error) {

                Toast.makeText(LoginActivity.this, "Can't login with facebook please check internet connection ..", Toast.LENGTH_LONG).show();
            }

        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        accessTokenTracker.stopTracking();
        profileTracker.stopTracking();
    }


    String emai = "gfdgfdgfd";

    private void LoginWithEmailPassword(){

        String EmailL = emailL.getText().toString().trim();
        String PasswordL = passwordL.getText().toString().trim();

        if (TextUtils.isEmpty(EmailL)) {
            Toast.makeText(getApplicationContext(), "Enter email address!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(PasswordL)) {
            Toast.makeText(getApplicationContext(), "Enter password!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (passwordL.length() < 6) {
            Toast.makeText(getApplicationContext(), "Password too short, enter minimum 6 characters!", Toast.LENGTH_SHORT).show();
            return;
        }


        mAuth.signInWithEmailAndPassword(EmailL, PasswordL)
                .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            // there was an error
                            if (passwordL.length() < 6) {
                                passwordL.setError("Wrong Password");
                            } else {
                                Toast.makeText(LoginActivity.this, "Auth Failed", Toast.LENGTH_LONG).show();
                            }
                        } else {
                            Intent intent = new Intent(LoginActivity.this, MapsActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    }
                });

    }


    private void handleFacebookAccessToken(AccessToken token) {

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information

                            FirebaseUser user = mAuth.getCurrentUser();
                            Profile profile = Profile.getCurrentProfile();
//                            Intent main = new Intent(getApplicationContext() , MapsActivity.class);
//              /              // sending user data to MapsActivity from facebook account :
//
//                            main.putExtra("name" , profile.getName());
//                            main.putExtra("imageUrl" , profile.getProfilePictureUri(100,100).toString());
//                            main.putExtra("id",profile.getId());



                            Toast.makeText(getApplicationContext(), "Welcome "+ profile.getName() + " :)", Toast.LENGTH_LONG).show();
//                            startActivity(main);
//                            finish();

                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(getApplicationContext(), "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();

                        }

                        // ...
                    }
                });
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        mAuth.addAuthStateListener(mAuthListener);
    }


    @Override
    public void onBackPressed() {
        finishAffinity();
    }

    private void anime(){
        RelativeLayout relativeLayout = (RelativeLayout)findViewById(R.id.login_id);
        relativeLayout.animate().alpha(0.0f).scaleX(25f).scaleY(25f).setDuration(1000).setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
//                startActivity(new Intent(getApplicationContext(), MapsActivity.class));
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
    }

}

