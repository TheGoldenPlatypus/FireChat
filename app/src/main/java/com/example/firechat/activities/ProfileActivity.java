package com.example.firechat.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.example.firechat.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private String receiverUserId,currentState, senderUserId;

    private CircleImageView userProfileImage;
    private TextView userProfileName, userProfileStatus;
    private MaterialButton sendMessageButton, declineMessageButton;

    private DatabaseReference userRef, chatRequestRef, contactsRef;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        chatRequestRef = FirebaseDatabase.getInstance().getReference().child("Chat Requests");
        contactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts");

        receiverUserId = getIntent().getExtras().get("visit_user_id").toString();
        senderUserId = mAuth.getCurrentUser().getUid();

        currentState = "new";

        findViews();
        retrieveUserInfo();

    }

    private void retrieveUserInfo() {
        userRef.child(receiverUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if((snapshot.exists()) && (snapshot.hasChild("image")))
                {
                    String retrievedUsrProfileImg= snapshot.child("image").getValue().toString();
                    String retrievedUsrName = snapshot.child("name").getValue().toString();
                    String retrievedUsrStatus = snapshot.child("status").getValue().toString();

                    Picasso.get().load(retrievedUsrProfileImg).placeholder(R.drawable.img_profile_default).into(userProfileImage);
                    userProfileName.setText(retrievedUsrName);
                    userProfileStatus.setText(retrievedUsrStatus);

                    manageChatRequests();
                }
                else{

                    String retrievedUsrName = snapshot.child("name").getValue().toString();
                    String retrievedUsrStatus = snapshot.child("status").getValue().toString();

                    userProfileName.setText(retrievedUsrName);
                    userProfileStatus.setText(retrievedUsrStatus);
                    manageChatRequests();

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }


    private void manageChatRequests(){
        chatRequestRef.child(senderUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.hasChild(receiverUserId)){
                    String requestType = snapshot.child(receiverUserId).child("request_type").getValue().toString();
                    if(requestType.equals("sent")){
                        currentState = "request_sent";
                        sendMessageButton.setText("Cancel Chat Request");
                    }
                    else if(requestType.equals("received")){
                        currentState = "request_received";
                        sendMessageButton.setText("Accept Chat Request");
                        declineMessageButton.setVisibility(View.VISIBLE);
                        declineMessageButton.setEnabled(true);
                        declineMessageButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                cancelChatRequest();
                            }
                        });
                    }
                }
                else{
                    contactsRef.child(senderUserId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(snapshot.hasChild(receiverUserId)){
                                currentState = "friends";
                                sendMessageButton.setText("Remove This Contact");
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        if(!senderUserId.equals(receiverUserId)){
            sendMessageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sendMessageButton.setEnabled(false);
                    if (currentState.equals("new")){
                        sendChatRequest();
                    }
                    if(currentState.equals("request_sent")){
                        cancelChatRequest();
                    }
                    if(currentState.equals("request_received")){
                        acceptChatRequest();
                    }
                    if(currentState.equals("friends")){
                        removeSpecificContact();
                    }
                }
            });

        }
        else{
            sendMessageButton.setVisibility(View.INVISIBLE);
        }
    }

    private void removeSpecificContact() {
        contactsRef.child(senderUserId).child(receiverUserId)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>()
                {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if (task.isSuccessful())
                        {
                            contactsRef.child(receiverUserId).child(senderUserId)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {
                                            if (task.isSuccessful())
                                            {
                                                sendMessageButton.setEnabled(true);
                                                currentState = "new";
                                                sendMessageButton.setText("Send Message");

                                                declineMessageButton.setVisibility(View.INVISIBLE);
                                                declineMessageButton.setEnabled(false);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void acceptChatRequest() {
        contactsRef.child(senderUserId).child(receiverUserId).child("Contacts").setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if(task.isSuccessful()){
                    contactsRef.child(receiverUserId).child(senderUserId).child("Contacts").setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if(task.isSuccessful()){
                                chatRequestRef.child(senderUserId).child(receiverUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()) {

                                            chatRequestRef.child(receiverUserId).child(senderUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                   sendMessageButton.setEnabled(true);
                                                   currentState = "friends";
                                                   sendMessageButton.setText("Remove this Contact");
                                                   declineMessageButton.setVisibility(View.INVISIBLE);
                                                   declineMessageButton.setEnabled(false);

                                                }
                                            });
                                        }

                                        }
                                });
                            }
                        }
                    });
                }
            }
        });

    }

    private void cancelChatRequest() {
        chatRequestRef.child(senderUserId).child(receiverUserId)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>()
                {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if (task.isSuccessful())
                        {
                            chatRequestRef.child(receiverUserId).child(senderUserId)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {
                                            if (task.isSuccessful())
                                            {
                                                sendMessageButton.setEnabled(true);
                                                currentState = "new";
                                                sendMessageButton.setText("Send Message");

                                                declineMessageButton.setVisibility(View.INVISIBLE);
                                                declineMessageButton.setEnabled(false);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void sendChatRequest() {
        chatRequestRef.child(senderUserId).child(receiverUserId).child("request_type").setValue("sent").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){



                    chatRequestRef.child(receiverUserId).child(senderUserId).child("request_type").setValue("received").
                            addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                sendMessageButton.setEnabled(true);
                                currentState = "request_Sent";
                                sendMessageButton.setText("Cancel Chat Request");
                            }
                        }
                    });
                }
            }
        });
    }


    private void findViews() {
        userProfileImage = findViewById(R.id.visit_profile_image);
        userProfileName = findViewById(R.id.visit_user_name);
        userProfileStatus = findViewById(R.id.visit_profile_status);
        sendMessageButton = findViewById(R.id.visit_BTN_send_message);
        declineMessageButton = findViewById(R.id.visit_BTN_decline_message);
    }
}