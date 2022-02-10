package com.example.firechat.libs;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.example.firechat.fragments.ChatsFragment;
import com.example.firechat.fragments.FeedFragment;
import com.example.firechat.fragments.GroupsFragment;
import com.example.firechat.fragments.RequestsFragment;

public class TabsAccessorAdapter extends FragmentPagerAdapter {

    public TabsAccessorAdapter(@NonNull FragmentManager fm) {
        super(fm);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position){

            case 0:
                ChatsFragment chatsFragment = new ChatsFragment();
                return chatsFragment;
            case 1:
                GroupsFragment groupsFragment = new GroupsFragment();
                return groupsFragment;
            case 2:
                RequestsFragment requestsFragment = new RequestsFragment();
                return requestsFragment;

            case 3:
                FeedFragment feedFragment = new FeedFragment();
                return feedFragment;

            default:
                return null;
        }

    }

    @Override
    public int getCount() {
        return 4;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        switch (position){

            case 0:
                return "Chats";

            case 1:
                return "Groups";
            case 2:
                return "Requests";
            case 3:
                return "Feed";
            default:
                return null;
        }
    }
}
