package com.cr7.budgetapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.shashank.sony.fancytoastlib.FancyToast;

public class LogInActivity extends AppCompatActivity implements View.OnClickListener {
    //Declaring the instance variables
    private TextInputEditText editTextLogInUsername, editTextLogInPassword;
    private Button buttonLogIn;
    private TextView textViewSignUp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);
        //Linking the instance variables to their Respective UI components
        editTextLogInUsername = findViewById(R.id.editTextLogInUsername);
        editTextLogInPassword = findViewById(R.id.editTextLogInPassword);
        buttonLogIn = findViewById(R.id.buttonLogIn);
        textViewSignUp = findViewById(R.id.textViewSignUp);
        //Setting the onClickListener for th
        buttonLogIn.setOnClickListener(this);
        textViewSignUp.setOnClickListener(this);
        //Checking if there is already a user that has logged in sp that it can directly switch
        // to the LoggedInActivity
        try {
            if (ParseUser.getCurrentUser() != null) {
                Intent intent = new Intent(LogInActivity.this, LoggedInActivity.class);
                startActivity(intent);
                finish();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //The OnClickListener's onClick function
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            //The Log In button's onClickListener
            case R.id.buttonLogIn:
                if (!editTextLogInUsername.getText().toString().isEmpty()) {
                    if (!editTextLogInPassword.getText().toString().isEmpty()) {
                        ParseUser.logInInBackground(editTextLogInUsername.getText().toString(),
                                editTextLogInPassword.getText().toString(), new LogInCallback() {
                                    @Override
                                    public void done(ParseUser user, ParseException e) {
                                        if (user != null && e == null) {
                                            FancyToast.makeText(LogInActivity.this, "Login " +
                                                            "successful",
                                                    FancyToast.LENGTH_LONG, FancyToast.SUCCESS,
                                                    false).show();
                                            //Switching to LoggedInActivity
                                            Intent intent = new Intent(LogInActivity.this,
                                                    LoggedInActivity.class);
                                            startActivity(intent);
                                            finish();
                                        } else {
                                            FancyToast.makeText(LogInActivity.this, e.getMessage(),
                                                    FancyToast.LENGTH_LONG, FancyToast.ERROR,
                                                    false).show();
                                        }
                                    }
                                });
                    } else {
                        FancyToast.makeText(LogInActivity.this, "Please enter your password",
                                FancyToast.LENGTH_LONG, FancyToast.ERROR, false).show();
                    }
                } else {
                    FancyToast.makeText(LogInActivity.this, "Please enter your username",
                            FancyToast.LENGTH_LONG, FancyToast.ERROR, false).show();
                }
                break;
            //The Sign Up text's onClickListener
            case R.id.textViewSignUp:
                Intent intent = new Intent(LogInActivity.this, SignUpActivity.class);
                startActivity(intent);
                finish();
                break;
        }
    }
}
