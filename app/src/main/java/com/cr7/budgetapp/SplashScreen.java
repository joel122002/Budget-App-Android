package com.cr7.budgetapp;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class SplashScreen extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Transitioning to LogInActivity
        Intent intent = new Intent(SplashScreen.this, LogInActivity.class);
        startActivity(intent);
        finish();
    }
}

