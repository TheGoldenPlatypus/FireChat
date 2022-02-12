package com.example.firechat.activities;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.firechat.R;
import com.example.firechat.data.KeysAndValues;
import com.example.firechat.fragments.FeedFragment;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class PostActivity extends AppCompatActivity {

    private Toolbar tb;
    private ProgressDialog loadingBar;

    private ImageButton postImage;
    private TextInputLayout postInput;
    private MaterialButton postButton;


    private Uri imageUri;
    private String  postContent;

    private StorageReference postsImagesReference;
    private DatabaseReference usersRef, postsRef;
    private FirebaseAuth mAuth;

    private String saveCurrentDate, saveCurrentTime, postRandomName, downloadUrl, current_user_id;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);
        mAuth = FirebaseAuth.getInstance();
        current_user_id = mAuth.getCurrentUser().getUid();
        postsImagesReference = FirebaseStorage.getInstance().getReference();
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        postsRef = FirebaseDatabase.getInstance().getReference().child("Posts");

        findViews();
        loadingBar = new ProgressDialog(this);

        tb = findViewById(R.id.update_post_toolbar);
        setSupportActionBar(tb);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Update Post");


        ///image
        postImage.setOnClickListener(v->openSomeActivityForResult());

        //post

        postButton.setOnClickListener(v->validatePostInfo());
    }

    private void validatePostInfo() {
        postContent =String.valueOf(postInput.getEditText().getText());
        if(imageUri ==null){
            displayToast("please select post image");
        }
        if(TextUtils.isEmpty(postContent)){
            displayToast("please write something about your image");
        }
        else{
            loadingBar.setTitle("Add New Post");
            loadingBar.setMessage("Please wait, while we are updating your new post...");
            loadingBar.show();
            loadingBar.setCanceledOnTouchOutside(true);
            storeImageToFirebaseStorage();
        }


    }

    private void storeImageToFirebaseStorage() {
        Calendar calForDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
        saveCurrentDate = currentDate.format(calForDate.getTime());

        Calendar calFordTime = Calendar.getInstance();
        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm");
        saveCurrentTime = currentTime.format(calForDate.getTime());


        postRandomName = saveCurrentDate + saveCurrentTime;
        StorageReference filePath = postsImagesReference.child("Post Images").child(imageUri.getLastPathSegment() + postRandomName + ".jpg");
        UploadTask uploadTask = filePath.putFile(imageUri);

        Task<Uri>uriTask = uploadTask.continueWithTask(task -> {
            if (!task.isSuccessful()) {
                throw task.getException();
            }

            // Continue with the task to get the download URL
            return filePath.getDownloadUrl();
        })
                .addOnCompleteListener(task -> {

                    if (task.isSuccessful()) {


                        downloadUrl = task.getResult().toString();


                        // Adding image upload id s child element into databaseReference.

                        savePostInformationToDatabase();


                    }

                });

    }

    private void savePostInformationToDatabase() {
        usersRef.child(current_user_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    String userName = snapshot.child("name").getValue().toString();
                    String userProfileImage = snapshot.child("image").getValue().toString();
                    HashMap postsMap = new HashMap();
                    postsMap.put("uid", current_user_id);
                    postsMap.put("date", saveCurrentDate);
                    postsMap.put("time", saveCurrentTime);
                    postsMap.put("description", postContent);
                    postsMap.put("postImage", downloadUrl);
                    postsMap.put("profileImage", userProfileImage);
                    postsMap.put("name", userName);

                    postsRef.child(current_user_id + postRandomName).updateChildren(postsMap).addOnCompleteListener(new OnCompleteListener() {
                        @Override
                        public void onComplete(@NonNull Task task) {

                            if(task.isSuccessful())
                            {
                                activityShifter(PostActivity.this, MainActivity.class,false,false);
                                displayToast(" New post is updated successfully");
                                loadingBar.dismiss();

                            }
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    ActivityResultLauncher<Intent> someActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK  ) {
                        imageUri = result.getData().getData();
                        postImage.setImageURI(imageUri);
                    }
                }
            });

    private void activityShifter(Activity from, Class to, boolean addFlags , boolean finish){
        Intent intent = new Intent(from,to);
        if(addFlags)
            // user can't go back if the back button is pressed
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        if(finish)
            finish();

    }
    public void openSomeActivityForResult() {
        Intent galleryIntent = new Intent();
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        someActivityResultLauncher.launch(galleryIntent);
    }
    private void displayToast(String message){
        Toast.makeText(this,message,Toast.LENGTH_SHORT).show();

    }
    private void findViews() {
        postImage = findViewById(R.id.post_image_post);
        postInput = findViewById(R.id.input_EDT_post);
        postButton = findViewById(R.id.post_BTN_post);
    }
}