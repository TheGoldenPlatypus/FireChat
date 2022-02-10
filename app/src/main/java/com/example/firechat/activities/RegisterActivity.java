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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity {

    private TextInputLayout edtRegisterEmail, edtRegisterPassword;
    private MaterialButton createAccountButton, loginButton;
    private ProgressDialog loadingBar;

    private FirebaseAuth mAuth;
    private DatabaseReference rootReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        initializeFirebaseComponents();
        findViews();
        initialViewsListeners();
    }

    private void createNewAccount(){
      String email =String.valueOf(edtRegisterEmail.getEditText().getText());
      String password = String.valueOf(edtRegisterPassword.getEditText().getText());
      if(TextUtils.isEmpty(email) || TextUtils.isEmpty(password))
          displayToast("Please check all fields are filled");
      else{
          setLoadingBarAttr();
            mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(task -> {
                if(task.isSuccessful()){

                    String currentUserId = mAuth.getCurrentUser().getUid();
                    rootReference.child("Users").child(currentUserId).setValue("");

                    activityShifter(RegisterActivity.this,MainActivity.class,true,true);
                    displayToast("Account created successfully" );
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
        loadingBar.setTitle("Creating new account");
        loadingBar.setMessage("Please wait, while we are  creating your new account");
        // loading bar will not go away until user creation process is done
        loadingBar.setCanceledOnTouchOutside(true);
        loadingBar.show();

    }
    private void initialViewsListeners() {
        loginButton.setOnClickListener(v ->activityShifter(RegisterActivity.this,LoginActivity.class,false,false));
        createAccountButton.setOnClickListener(v ->createNewAccount());

    }
    private void displayToast(String message){
        Toast.makeText(this,message,Toast.LENGTH_SHORT).show();

    }
    private void initializeFirebaseComponents() {
        mAuth = FirebaseAuth.getInstance();
        rootReference = FirebaseDatabase.getInstance().getReference();

    }
    private void findViews() {
        edtRegisterEmail = findViewById(R.id.register_EDT_email);
        edtRegisterPassword = findViewById(R.id.register_EDT_password);
        createAccountButton = findViewById(R.id.register_BTN_create_account);
        loginButton = findViewById(R.id.register_BTN_login);
        loadingBar = new ProgressDialog(this);

    }
}