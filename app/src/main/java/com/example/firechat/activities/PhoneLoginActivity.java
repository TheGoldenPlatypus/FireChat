package com.example.firechat.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.example.firechat.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class PhoneLoginActivity extends AppCompatActivity {

    private TextInputLayout inputPhoneNr,inputVerificationCode;
    private MaterialButton sendVerificationButton,verifyButton;

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private FirebaseAuth mAuth;

    private ProgressDialog loadingBar;

    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_login);
        mAuth = FirebaseAuth.getInstance();
        findViews();
      //  initialViewsListeners();

        sendVerificationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phoneNumber =String.valueOf(inputPhoneNr.getEditText().getText());
                if(TextUtils.isEmpty(phoneNumber))
                    displayToast("Phone number is required");
                else{
                    loadingBar.setTitle("Phone Verification");
                    loadingBar.setMessage("Please wait while we are authenticating your phone");
                    loadingBar.setCanceledOnTouchOutside(false);
                    loadingBar.show();
                    PhoneAuthProvider.getInstance().verifyPhoneNumber(phoneNumber,60, TimeUnit.SECONDS,PhoneLoginActivity.this,mCallbacks);

                }
            }
        });

        verifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendVerificationButton.setVisibility(View.INVISIBLE);
                inputPhoneNr.setVisibility(View.INVISIBLE);
                String verificationCode =String.valueOf(inputVerificationCode.getEditText().getText());
                if(TextUtils.isEmpty(verificationCode))
                    displayToast("Verification code is required");
                else{
                    loadingBar.setTitle("Code Verification");
                    loadingBar.setMessage("Please wait while we are verifying verification code");
                    loadingBar.setCanceledOnTouchOutside(false);
                    loadingBar.show();

                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, verificationCode);
                    signInWithPhoneAuthCredential(credential);
                }

            }
        });

        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {

                signInWithPhoneAuthCredential(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                loadingBar.dismiss();
            //    displayToast("Invalid phone number, please try again");
                displayToast(e.getMessage());
                sendVerificationButton.setVisibility(View.VISIBLE);
                inputPhoneNr.setVisibility(View.VISIBLE);
                verifyButton.setVisibility(View.INVISIBLE);
                inputVerificationCode.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onCodeSent(@NonNull String verificationId,
                                   @NonNull PhoneAuthProvider.ForceResendingToken token) {

                // Save verification ID and resending token so we can use them later
                mVerificationId = verificationId;
                mResendToken = token;

                loadingBar.dismiss();
                displayToast("Code has been sent, please check your SMS inbox");
                sendVerificationButton.setVisibility(View.INVISIBLE);
                inputPhoneNr.setVisibility(View.INVISIBLE);
                verifyButton.setVisibility(View.VISIBLE);
                inputVerificationCode.setVisibility(View.VISIBLE);
            }
        };

    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            loadingBar.dismiss();
                            displayToast("Congratulations, you're logged in successfully...");
                            passUserToMainActivity();
                        }
                        else {
                            String message = task.getException().toString();
                            displayToast("Error : " + message);
                        }
                    }

                });
    }

    private void passUserToMainActivity() {
        Intent mainIntent = new Intent(PhoneLoginActivity.this,MainActivity.class);
        startActivity(mainIntent);
        finish();

    }

    private void displayToast(String message){
        Toast.makeText(this,message,Toast.LENGTH_SHORT).show();

    }

    private void findViews() {
        inputPhoneNr = findViewById(R.id.register_EDT_phone);
        inputVerificationCode = findViewById(R.id.register_EDT_verification_code);
        sendVerificationButton = findViewById(R.id.register_BTN_send_code);
        verifyButton = findViewById(R.id.register_BTN_verify);

        loadingBar = new ProgressDialog(this);
    }
}