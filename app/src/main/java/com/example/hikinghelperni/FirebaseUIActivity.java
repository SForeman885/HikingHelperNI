package com.example.hikinghelperni;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract;
import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;
import java.util.List;

public class FirebaseUIActivity extends AppCompatActivity {

    // variables for Firebase Auth
    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    //new instance of my FirebaseDatabase class
    private FirebaseDatabase db = new FirebaseDatabase();
    //list of all login options I want to set up
    List<AuthUI.IdpConfig> providers = Arrays.asList(
            new AuthUI.IdpConfig.EmailBuilder().build(),
            new AuthUI.IdpConfig.GoogleBuilder().build(),
            new AuthUI.IdpConfig.FacebookBuilder().build());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        createSignInIntent();
    }

    //setting up signin launcher with listener to handle after signin attempt
    private final ActivityResultLauncher<Intent> signInLauncher = registerForActivityResult(
            new FirebaseAuthUIActivityResultContract(),
            result -> onSignInResult(result)
    );

    public void createSignInIntent() {
        mFirebaseAuth = FirebaseAuth.getInstance();

        // call auth listener for firebase authentication authstatechanged
        mAuthStateListener = firebaseAuth -> {
            FirebaseUser user = firebaseAuth.getCurrentUser();

            if (user != null) {
                // if the user is already authenticated go to home screen
                Intent i = new Intent(FirebaseUIActivity.this, MainActivity.class);
                startActivity(i);
                // we are calling finish method to kill our
                // firebaseuiactivity which is displaying our login ui.
                finish();
            } else {
                // call when user is not authenticated previously.
                Intent signInIntent =
                        // get our authentication instance.
                        AuthUI.getInstance()
                                // create our sign in intent
                                .createSignInIntentBuilder()

                                // smart lock is used to check if the user
                                // is authentication through different devices.
                                // currently I am disabling it.
                                .setIsSmartLockEnabled(false)

                                .setAvailableProviders(providers)
                                // customizing theme and adding logo
                                .setTheme(R.style.Theme_SignIn)
                                .setLogo(R.drawable.logo)
                                // build our login screen.
                                .build();
                signInLauncher.launch(signInIntent);
            }
        };
    }

    private void onSignInResult(FirebaseAuthUIAuthenticationResult result) {
        IdpResponse response = result.getIdpResponse();
        if (result.getResultCode() == RESULT_OK) {
            // Successfully signed in
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            db.addNewUser(user.getUid());
            // ...
        } else {
            //if response is null the user canceled the sign-in flow using the back button.
            if(response == null) {
                Log.d(this.getClass().toString(), "Sign Up cancelled by user");
            }
            else {
                //else log error returned by firebase
                Log.d(this.getClass().toString(), response.getError().toString());
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //we are calling our auth listener method on app resume.
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        //here we are calling remove auth listener method on stop.
        mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
    }
}
