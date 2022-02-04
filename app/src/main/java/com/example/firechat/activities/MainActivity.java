package com.example.firechat.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.widget.Toolbar;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.example.firechat.R;
import com.example.firechat.libs.TabsAccessorAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private ViewPager vp;
    private TabLayout tabLayout;
    private TabsAccessorAdapter myTabsAccessorAdapter;

    private FirebaseUser currentUser;
    private FirebaseAuth mAuth;
    private DatabaseReference rootReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        rootReference = FirebaseDatabase.getInstance().getReference();
        findViews();
        setViewsObjectsAttr();

    }

    @Override
    protected void onStart() {
        super.onStart();

        // user is not authenticated
        if(currentUser ==null)
            // send user first to the login activity
            passUserToLoginActivity();
        else
            VerifyUserExistence();
    }

    private void VerifyUserExistence() {
        String currentUserId = mAuth.getCurrentUser().getUid();
        rootReference.child("Users").child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                if((snapshot.child("name").exists())){
                    displayToast("Welcome");
                }
                else
                {
                    passUserToSettingsActivity();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //access options_menu.xml
        getMenuInflater().inflate(R.menu.options_menu,menu);
        return super.onCreateOptionsMenu(menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        if(item.getItemId() == R.id.main_logout_option)
        {
            mAuth.signOut();
            passUserToLoginActivity();
        }
        if(item.getItemId() == R.id.main_group_option)
            requestNewGroup();

        if(item.getItemId() == R.id.main_settings_option)
            passUserToSettingsActivity();
        if(item.getItemId() == R.id.main_find_friends_option)
            passUserToFindFriendsActivity();

        return true ;
    }

    private void passUserToFindFriendsActivity() {
        Intent friendsIntent = new Intent(MainActivity.this, FindFriendsActivity.class);
        startActivity(friendsIntent);
    }

    private void requestNewGroup() {

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this,R.style.AlertDialog);
        builder.setTitle("Enter Group Name: ");

        final EditText groupNameField = new EditText(MainActivity.this);
        groupNameField.setHint("e.g Afeka Android Class Winter 2021");
        builder.setView(groupNameField);

        builder.setPositiveButton("Create", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String groupName = groupNameField.getText().toString();
                if(TextUtils.isEmpty(groupName)){
                    displayToast("Please insert group name");
                }
                else{
                        createNewGroup(groupName);
                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
               dialog.cancel();
            }
        });

        builder.show();
    }

    private void createNewGroup(String groupName) {
        rootReference.child("Groups").child(groupName).setValue("").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    displayToast(groupName + " is created successfully");
                }
            }
        });
    }

    private void displayToast(String message){
        Toast.makeText(this,message,Toast.LENGTH_SHORT).show();

    }
    private void passUserToLoginActivity() {
        Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
        // user can't go back if the back button is pressed
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();

    }

    private void passUserToSettingsActivity() {
        Intent settingsIntent = new Intent(MainActivity.this, SettingsActivity.class);
        startActivity(settingsIntent);

    }
    private void findViews(){
        toolbar  = (Toolbar) findViewById(R.id.main_page_toolbar);
        vp = (ViewPager) findViewById(R.id.main_tabs_pager);
        tabLayout = (TabLayout) findViewById(R.id.main_tabs);


    }
    private void setViewsObjectsAttr(){
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("FireChat");
        myTabsAccessorAdapter = new TabsAccessorAdapter(getSupportFragmentManager());
        vp.setAdapter(myTabsAccessorAdapter);
        tabLayout.setupWithViewPager(vp);
    }
}