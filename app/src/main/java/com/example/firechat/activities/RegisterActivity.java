package com.example.firechat.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.firechat.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;

import de.hdodenhof.circleimageview.CircleImageView;

public class RegisterActivity extends AppCompatActivity {

    private TextInputLayout register_EDT_email,register_EDT_password;
    private MaterialButton register_BTN_create_account,register_BTN_login;


    private FirebaseAuth mAuth;
    private DatabaseReference rootReference;

    private ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        mAuth = FirebaseAuth.getInstance();
        rootReference = FirebaseDatabase.getInstance().getReference();
        findViews();
        initialViewsListeners();
    }


    private void createNewAccount(){
      String email =String.valueOf(register_EDT_email.getEditText().getText());
      String password = String.valueOf(register_EDT_password.getEditText().getText());
      if(TextUtils.isEmpty(email) || TextUtils.isEmpty(password))
          displayToast("Please check all fields are filled");
      else{
          setLoadingBarAttr();
            mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){

                        String currentUserId = mAuth.getCurrentUser().getUid();
                        rootReference.child("Users").child(currentUserId).setValue("");

                        passUserToMainActivity();
                        displayToast("Account created successfully" );
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
    private void displayToast(String message){
        Toast.makeText(this,message,Toast.LENGTH_SHORT).show();

    }
    private void setLoadingBarAttr(){
        loadingBar.setTitle("Creating new account");
        loadingBar.setMessage("Please wait, while we are  creating your new account");
        // loading bar will not go away until user creation process is done
        loadingBar.setCanceledOnTouchOutside(true);
        loadingBar.show();

    }

    private void initialViewsListeners() {
        register_BTN_login.setOnClickListener(v ->passUserToLoginActivity());
        register_BTN_create_account.setOnClickListener(v ->createNewAccount());

    }
    private void passUserToMainActivity() {
        Intent mainIntent = new Intent(RegisterActivity.this,MainActivity.class);
        // user can't go back if the back button is pressed
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();

    }
    private void passUserToLoginActivity() {
        Intent loginIntent = new Intent(RegisterActivity.this,LoginActivity.class);
        startActivity(loginIntent);

    }
    private void findViews() {
        register_EDT_email  = findViewById(R.id.register_EDT_email);
        register_EDT_password  = findViewById(R.id.register_EDT_password);
        register_BTN_create_account  = findViewById(R.id.register_BTN_create_account);
        register_BTN_login  = findViewById(R.id.register_BTN_login);
        loadingBar = new ProgressDialog(this);

    }
}