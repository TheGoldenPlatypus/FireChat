package com.example.firechat.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.example.firechat.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private FirebaseUser currentUser;
    private TextInputLayout login_EDT_email,login_EDT_password;
    private MaterialButton login_BTN_forgot_password,login_BTN_login,login_BTN_register,login_BTN_login_phone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        findViews();
        initializeViewsListeners();
    }



    @Override
    protected void onStart() {
        super.onStart();
        // user is logged in
        if(currentUser != null)
        {
            // send user back to the main activity
            passUserToMainActivity();

        }
    }

    private void initializeViewsListeners() {
        login_BTN_register.setOnClickListener(v ->passUserToRegisterActivity());
    }


    private void passUserToMainActivity() {

        Intent mainIntent = new Intent( LoginActivity.this,MainActivity.class);
        startActivity(mainIntent);

    }

    private void passUserToRegisterActivity() {

        Intent registerIntent = new Intent( LoginActivity.this,RegisterActivity.class);
        startActivity(registerIntent);

    }

    private void findViews() {
        login_EDT_email  = findViewById(R.id.login_EDT_email);
        login_EDT_password  = findViewById(R.id.login_EDT_password);
        login_BTN_forgot_password  = findViewById(R.id.login_BTN_forgot_password);
        login_BTN_login  = findViewById(R.id.login_BTN_login);
        login_BTN_register  = findViewById(R.id.login_BTN_register);
        login_BTN_login_phone  = findViewById(R.id.login_BTN_login_phone);
    }
}