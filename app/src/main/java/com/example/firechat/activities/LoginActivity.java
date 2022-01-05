package com.example.firechat.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.firechat.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private FirebaseUser currentUser;
    private TextInputLayout login_EDT_email,login_EDT_password;
    private MaterialButton login_BTN_forgot_password,login_BTN_login,login_BTN_register,login_BTN_login_phone;

    private FirebaseAuth mAuth;

    private ProgressDialog loadingBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
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
        login_BTN_login.setOnClickListener(v ->login());
    }

    private void login() {
        String email =String.valueOf(login_EDT_email.getEditText().getText());
        String password = String.valueOf(login_EDT_password.getEditText().getText());
        if(TextUtils.isEmpty(email) || TextUtils.isEmpty(password))
            displayToast("Please check all fields are filled");
        else{
            setLoadingBarAttr();
            mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {

                    if(task.isSuccessful()){
                        passUserToMainActivity();
                        displayToast("Account logged in successfully" );
                    }
                    else{
                        String message = task.getException().toString();
                        displayToast("Error : " + message);

                    }
                    loadingBar.dismiss();
                }
            });
        }
    }
    private void setLoadingBarAttr(){
        loadingBar.setTitle("Sign in");
        loadingBar.setMessage("Please wait...");
        // loading bar will not go away until user creation process is done
        loadingBar.setCanceledOnTouchOutside(true);
        loadingBar.show();

    }
    private void displayToast(String message){
        Toast.makeText(this,message,Toast.LENGTH_SHORT).show();

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
        loadingBar = new ProgressDialog(this);

    }
}