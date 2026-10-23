package com.group12.chatly;

import android.graphics.Color;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.activity.SystemBarStyle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.group12.chatly.ui.chats.ChatsFragment;
import com.group12.chatly.ui.contacts.ContactsFragment;
import com.group12.chatly.ui.profile.ProfileFragment;

public class MainActivity extends AppCompatActivity {

    private static final String TAG_CHATS = "tab_chats";
    private static final String TAG_CONTACTS = "tab_contacts";
    private static final String TAG_PROFILE = "tab_profile";

    private Fragment chats, contacts, profile, active;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Thanh trạng thái: icon trắng (header xanh). Thanh điều hướng: icon tối (nền trắng).
        EdgeToEdge.enable(this,
                SystemBarStyle.dark(Color.TRANSPARENT),
                SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT));
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        final int basePaddingBottom = bottomNav.getPaddingBottom();
        ViewCompat.setOnApplyWindowInsetsListener(bottomNav, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(v.getPaddingLeft(), v.getPaddingTop(),
                    v.getPaddingRight(), basePaddingBottom + bars.bottom);
            return insets;
        });

        FragmentManager fm = getSupportFragmentManager();
        if (savedInstanceState == null) {
            chats = new ChatsFragment();
            contacts = new ContactsFragment();
            profile = new ProfileFragment();
            fm.beginTransaction()
                    .add(R.id.fragment_container, profile, TAG_PROFILE).hide(profile)
                    .add(R.id.fragment_container, contacts, TAG_CONTACTS).hide(contacts)
                    .add(R.id.fragment_container, chats, TAG_CHATS)
                    .commit();
            active = chats;
        } else {
            chats = fm.findFragmentByTag(TAG_CHATS);
            contacts = fm.findFragmentByTag(TAG_CONTACTS);
            profile = fm.findFragmentByTag(TAG_PROFILE);
            active = !chats.isHidden() ? chats : !contacts.isHidden() ? contacts : profile;
        }

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_chats) show(chats);
            else if (id == R.id.nav_contacts) show(contacts);
            else if (id == R.id.nav_profile) show(profile);
            else return false;
            return true;
        });
    }

    private void show(Fragment target) {
        if (target == active) return;
        getSupportFragmentManager().beginTransaction()
                .hide(active).show(target).commit();
        active = target;
    }
}