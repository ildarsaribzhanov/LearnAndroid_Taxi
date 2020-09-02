package com.example.taxiapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class ChoseModeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chose_mode);
    }

    public void goToPassengerSignIn(View view) {
        startActivity(new Intent(ChoseModeActivity.this,
                PassangerSignInActivity.class));
    }

    public void goToDriverSignIn(View view) {
        startActivity(new Intent(ChoseModeActivity.this,
                DriverSignInActivity.class));
    }
}