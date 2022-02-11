package com.example.firechat.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.firechat.R;
import com.example.firechat.activities.PostActivity;
import com.example.firechat.utils.Contacts;
import com.example.firechat.utils.Posts;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.core.Context;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


public class FeedFragment extends Fragment {

    private View feedFragmentView;
    private MaterialButton postToFeed;
    private RecyclerView feedList;

    private DatabaseReference postsRef;

    public FeedFragment() {
        // Required empty public constructor
    }


    private void initialListeners(){
        postToFeed.setOnClickListener(v->passUserToPostActivity());
    }

    private void passUserToPostActivity() {
        Intent addNewPostIntent = new Intent(getContext(), PostActivity.class);
        startActivity(addNewPostIntent);

    }

    private void findViews() {
        postToFeed = feedFragmentView.findViewById(R.id.feed_BTN_post);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        feedFragmentView= inflater.inflate(R.layout.fragment_feed, container, false);
        postsRef = FirebaseDatabase.getInstance().getReference().child("Posts");
        findViews();
        feedList = feedFragmentView.findViewById(R.id.feed_posts_list);
        feedList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        feedList.setLayoutManager(linearLayoutManager);
        initialListeners();
        displayAllUsersPost();
        return feedFragmentView;
    }

    private void displayAllUsersPost() {

        FirebaseRecyclerOptions<Posts> options =
                new FirebaseRecyclerOptions.Builder<Posts>()
                        .setQuery(postsRef,Posts.class)
                        .build();



        FirebaseRecyclerAdapter<Posts, PostsViewHolder> firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<Posts, PostsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull PostsViewHolder holder, int position, @NonNull Posts model) {

                final String postIDs = getRef(position).getKey();
                final String[] retImage = {"default_image"};
                final String[] retImage1 = {"default_image"};


                postsRef.child(postIDs).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists())
                        {
                            if(snapshot.hasChild("profileImage")){
                                retImage[0]= snapshot.child("profileImage").getValue().toString();
                                Picasso.get().load(retImage[0]).into(holder.profileImage);

                            }
                            if(snapshot.hasChild("postImage")){
                                retImage1[0]= snapshot.child("postImage").getValue().toString();
                                Picasso.get().load(retImage1[0]).into(holder.postedImage);

                            }
                            final String retName = snapshot.child("name").getValue().toString();
                            final String retTime = snapshot.child("time").getValue().toString();
                            final String retDate = snapshot.child("date").getValue().toString();
                            final String retDescription = snapshot.child("description").getValue().toString();

                            holder.userName.setText(retName);
                            holder.postTime.setText(retTime);
                            holder.postDate.setText(" "+retDate);
                            holder.postDescription.setText(retDescription);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

            }

            @NonNull
            @Override
            public PostsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.all_posts_layout, parent, false);
                return new PostsViewHolder(view);
            }

        };

        feedList.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();


    }
    private void displayToast(String message){
        Toast.makeText(getContext(),message,Toast.LENGTH_SHORT).show();

    }
    public static class PostsViewHolder extends RecyclerView.ViewHolder
    {
        CircleImageView profileImage;
        TextView userName,postDate,postTime,postDescription;
        ImageView postedImage;

        public PostsViewHolder(View itemView)
        {
            super(itemView);
            profileImage= itemView.findViewById(R.id.post_profile_image);
            userName= itemView.findViewById(R.id.post_user_name);
            postDate= itemView.findViewById(R.id.post_date);
            postTime= itemView.findViewById(R.id.post_time);
            postDescription= itemView.findViewById(R.id.post_description);
            postedImage = itemView.findViewById(R.id.post_image);
        }


    }
}