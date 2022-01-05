package com.example.firechat.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toolbar;

import com.example.firechat.R;
import com.example.firechat.libs.TabsAccessorAdapter;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private ViewPager vp;
    private TabLayout tabLayout;
    private TabsAccessorAdapter myTabsAccessorAdapter;

    private FirebaseUser currentUser;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
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

    }
    private void passUserToLoginActivity() {
        Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(loginIntent);

    }
    private void findViews(){
        toolbar  = findViewById(R.id.main_page_toolbar);
        vp = (ViewPager) findViewById(R.id.main_tabs_pager);
        tabLayout = (TabLayout) findViewById(R.id.main_tabs);


    }
    private void setViewsObjectsAttr(){
        setActionBar(toolbar);
        getActionBar().setTitle("FireChat");
        myTabsAccessorAdapter = new TabsAccessorAdapter(getSupportFragmentManager());
        vp.setAdapter(myTabsAccessorAdapter);
        tabLayout.setupWithViewPager(vp);
    }
}