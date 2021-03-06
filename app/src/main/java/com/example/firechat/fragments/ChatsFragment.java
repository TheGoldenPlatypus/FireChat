package com.example.firechat.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.firechat.activities.ChatActivity;
import com.example.firechat.R;
import com.example.firechat.classes.Contacts;
import com.example.firechat.data.KeysAndValues;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


public class ChatsFragment extends Fragment
{
    private View privateChatsView;
    private RecyclerView chatsList;

    private DatabaseReference chatsRef, usersRef;
    private FirebaseAuth mAuth;
    private String currentUserID="";

    public ChatsFragment()
    {

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        privateChatsView = inflater.inflate(R.layout.fragment_chats, container, false);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        chatsRef = FirebaseDatabase.getInstance().getReference().child(KeysAndValues.CONTACTS).child(currentUserID);
        usersRef = FirebaseDatabase.getInstance().getReference().child(KeysAndValues.USERS);

        chatsList = (RecyclerView) privateChatsView.findViewById(R.id.chats_list);
        chatsList.setLayoutManager(new LinearLayoutManager(getContext()));

        chatsList.setLayoutManager(new LinearLayoutManager(getContext()));
        chatsList.addItemDecoration(new DividerItemDecoration(getContext(),DividerItemDecoration.VERTICAL));

        return privateChatsView;
    }
    @Override
    public void onStart()
    {
        super.onStart();


        FirebaseRecyclerOptions<Contacts> options =
                new FirebaseRecyclerOptions.Builder<Contacts>()
                        .setQuery(chatsRef, Contacts.class)
                        .build();


        FirebaseRecyclerAdapter<Contacts, ChatsViewHolder> adapter =
                new FirebaseRecyclerAdapter<Contacts, ChatsViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull final ChatsViewHolder holder, int position, @NonNull Contacts model)
                    {
                        final String usersIDs = getRef(position).getKey();
                        final String[] retImage = {"default_image"};

                        usersRef.child(usersIDs).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot)
                            {
                                if (dataSnapshot.exists())
                                {
                                    if (dataSnapshot.hasChild(KeysAndValues.IMAGE))
                                    {
                                        retImage[0] = dataSnapshot.child(KeysAndValues.IMAGE).getValue().toString();
                                        Picasso.get().load(retImage[0]).into(holder.profileImage);
                                    }

                                    final String retName = dataSnapshot.child(KeysAndValues.NAME).getValue().toString();
                                    final String retStatus = dataSnapshot.child(KeysAndValues.STATUS).getValue().toString();

                                    holder.userName.setText(retName);


                                    if (dataSnapshot.child(KeysAndValues.USER_STATE).hasChild("state"))
                                    {
                                        String state = dataSnapshot.child(KeysAndValues.USER_STATE).child("state").getValue().toString();
                                        String date = dataSnapshot.child(KeysAndValues.USER_STATE).child("date").getValue().toString();
                                        String time = dataSnapshot.child(KeysAndValues.USER_STATE).child("time").getValue().toString();

                                        if (state.equals(KeysAndValues.USER_STATUS_ONLINE))
                                        {
                                            holder.userStatus.setText(retStatus+"\nonline");
                                        }
                                        else if (state.equals(KeysAndValues.USER_STATUS_OFFLINE))
                                        {
                                            holder.userStatus.setText( retStatus+ "\n\nLast Seen: " + date + " " + time);
                                        }
                                    }
                                    else
                                    {
                                        holder.userStatus.setText("offline");
                                    }


                                    holder.itemView.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view)
                                        {
                                            Intent chatIntent = new Intent(getContext(), ChatActivity.class);
                                            chatIntent.putExtra(KeysAndValues.VISIT_USER_ID, usersIDs);
                                            chatIntent.putExtra(KeysAndValues.VISIT_USER_NAME_KEY, retName);
                                            chatIntent.putExtra(KeysAndValues.VISIT_USER_IMAGE_KEY, retImage[0]);
                                            startActivity(chatIntent);
                                        }
                                    });
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }

                    @NonNull
                    @Override
                    public ChatsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i)
                    {
                        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.users_display_layout, viewGroup, false);
                        return new ChatsViewHolder(view);
                    }
                };

        chatsList.setAdapter(adapter);
        adapter.startListening();
    }




    public static class  ChatsViewHolder extends RecyclerView.ViewHolder
    {
        CircleImageView profileImage;
        TextView userStatus, userName;

        public ChatsViewHolder(@NonNull View itemView)
        {
            super(itemView);

            profileImage = itemView.findViewById(R.id.user_profile_image);
            userStatus = itemView.findViewById(R.id.user_status);
            userName = itemView.findViewById(R.id.user_profile_name);
        }
    }
}