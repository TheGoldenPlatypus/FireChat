package com.example.firechat.activities;


import androidx.appcompat.app.AppCompatActivity;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;
import com.example.firechat.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    private TextInputLayout edtEmail, edtPassword;
    private MaterialButton loginButton, registerButton;

    private FirebaseAuth mAuth;

    private ProgressDialog loadingBar;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initializeFirebaseComponents();
        findViews();
        initializeViewsListeners();
    }

    private void login() {
        String email =String.valueOf(edtEmail.getEditText().getText());
        String password = String.valueOf(edtPassword.getEditText().getText());
        if(TextUtils.isEmpty(email) || TextUtils.isEmpty(password))
            displayToast("Please check all fields are filled");
        else{
            setLoadingBarAttr();
            mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(task -> {

                if(task.isSuccessful()){
                    activityShifter(LoginActivity.this,MainActivity.class,true,true);
                    displayToast("Account logged in successfully" );
                }
                else{
                    String message = task.getException().toString();
                    displayToast("Error : " + message);

                }
                loadingBar.dismiss();
            });
        }
    }
    private void activityShifter(Activity from, Class to, boolean addFlags , boolean finish){
        Intent intent = new Intent(from,to);
        if(addFlags)
            // user can't go back if the back button is pressed
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        if(finish)
            finish();

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
    private void initializeViewsListeners() {
        registerButton.setOnClickListener(v -> activityShifter(LoginActivity.this,RegisterActivity.class,false,false));
        loginButton.setOnClickListener(v ->login());
    }
    private void initializeFirebaseComponents() {
        mAuth = FirebaseAuth.getInstance();
    }
    private void findViews() {
        edtEmail = findViewById(R.id.login_EDT_email);
        edtPassword = findViewById(R.id.login_EDT_password);
        loginButton = findViewById(R.id.login_BTN_login);
        registerButton = findViewById(R.id.login_BTN_register);
        loadingBar = new ProgressDialog(this);

    }
}