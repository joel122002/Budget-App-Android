package com.cr7.budgetapp;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.google.android.material.textfield.TextInputEditText;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;
import com.shashank.sony.fancytoastlib.FancyToast;

public class SignUpActivity extends AppCompatActivity implements View.OnClickListener{
    //Declaration of Instance variables
    private Button buttonSignUp;
    private TextInputEditText editTextSignUpUsername, editTextSignUpPassword, editTextSignUpPasswordConfirm;
    private TextView textViewLogIn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        //Linking the instance variables to their Respective UI components
        editTextSignUpUsername = findViewById(R.id.editTextSignUpUsername);
        editTextSignUpPassword = findViewById(R.id.editTextSignUpPassword);
        editTextSignUpPasswordConfirm = findViewById(R.id.editTextSignUpPasswordConfirm);
        buttonSignUp = findViewById(R.id.buttonSignUp);
        textViewLogIn = findViewById(R.id.textViewLogIn);
        //Setting Up the OnClickListeners for the sign up button and login text
        buttonSignUp.setOnClickListener(this);
        textViewLogIn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        //Switch statement to identify the UI component which is clicked
        switch (v.getId())
        {
            //The Sign Up button onClickListener
            case R.id.buttonSignUp :
                if (!editTextSignUpUsername.getText().toString().isEmpty())
                {
                    if (!editTextSignUpPassword.getText().toString().isEmpty())
                    {
                        if (!editTextSignUpPasswordConfirm.getText().toString().isEmpty())
                        {
                            if (editTextSignUpPassword.getText().toString().matches(editTextSignUpPasswordConfirm.getText().toString()))
                            {
                                ParseUser newuser = new ParseUser();
                                newuser.setUsername(editTextSignUpUsername.getText().toString());
                                newuser.setPassword(editTextSignUpPassword.getText().toString());
                                newuser.signUpInBackground(new SignUpCallback() {
                                    @Override
                                    public void done(ParseException e) {
                                        if (e == null)
                                        {
                                            FancyToast.makeText(SignUpActivity.this, "SignUp successful", FancyToast.LENGTH_LONG, FancyToast.SUCCESS, false).show();
                                            //Switching to Logged in activity
                                            Intent intent = new Intent(SignUpActivity.this, LoggedInActivity.class);
                                            startActivity(intent);
                                            finish();
                                        }
                                        else
                                        {
                                            FancyToast.makeText(SignUpActivity.this, e.getMessage(), FancyToast.LENGTH_LONG, FancyToast.ERROR, false).show();
                                        }
                                    }
                                });
                            }
                            else
                            {
                                FancyToast.makeText(SignUpActivity.this, "Passwords do not match", FancyToast.LENGTH_LONG, FancyToast.ERROR, false).show();
                            }
                        }
                        else
                        {
                            FancyToast.makeText(SignUpActivity.this, "Please confirm your password", FancyToast.LENGTH_LONG, FancyToast.ERROR, false).show();
                        }
                    }
                    else
                    {
                        FancyToast.makeText(SignUpActivity.this, "Please enter a password", FancyToast.LENGTH_LONG, FancyToast.ERROR, false).show();
                    }
                }
                else
                {
                    FancyToast.makeText(SignUpActivity.this, "Please enter a username", FancyToast.LENGTH_LONG, FancyToast.ERROR, false).show();
                }
                break;
            //The Log in text onClickListener
            case R.id.textViewLogIn :
                Intent intent = new Intent(SignUpActivity.this, LogInActivity.class);
                startActivity(intent);
                finish();
                break;
        }
    }
}
