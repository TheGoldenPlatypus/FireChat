package com.example.firechat.libs;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.example.firechat.fragments.ChatsFragment;
import com.example.firechat.fragments.ContactsFragment;
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
                ContactsFragment contactsFragment = new ContactsFragment();
                return contactsFragment;
            case 3:
                RequestsFragment requestsFragment = new RequestsFragment();
                return requestsFragment;

            case 4:
                FeedFragment feedFragment = new FeedFragment();
                return feedFragment;

            default:
                return null;
        }

    }

    @Override
    public int getCount() {
        return 5;
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
                return "Contacts";
            case 3:
                return "Requests";
            case 4:
                return "Feed";
            default:
                return null;
        }
    }
}
