package com.example.firechat.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;
import com.example.firechat.R;
import com.example.firechat.data.KeysAndValues;
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

    private TextInputLayout edtUsrName, edtUsrStatus;
    private MaterialButton settingsUpdateButton;
    private CircleImageView profileImage;
    private Toolbar settingsToolbar;

    private FirebaseAuth mAuth;
    private DatabaseReference rootReference;
    private StorageReference userProfileImagesRef;

    private static final int galleryPick = 1;
    private String currentUserId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        initializeFirebaseComponents();
        findViews();
        initialViewsListeners();
        retrieveUserInfo();


    }

    private void retrieveUserInfo() {
        rootReference.child(KeysAndValues.USERS).child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if((snapshot.exists()) && (snapshot.hasChild(KeysAndValues.NAME) && (snapshot.hasChild(KeysAndValues.IMAGE)))){
                    String retrieveUsrName = snapshot.child(KeysAndValues.NAME).getValue().toString();
                    String retrieveUsrStatus = snapshot.child(KeysAndValues.STATUS).getValue().toString();
                    String retrieveUsrProfileImg= snapshot.child(KeysAndValues.IMAGE).getValue().toString();

                    edtUsrName.getEditText().setText(retrieveUsrName);
                    edtUsrStatus.getEditText().setText(retrieveUsrStatus);
                    Picasso.get().load(retrieveUsrProfileImg).into(profileImage);


                }
                else if((snapshot.exists()) && (snapshot.hasChild(KeysAndValues.NAME))){
                    String retrieveUsrName = snapshot.child(KeysAndValues.NAME).getValue().toString();
                    String retrieveUsrStatus = snapshot.child(KeysAndValues.STATUS).getValue().toString();

                    edtUsrName.getEditText().setText(retrieveUsrName);
                    edtUsrStatus.getEditText().setText(retrieveUsrStatus);

                }
                else{
                    edtUsrName.setVisibility(View.VISIBLE);
                    displayToast("Please set & update your profile information");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
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
                Task<Uri>uriTask = uploadTask.continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }

                    // Continue with the task to get the download URL
                    return filePath.getDownloadUrl();
                })
                        .addOnCompleteListener(task -> {

                            if (task.isSuccessful()) {

                                //  Uri downloadUri = task.getResult();
                                // Getting image upload ID.
                                final String downloadURL = task.getResult().toString();
                                String ImageUploadId = rootReference.push().getKey();

                                // Adding image upload id s child element into databaseReference.

                                rootReference.child(KeysAndValues.USERS).child(currentUserId).child(KeysAndValues.IMAGE)
                                        .setValue(downloadURL);

                            }

                        });


            }}
        }
    private void updateSettings() {
        String setUsrName =String.valueOf(this.edtUsrName.getEditText().getText());
        String setUsrStatus = String.valueOf(this.edtUsrStatus.getEditText().getText());

        if(TextUtils.isEmpty(setUsrName) || TextUtils.isEmpty(setUsrName))
            displayToast("Please check all fields are filled");
        else{
            HashMap<String,Object> profileMap = new HashMap<>();
            profileMap.put(KeysAndValues.UID,currentUserId);
            profileMap.put(KeysAndValues.NAME,setUsrName);
            profileMap.put(KeysAndValues.STATUS,setUsrStatus);
            rootReference.child(KeysAndValues.USERS).child(currentUserId).updateChildren(profileMap).addOnCompleteListener(task -> {

                if(task.isSuccessful()){
                    activityShifter(SettingsActivity.this,MainActivity.class,true,true);
                    displayToast("Profile updated successfully");
                }
                else{
                    String message = task.getException().toString();
                    displayToast("Error: "+message);
                }
            });

        }
    }
    private void initializeFirebaseComponents() {
        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        rootReference = FirebaseDatabase.getInstance().getReference();
        userProfileImagesRef = FirebaseStorage.getInstance().getReference().child(KeysAndValues.PROFILE_IMAGES);

    }
    private void initialViewsListeners() {
        settingsUpdateButton.setOnClickListener(v ->updateSettings());
        profileImage.setOnClickListener(v ->updateUserImage());
    }
    private void updateUserImage() {

        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1, 1)
                .start(this);

    }

    private void activityShifter(Activity from,Class to, boolean addFlags , boolean finish){
        Intent intent = new Intent(from,to);
        if(addFlags)
            // user can't go back if the back button is pressed
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        if(finish)
            finish();

    }
    private void displayToast(String message){
        Toast.makeText(this,message,Toast.LENGTH_SHORT).show();

    }
    private void findViews() {
        edtUsrName = findViewById(R.id.settings_EDT_set_usr_name);
        edtUsrStatus = findViewById(R.id.settings_EDT_set_usr_status);
        settingsUpdateButton = findViewById(R.id.settings_BTN_update);
        profileImage = findViewById(R.id.img_profile_image);
        edtUsrName.setVisibility(View.INVISIBLE);
        settingsToolbar = findViewById(R.id.settings_toolbar);
        setSupportActionBar(settingsToolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setTitle(KeysAndValues.ACCOUNT_SETTINGS);

    }
}