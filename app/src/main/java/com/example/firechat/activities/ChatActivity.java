package com.example.firechat.activities;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.firechat.R;

import com.example.firechat.data.KeysAndValues;
import com.example.firechat.utils.MessageAdapter;
import com.example.firechat.utils.Messages;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private TextView userName, userLastSeen;
    private CircleImageView userImage;
    private Toolbar chatToolBar;
    private ImageButton sendMessageButton, sendFilesButton;
    private TextInputLayout messageInputText;
    private RecyclerView userMessagesList;
    private ProgressDialog loadingBar;


    private FirebaseAuth mAuth;
    private DatabaseReference rootRef;
    private StorageTask uploadTask;
    private Uri fileUri;

    private final List<Messages> messagesList = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private MessageAdapter messageAdapter;

    private String messageReceiverId, messageReceiverName,messageReceiverImage, messageSenderId,saveCurrentTime, saveCurrentDate,downloadUrl;
    private String checker= "text", myUrl="";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        initializeFirebaseComponents();
        unloadIntentData();
        initializeControllers();
        initializeListeners();
        userName.setText(messageReceiverName);
        Picasso.get().load(messageReceiverImage).placeholder(R.drawable.img_profile_default).into(userImage);
        displayLastSeen();

        rootRef.child("Messages").child(messageSenderId).child(messageReceiverId).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Messages messages = snapshot.getValue(Messages.class);
                messagesList.add(messages);
                messageAdapter.notifyDataSetChanged();
                userMessagesList.smoothScrollToPosition(userMessagesList.getAdapter().getItemCount());
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }





    private void initializeControllers() {
        chatToolBar = (Toolbar) findViewById(R.id.chat_toolbar);
        setSupportActionBar(chatToolBar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionBarView = layoutInflater.inflate(R.layout.custom_chat_bar,null);
        actionBar.setCustomView(actionBarView);

        userName =   findViewById(R.id.custom_profile_name);
        userImage =  findViewById(R.id.custom_profile_image);
        userLastSeen = findViewById(R.id.custom_user_last_seen);
        sendMessageButton = findViewById(R.id.img_BTN_chat_send);
        sendFilesButton = findViewById(R.id.img_BTN_chat_send_file);
        messageInputText = findViewById(R.id.input_EDT_chat_message);

        messageAdapter = new MessageAdapter(messagesList);
        userMessagesList = (RecyclerView) findViewById(R.id.chat_recycler_list_view);
        linearLayoutManager = new LinearLayoutManager(this);
        userMessagesList.setLayoutManager(linearLayoutManager);
        userMessagesList.setAdapter(messageAdapter);

        loadingBar = new ProgressDialog(this);

        Calendar calendar = Calendar.getInstance();

        SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd, yyyy");
        saveCurrentDate = currentDate.format(calendar.getTime());

        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
        saveCurrentTime = currentTime.format(calendar.getTime());


    }







    private void sendMessage(){
        String message =String.valueOf(messageInputText.getEditText().getText());
        if(TextUtils.isEmpty(message))
            displayToast("Please write message first");

        else{
            String messageSenderRef = "Messages/"+ messageSenderId + "/" + messageReceiverId;
            String messageReceiverRef = "Messages/"+ messageReceiverId + "/" + messageSenderId;

            DatabaseReference userMessageKeyRef = rootRef.child("Messages").child(messageSenderId).child(messageReceiverId).push();
            String messagePushId = userMessageKeyRef.getKey();

            Map messageTextBody = new HashMap();
            messageTextBody.put("message", message);
            messageTextBody.put("type", checker);
            messageTextBody.put("from", messageSenderId);
            messageTextBody.put("to", messageReceiverId);
            messageTextBody.put("messageID", messagePushId);
            messageTextBody.put("time", saveCurrentTime);
            messageTextBody.put("date", saveCurrentDate);


            Map messageBodyDetails = new HashMap();
            messageBodyDetails.put(messageSenderRef + "/" + messagePushId, messageTextBody);
            messageBodyDetails.put(messageReceiverRef + "/" + messagePushId, messageTextBody);

            rootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    messageInputText.getEditText().setText("");
                }
            });

        }
    }

    private void displayLastSeen()
    {
        rootRef.child("Users").child(messageReceiverId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot)
                    {
                        if (dataSnapshot.child("userState").hasChild("state"))
                        {
                            String state = dataSnapshot.child("userState").child("state").getValue().toString();
                            String date = dataSnapshot.child("userState").child("date").getValue().toString();
                            String time = dataSnapshot.child("userState").child("time").getValue().toString();

                            if (state.equals("online"))
                            {
                                userLastSeen.setText("online");
                            }
                            else if (state.equals("offline"))
                            {
                                userLastSeen.setText("Last Seen: " + date + " " + time);
                            }
                        }
                        else
                        {
                            userLastSeen.setText("offline");
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }
    private void displayToast(String message){
        Toast.makeText(this,message,Toast.LENGTH_SHORT).show();
    }
    private void unloadIntentData(){
        messageReceiverId = getIntent().getExtras().get(KeysAndValues.VISIT_USER_ID).toString();
        messageReceiverName = getIntent().getExtras().get(KeysAndValues.VISIT_USER_NAME_KEY).toString();
        messageReceiverImage = getIntent().getExtras().get(KeysAndValues.VISIT_USER_IMAGE_KEY).toString();

    }
    private void initializeFirebaseComponents() {
        mAuth = FirebaseAuth.getInstance();
        messageSenderId = mAuth.getCurrentUser().getUid();
        rootRef = FirebaseDatabase.getInstance().getReference();

    }
    private void initializeListeners() {
        sendMessageButton.setOnClickListener(v -> sendMessage());
        sendFilesButton.setOnClickListener(v -> filesHandler());

    }

    private void filesHandler() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        someActivityResultLauncher.launch(intent);

    }
    ActivityResultLauncher<Intent> someActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK  ) {
                        loadingBar.setTitle("Sending Image");
                        loadingBar.setMessage("Please wait while we are sending the image");
                        loadingBar.setCanceledOnTouchOutside(false);
                        loadingBar.show();

                        checker = "image";
                        fileUri = result.getData().getData();
                        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Image Files");
                        String messageSenderRef = "Messages/"+ messageSenderId + "/" + messageReceiverId;
                        String messageReceiverRef = "Messages/"+ messageReceiverId + "/" + messageSenderId;

                        DatabaseReference userMessageKeyRef = rootRef.child("Messages").child(messageSenderId).child(messageReceiverId).push();
                        String messagePushId = userMessageKeyRef.getKey();
                        StorageReference filePath = storageReference.child(messagePushId+"."+"jpg");
                        uploadTask =  filePath.putFile(fileUri);
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
                                        downloadUrl = task.getResult().toString();
                                        //     String ImageUploadId = rootReference.push().getKey();

                                        // Adding image upload id s child element into databaseReference.



                                        Map messageTextBody = new HashMap();
                                        messageTextBody.put("message", downloadUrl);
                                        messageTextBody.put("name", fileUri.getLastPathSegment());
                                        messageTextBody.put("type", checker);
                                        messageTextBody.put("from", messageSenderId);
                                        messageTextBody.put("to", messageReceiverId);
                                        messageTextBody.put("messageID", messagePushId);
                                        messageTextBody.put("time", saveCurrentTime);
                                        messageTextBody.put("date", saveCurrentDate);


                                        Map messageBodyDetails = new HashMap();
                                        messageBodyDetails.put(messageSenderRef + "/" + messagePushId, messageTextBody);
                                        messageBodyDetails.put(messageReceiverRef + "/" + messagePushId, messageTextBody);

                                        rootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                                            @Override
                                            public void onComplete(@NonNull Task task) {
                                                loadingBar.dismiss();
                                                messageInputText.getEditText().setText("");
                                                checker = "text";

                                            }
                                        });

                                    }

                                });

                    }
                }
            });

    private void findViews() {


    }



}