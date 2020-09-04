package com.example.taxiapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class PassengerSignInActivity extends AppCompatActivity {

    private static final String TAG = "PassengerSignInActivity";

    private TextInputLayout textInputEmailTIL,
            textInputNameTIL,
            textInputPassTIL,
            textInputConfirmPassTIL;
    private Button loginSignInBtn;
    private TextView toggleLoginSignUpTV;

    private FirebaseAuth auth;

    private Boolean isLoginMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_passenger_sign_in);

        bind();

        auth = FirebaseAuth.getInstance();

        if (auth.getCurrentUser() != null) {
            moveToMap();
        }
    }

    private void bind() {
        textInputEmailTIL = findViewById(R.id.textInputEmail);
        textInputNameTIL = findViewById(R.id.textInputName);
        textInputPassTIL = findViewById(R.id.textInputPass);
        textInputConfirmPassTIL = findViewById(R.id.textInputConfirmPass);

        loginSignInBtn = findViewById(R.id.loginSignInBtn);
        toggleLoginSignUpTV = findViewById(R.id.toggleLoginSignUpTV);
    }

    private boolean validateEmail() {
        String emailVal = textInputEmailTIL.getEditText().getText().toString().trim();

        textInputEmailTIL.setError("");

        if (emailVal.isEmpty()) {
            textInputEmailTIL.setError("Please set email");
            return false;
        }

        return true;
    }

    private boolean validateName() {
        String nameVal = textInputNameTIL.getEditText().getText().toString().trim();

        textInputNameTIL.setError("");

        if (nameVal.isEmpty()) {
            textInputNameTIL.setError("Please set Name");
            return false;
        }

        if (nameVal.length() > 15) {
            textInputNameTIL.setError("Name must be less or equal then 15 char");
            return false;
        }

        return true;
    }

    private boolean validatePass() {
        String passVal = textInputPassTIL.getEditText().getText().toString().trim();
        String passConfirmVal = textInputConfirmPassTIL.getEditText().getText().toString().trim();

        textInputPassTIL.setError("");

        if (passVal.isEmpty()) {
            textInputPassTIL.setError("Please set password");
            return false;
        }

        if (passVal.length() < 6) {
            textInputPassTIL.setError("Password must be more than 6 char");
            return false;
        }

        if (!isLoginMode && !passVal.equals(passConfirmVal)) {
            textInputPassTIL.setError("Password must be equal confirm value");
            return false;
        }

        return true;
    }

    public void loginSignUp(View view) {
        Boolean valid = true;

        valid = valid & validateEmail();
        valid = valid & validateName();
        valid = valid & validatePass();

        if (!valid) {
            return;
        }

        String userEmail = textInputEmailTIL.getEditText().getText().toString().toLowerCase().trim();
        String userPass = textInputPassTIL.getEditText().getText().toString().toLowerCase().trim();

        if (isLoginMode) {
            logIn(userEmail, userPass);
            return;
        }

        signUp(userEmail, userPass);
    }

    public void toggleLoginSignUp(View view) {
        isLoginMode = !isLoginMode;

        if (isLoginMode) {
            loginSignInBtn.setText("Login");
            toggleLoginSignUpTV.setText("Tap to Sign In");
            textInputConfirmPassTIL.setVisibility(View.GONE);
            return;
        }

        loginSignInBtn.setText("Sign Up");
        toggleLoginSignUpTV.setText("Tap to Login");
        textInputConfirmPassTIL.setVisibility(View.VISIBLE);
    }


    private void signUp(String userEmail, String userPass) {
        auth.createUserWithEmailAndPassword(userEmail, userPass)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "createUserWithEmail:success");
                            FirebaseUser user = auth.getCurrentUser();
                            moveToMap();
                        } else {
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(PassengerSignInActivity.this,
                                    "Authentication failed.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void logIn(String userEmail, String userPass) {
        auth.signInWithEmailAndPassword(userEmail, userPass)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "signInWithEmail:success");
                            FirebaseUser user = auth.getCurrentUser();
                            moveToMap();
                        } else {
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(PassengerSignInActivity.this,
                                    "Authentication failed.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void moveToMap() {
        startActivity(new Intent(PassengerSignInActivity.this, PassengerMapsActivity.class));
    }
}