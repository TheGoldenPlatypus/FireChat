package com.example.firechat.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.example.firechat.R;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {

    private TextInputLayout settings_EDT_set_usr_name,settings_EDT_set_usr_status;
    private MaterialButton settings_BTN_update;
    private CircleImageView img_profile_image;

    private FirebaseAuth mAuth;
    private String currentUserId;
    private DatabaseReference rootReference;

    private static final int galleryPick = 1;
    private StorageReference userProfileImagesRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        rootReference = FirebaseDatabase.getInstance().getReference();
        userProfileImagesRef = FirebaseStorage.getInstance().getReference().child("Profile Images");

        findViews();

        initialViewsListeners();
        retrieveUserInfo();


    }

    private void retrieveUserInfo() {
        rootReference.child("Users").child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if((snapshot.exists()) && (snapshot.hasChild("name") && (snapshot.hasChild("image")))){
                    String retrieveUsrName = snapshot.child("name").getValue().toString();
                    String retrieveUsrStatus = snapshot.child("status").getValue().toString();
                    String retrieveUsrProfileImg= snapshot.child("image").getValue().toString();

                    settings_EDT_set_usr_name.getEditText().setText(retrieveUsrName);
                    settings_EDT_set_usr_status.getEditText().setText(retrieveUsrStatus);
                    Picasso.get().load(retrieveUsrProfileImg).into(img_profile_image);


                }
                else if((snapshot.exists()) && (snapshot.hasChild("name"))){
                    String retrieveUsrName = snapshot.child("name").getValue().toString();
                    String retrieveUsrStatus = snapshot.child("status").getValue().toString();

                    settings_EDT_set_usr_name.getEditText().setText(retrieveUsrName);
                    settings_EDT_set_usr_status.getEditText().setText(retrieveUsrStatus);

                }
                else{
                    settings_EDT_set_usr_name.setVisibility(View.VISIBLE);
                    displayToast("Please set & update your profile information");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void initialViewsListeners() {
        settings_BTN_update.setOnClickListener(v ->updateSettings());
        img_profile_image.setOnClickListener(v ->updateUserImage());
    }
    private void updateUserImage() {

        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1, 1)
                .start(this);

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode==galleryPick  &&  resultCode==RESULT_OK  &&  data!=null)
        {
            Uri ImageUri = data.getData();

        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE)
        {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if(resultCode == RESULT_OK)
            {

                Uri resultUri = result.getUri();
                final StorageReference filePath  = userProfileImagesRef.child(currentUserId + ".jpg");
                UploadTask uploadTask = filePath.putFile(resultUri);
                Task<Uri>uriTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        if (!task.isSuccessful()) {
                            throw task.getException();
                        }

                        // Continue with the task to get the download URL
                        return filePath.getDownloadUrl();
                    }
                })
                        .addOnCompleteListener(new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(@NonNull Task<Uri> task) {

                                if (task.isSuccessful()) {

                                    //  Uri downloadUri = task.getResult();
                                    // Getting image upload ID.
                                    final String downloadURL = task.getResult().toString();
                                    String ImageUploadId = rootReference.push().getKey();

                                    // Adding image upload id s child element into databaseReference.

                                    rootReference.child("Users").child(currentUserId).child("image")
                                            .setValue(downloadURL);

                                } else {

                                }

                            }
                        });


            }}
        }
    private void updateSettings() {
        String setUsrName =String.valueOf(settings_EDT_set_usr_name.getEditText().getText());
        String setUsrStatus = String.valueOf(settings_EDT_set_usr_status.getEditText().getText());

        if(TextUtils.isEmpty(setUsrName) || TextUtils.isEmpty(setUsrName))
            displayToast("Please check all fields are filled");
        else{
            HashMap<String,Object> profileMap = new HashMap<>();
            profileMap.put("uid",currentUserId);
            profileMap.put("name",setUsrName);
            profileMap.put("status",setUsrStatus);
            rootReference.child("Users").child(currentUserId).setValue(profileMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {

                    if(task.isSuccessful()){
                        passUserToMainActivity();
                        displayToast("Profile updated successfully");
                    }
                    else{
                        String message = task.getException().toString();
                        displayToast("Error: "+message);
                    }
                }
            });

        }
    }
    private void passUserToMainActivity() {
        Intent mainIntent = new Intent(SettingsActivity.this,MainActivity.class);
        // user can't go back if the back button is pressed
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();

    }
    private void displayToast(String message){
        Toast.makeText(this,message,Toast.LENGTH_SHORT).show();

    }
    private void findViews() {
        settings_EDT_set_usr_name  = findViewById(R.id.settings_EDT_set_usr_name);
        settings_EDT_set_usr_status  = findViewById(R.id.settings_EDT_set_usr_status);
        settings_BTN_update  = findViewById(R.id.settings_BTN_update);
        img_profile_image  = findViewById(R.id.img_profile_image);

        settings_EDT_set_usr_name.setVisibility(View.INVISIBLE);

    }
}