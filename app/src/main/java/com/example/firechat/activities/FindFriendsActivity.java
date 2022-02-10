package com.example.firechat.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.firechat.R;
import com.example.firechat.data.KeysAndValues;
import com.example.firechat.utils.Contacts;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class FindFriendsActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private RecyclerView findFriendsRecyclerList;
    private DatabaseReference usersRef;
    private  String visitUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friends);

        initializeFirebaseComponents();
        findViews();
        initializeViewConfigurations();
    }


    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Contacts> options = new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(usersRef, Contacts.class)
                .build();

        FirebaseRecyclerAdapter<Contacts,FindFriendsViewHolder> adapter =
                new FirebaseRecyclerAdapter<Contacts, FindFriendsViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull FindFriendsViewHolder holder, @SuppressLint("RecyclerView") int position, @NonNull Contacts model) {
                        holder.userName.setText(model.getName());
                        holder.userStatus.setText(model.getStatus());
                        Picasso.get().load(model.getImage()).placeholder(R.drawable.img_profile_default).into(holder.profileImage);


                        holder.itemView.setOnClickListener(v -> {
                            visitUserId = getRef(position).getKey();
                            passUserToProfileActivity(visitUserId);

                        });
                    }

                    @NonNull
                    @Override
                    public FindFriendsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_display_layout,parent,false);
                        FindFriendsViewHolder viewHolder = new FindFriendsViewHolder(view);
                        return viewHolder;
                    }
                };
       findFriendsRecyclerList.setAdapter(adapter);
       adapter.startListening();
    }
    public static class FindFriendsViewHolder extends RecyclerView.ViewHolder{
        TextView userName,userStatus;
        CircleImageView profileImage;

        public FindFriendsViewHolder(@NonNull View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.user_profile_name);
            userStatus = itemView.findViewById(R.id.user_status);
            profileImage = itemView.findViewById(R.id.user_profile_image);
        }
    }
    private void passUserToProfileActivity(String visitUserId){
        Intent profileIntent = new Intent(FindFriendsActivity.this,ProfileActivity.class);
        profileIntent.putExtra(KeysAndValues.VISIT_USER_ID,visitUserId);
        startActivity(profileIntent);
    }
    private void initializeViewConfigurations() {
        findFriendsRecyclerList.setLayoutManager(new LinearLayoutManager(this));
        findFriendsRecyclerList.setLayoutManager(new LinearLayoutManager(this));
        findFriendsRecyclerList.addItemDecoration(new DividerItemDecoration(this,DividerItemDecoration.VERTICAL));

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(KeysAndValues.TITLE_FIND_FRIENDS);
    }
    private void initializeFirebaseComponents() {
        usersRef = FirebaseDatabase.getInstance().getReference().child(KeysAndValues.USERS);
    }
    private void findViews() {
        findFriendsRecyclerList = findViewById(R.id.find_friends_recycler_list);
        toolbar = findViewById(R.id.find_friends_toolbar);

    }
}