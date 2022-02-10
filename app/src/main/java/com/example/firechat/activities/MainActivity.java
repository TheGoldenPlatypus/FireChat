package com.example.firechat.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.widget.Toolbar;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.example.firechat.R;
import com.example.firechat.data.KeysAndValues;
import com.example.firechat.libs.TabsAccessorAdapter;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private ViewPager vp;
    private TabLayout tabLayout;
    private TabsAccessorAdapter myTabsAccessorAdapter;

    private FirebaseAuth mAuth;
    private DatabaseReference rootReference;
    private FirebaseUser currentUser;

    private String currentUserId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeFirebaseComponents();
        findViews();
        setViewsObjectsAttr();

    }

    private void updateUserStatus(String state){

        String saveCurrentTime, saveCurrentDate;

        Calendar calendar = Calendar.getInstance();

        SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd, yyyy");
        saveCurrentDate = currentDate.format(calendar.getTime());

        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
        saveCurrentTime = currentTime.format(calendar.getTime());

        HashMap<String, Object> onlineStateMap = new HashMap<>();
        onlineStateMap.put(KeysAndValues.TIME, saveCurrentTime);
        onlineStateMap.put(KeysAndValues.DATE, saveCurrentDate);
        onlineStateMap.put(KeysAndValues.STATE, state);

        currentUserId = mAuth.getCurrentUser().getUid();
        rootReference.child(KeysAndValues.USERS).child(currentUserId).child(KeysAndValues.USER_STATE)
                .updateChildren(onlineStateMap);

    }
    private void requestNewGroup() {

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this,R.style.AlertDialog);
        builder.setTitle(KeysAndValues.TITLE_ENTER_GROUP_NAME);

        final EditText groupNameField = new EditText(MainActivity.this);
        groupNameField.setHint("e.g Afeka Android Class Winter 2021");
        builder.setView(groupNameField);

        builder.setPositiveButton("Create", (dialog, which) -> {
            String groupName = groupNameField.getText().toString();
            if(TextUtils.isEmpty(groupName)){
                displayToast("Please insert group name");
            }
            else{
                createNewGroup(groupName);
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }
    private void createNewGroup(String groupName) {
        rootReference.child(KeysAndValues.GROUPS).child(groupName).setValue("").addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                displayToast(groupName + " is created successfully");
            }
        });
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        if(item.getItemId() == R.id.main_logout_option)
        {
            updateUserStatus(KeysAndValues.USER_STATUS_OFFLINE);
            mAuth.signOut();
            activityShifter(MainActivity.this, LoginActivity.class,true,true);
        }
        if(item.getItemId() == R.id.main_group_option)
            requestNewGroup();
        if(item.getItemId() == R.id.main_settings_option)
            activityShifter(MainActivity.this, SettingsActivity.class,false,false);
        if(item.getItemId() == R.id.main_find_friends_option)
            activityShifter(MainActivity.this, FindFriendsActivity.class,false,false);

        return true ;
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
    private void displayToast(String message){
        Toast.makeText(this,message,Toast.LENGTH_SHORT).show();

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //access options_menu.xml
        getMenuInflater().inflate(R.menu.options_menu,menu);
        return super.onCreateOptionsMenu(menu);

    }
    private void VerifyUserExistence() {
        String currentUserId = mAuth.getCurrentUser().getUid();
        rootReference.child(KeysAndValues.USERS).child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                if(!(snapshot.child(KeysAndValues.NAME).exists())){
                    activityShifter(MainActivity.this, SettingsActivity.class,false,false);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void initializeFirebaseComponents() {

        mAuth = FirebaseAuth.getInstance();
        rootReference = FirebaseDatabase.getInstance().getReference();

    }
    @Override
    protected void onStart() {
        super.onStart();
        currentUser = mAuth.getCurrentUser();
        // user is not authenticated
        if(currentUser ==null)
            // send user first to the login activity
            activityShifter(MainActivity.this, LoginActivity.class,true,true);
        else
            updateUserStatus(KeysAndValues.USER_STATUS_ONLINE);
        VerifyUserExistence();
    }
    @Override
    protected void onStop() {
        super.onStop();
        currentUser = mAuth.getCurrentUser();

        if(currentUser != null){
            updateUserStatus(KeysAndValues.USER_STATUS_OFFLINE);

        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        currentUser = mAuth.getCurrentUser();

        if(currentUser != null){
            updateUserStatus("offline");

        }
    }
    private void findViews(){
        toolbar  = (Toolbar) findViewById(R.id.main_page_toolbar);
        vp = (ViewPager) findViewById(R.id.main_tabs_pager);
        tabLayout = (TabLayout) findViewById(R.id.main_tabs);


    }
    private void setViewsObjectsAttr(){
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(KeysAndValues.FIRECHAT);
        myTabsAccessorAdapter = new TabsAccessorAdapter(getSupportFragmentManager());
        vp.setAdapter(myTabsAccessorAdapter);
        tabLayout.setupWithViewPager(vp);
    }
}