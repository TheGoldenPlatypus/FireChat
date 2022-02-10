package com.example.firechat.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import com.example.firechat.R;
import com.example.firechat.data.KeysAndValues;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private CircleImageView userProfileImage;
    private TextView userProfileName, userProfileStatus;
    private MaterialButton sendMessageButton, declineMessageButton;

    private DatabaseReference userRef, chatRequestRef, contactsRef;
    private FirebaseAuth mAuth;

    private String receiverUserId,currentState, senderUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        initializeFirebaseComponents();
        receiverUserId = getIntent().getExtras().get(KeysAndValues.VISIT_USER_ID).toString();
        senderUserId = mAuth.getCurrentUser().getUid();
        currentState = KeysAndValues.NEW_STATE;
        findViews();
        retrieveUserInfo();

    }


    private void manageChatRequests(){
        chatRequestRef.child(senderUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.hasChild(receiverUserId)){
                    String requestType = snapshot.child(receiverUserId).child(KeysAndValues.REQUEST_TYPE).getValue().toString();
                    if(requestType.equals(KeysAndValues.STATUS_SENT)){
                        currentState = KeysAndValues.REQUEST_SENT_STATE;
                        sendMessageButton.setText(KeysAndValues.CANCEL_CHAT_REQUEST);
                    }
                    else if(requestType.equals(KeysAndValues.STATUS_RECEIVED)){
                        currentState = KeysAndValues.REQUEST_RECEIVED_STATE;
                        sendMessageButton.setText(KeysAndValues.ACCEPT_CHAT_REQUEST);
                        declineMessageButton.setVisibility(View.VISIBLE);
                        declineMessageButton.setEnabled(true);
                        declineMessageButton.setOnClickListener(v -> cancelChatRequest());
                    }
                }
                else{
                    contactsRef.child(senderUserId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(snapshot.hasChild(receiverUserId)){
                                currentState = KeysAndValues.FRIENDS_STATE;
                                sendMessageButton.setText(KeysAndValues.REMOVE_CONTACT);
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
            sendMessageButton.setOnClickListener(v -> {
                sendMessageButton.setEnabled(false);
                responseToState();
            });

        }
        else{
            sendMessageButton.setVisibility(View.INVISIBLE);
        }
    }
    private void setRequestsAttr(boolean isButtonEnabled, String state, String buttonText ){
        sendMessageButton.setEnabled(isButtonEnabled);
        currentState = state;
        sendMessageButton.setText(buttonText);

    }
    private void responseToState(){
        if (currentState.equals(KeysAndValues.NEW_STATE)){
            sendChatRequest();
        }
        if(currentState.equals(KeysAndValues.REQUEST_SENT_STATE)){
            cancelChatRequest();
        }
        if(currentState.equals(KeysAndValues.REQUEST_RECEIVED_STATE)){
            acceptChatRequest();
        }
        if(currentState.equals(KeysAndValues.FRIENDS_STATE)){
            removeSpecificContact();
        }

    }
    private void acceptChatRequest() {
        contactsRef.child(senderUserId).child(receiverUserId).child(KeysAndValues.CONTACTS).setValue(KeysAndValues.STATUS_SAVED).addOnCompleteListener(task -> {

            if(task.isSuccessful()){
                contactsRef.child(receiverUserId).child(senderUserId).child(KeysAndValues.CONTACTS).setValue(KeysAndValues.STATUS_SAVED).addOnCompleteListener(task1 -> {

                    if(task1.isSuccessful()){
                        chatRequestRef.child(senderUserId).child(receiverUserId).removeValue().addOnCompleteListener(task11 -> {
                            if(task11.isSuccessful()) {
                                chatRequestRef.child(receiverUserId).child(senderUserId).removeValue().addOnCompleteListener(task111 -> {
                                    setRequestsAttr(true,KeysAndValues.FRIENDS_STATE,KeysAndValues.REMOVE_CONTACT);
                                    declineMessageButton.setVisibility(View.INVISIBLE);
                                    declineMessageButton.setEnabled(false);

                                });
                            }

                        });
                    }
                });
            }
        });

    }
    private void removeSpecificContact() {
        contactsRef.child(senderUserId).child(receiverUserId)
                .removeValue()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful())
                    {
                        contactsRef.child(receiverUserId).child(senderUserId)
                                .removeValue()
                                .addOnCompleteListener(task1 -> {
                                    if (task1.isSuccessful())
                                    {
                                        setRequestsAttr(true,KeysAndValues.NEW_STATE,KeysAndValues.SEND_MESSAGE);
                                        declineMessageButton.setVisibility(View.INVISIBLE);
                                        declineMessageButton.setEnabled(false);
                                    }
                                });
                    }
                });
    }
    private void retrieveUserInfo() {
        userRef.child(receiverUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String retrievedUsrName = snapshot.child(KeysAndValues.NAME).getValue().toString();
                String retrievedUsrStatus = snapshot.child(KeysAndValues.STATUS).getValue().toString();

                if((snapshot.exists()) && (snapshot.hasChild(KeysAndValues.IMAGE)))
                {
                    String retrievedUsrProfileImg= snapshot.child(KeysAndValues.IMAGE).getValue().toString();
                    Picasso.get().load(retrievedUsrProfileImg).placeholder(R.drawable.img_profile_default).into(userProfileImage);
                }
                userProfileName.setText(retrievedUsrName);
                userProfileStatus.setText(retrievedUsrStatus);
                manageChatRequests();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
    private void cancelChatRequest() {
        chatRequestRef.child(senderUserId).child(receiverUserId)
                .removeValue()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful())
                    {
                        chatRequestRef.child(receiverUserId).child(senderUserId)
                                .removeValue()
                                .addOnCompleteListener(task1 -> {
                                    if (task1.isSuccessful())
                                    {
                                        setRequestsAttr(true,KeysAndValues.NEW_STATE,KeysAndValues.SEND_MESSAGE);
                                        declineMessageButton.setVisibility(View.INVISIBLE);
                                        declineMessageButton.setEnabled(false);
                                    }
                                });
                    }
                });
    }
    private void sendChatRequest() {
        chatRequestRef.child(senderUserId).child(receiverUserId).child(KeysAndValues.REQUEST_TYPE)
                .setValue(KeysAndValues.STATUS_SENT)
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        chatRequestRef.child(receiverUserId).child(senderUserId).child(KeysAndValues.REQUEST_TYPE)
                                .setValue(KeysAndValues.STATUS_RECEIVED)
                                .addOnCompleteListener(task1 -> {
                                    if(task1.isSuccessful()){
                                        setRequestsAttr(true,KeysAndValues.REQUEST_SENT_STATE,KeysAndValues.CANCEL_CHAT_REQUEST);
                                    }
                                });
                    }
                });
    }
    private void initializeFirebaseComponents() {
        mAuth = FirebaseAuth.getInstance();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        chatRequestRef = FirebaseDatabase.getInstance().getReference().child("Chat Requests");
        contactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts");

    }
    private void findViews() {
        userProfileImage = findViewById(R.id.visit_profile_image);
        userProfileName = findViewById(R.id.visit_user_name);
        userProfileStatus = findViewById(R.id.visit_profile_status);
        sendMessageButton = findViewById(R.id.visit_BTN_send_message);
        declineMessageButton = findViewById(R.id.visit_BTN_decline_message);
    }
}