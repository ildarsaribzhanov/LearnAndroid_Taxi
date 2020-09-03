package com.example.taxiapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;

public class DriverSignInActivity extends AppCompatActivity {

    private TextInputLayout textInputEmailTIL,
            textInputNameTIL,
            textInputPassTIL,
            textInputConfirmPassTIL;
    private Button loginSignInBtn;
    private TextView toggleLoginSignUpTV;

    Boolean isLoginMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_sign_in);

        bind();
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

        if (!passVal.equals(passConfirmVal)) {
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

        String userInput = "Email: " + textInputEmailTIL.getEditText().getText().toString().trim()
                + "\nName: " + textInputNameTIL.getEditText().getText().toString().trim()
                + "\nPass: " + textInputPassTIL.getEditText().getText().toString().trim();
        Toast.makeText(this, userInput, Toast.LENGTH_LONG).show();
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
}