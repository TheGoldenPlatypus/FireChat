package com.example.firechat.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.firechat.R;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;

public class GroupChatActivity extends AppCompatActivity {
    private Toolbar tb;
    private ImageButton sendButton;
    private TextInputLayout userMessageInput;
    private ScrollView sv;
    private TextView displayTextMessages;

    private FirebaseAuth mAuth;
    private DatabaseReference usersRef, groupNameRef, groupMessageKeyRef;
    private String currentGroupName,currentUserID,currentUserName,currentDate,currentTime;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);

        currentGroupName = getIntent().getExtras().get("groupName").toString();
        displayToast(currentGroupName);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        groupNameRef = FirebaseDatabase.getInstance().getReference().child("Groups").child(currentGroupName);


        findViews();
        getUserInfo();
        initialViewsListeners();

    }

    @Override
    protected void onStart() {
        super.onStart();

        groupNameRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if(snapshot.exists()){
                    displayMessages(snapshot);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if(snapshot.exists()){
                    displayMessages(snapshot);
                }
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

    private void displayMessages(DataSnapshot snapshot) {
        Iterator it = snapshot.getChildren().iterator();
        while(it.hasNext()){
            String chatDate = (String) ((DataSnapshot)it.next()).getValue();
            String chatMessage = (String) ((DataSnapshot)it.next()).getValue();
            String chatName = (String) ((DataSnapshot)it.next()).getValue();
            String chatTime = (String) ((DataSnapshot)it.next()).getValue();

            displayTextMessages.append(chatName + " :\n" + chatMessage + "\n" + chatTime + "    "  + chatDate + "\n\n\n" );

            // scroll automatically to the last message sent
            sv.fullScroll(ScrollView.FOCUS_DOWN);


        }
    }

    private void getUserInfo() {
    usersRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            if(snapshot.exists()){
                currentUserName = snapshot.child("name").getValue().toString();
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {

        }
    });
    }

    private void initialViewsListeners() {
        sendButton.setOnClickListener(v ->saveMessageInfoToDB());

    }

    private void saveMessageInfoToDB() {
        String message =String.valueOf(userMessageInput.getEditText().getText());
        String messageKey = groupNameRef.push().getKey();
        if(TextUtils.isEmpty(message))
            displayToast("Please write message first");
        else{
            Calendar calForDate = Calendar.getInstance();
            SimpleDateFormat currentDateFormat = new SimpleDateFormat("dd MMM, yyyy");
            currentDate = currentDateFormat.format(calForDate.getTime());

            Calendar calForTime = Calendar.getInstance();
            SimpleDateFormat currentTimeFormat = new SimpleDateFormat("hh:mm a");
            currentTime = currentTimeFormat.format(calForTime.getTime());

            HashMap<String, Object> groupMessageKey = new HashMap<>();
            groupNameRef.updateChildren(groupMessageKey);

            groupMessageKeyRef = groupNameRef.child(messageKey);

            HashMap<String, Object> messageInfoMap = new HashMap<>();
            messageInfoMap.put("name",currentUserName);
            messageInfoMap.put("message",message);
            messageInfoMap.put("date",currentDate);
            messageInfoMap.put("time",currentTime);
            groupMessageKeyRef.updateChildren(messageInfoMap);



        }

        userMessageInput.getEditText().setText("");
        sv.fullScroll(ScrollView.FOCUS_DOWN);




    }

    private void displayToast(String message){
        Toast.makeText(this,message,Toast.LENGTH_SHORT).show();

    }
    private void findViews() {
        tb = (Toolbar) findViewById(R.id.group_chat_bar_layout);
        setSupportActionBar(tb);
        getSupportActionBar().setTitle(currentGroupName);
        sendButton = findViewById(R.id.img_BTN_send);
        userMessageInput= findViewById(R.id.input_EDT_group_message);
        sv = findViewById(R.id.group_chat_scroll_view);
        displayTextMessages = findViewById(R.id.group_chat_TXT_display);;
    }
}