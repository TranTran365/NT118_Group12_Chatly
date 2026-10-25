package com.group12.chatly.ui.profile;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.group12.chatly.R;
import com.group12.chatly.data.model.User;
import com.group12.chatly.ui.welcome.WelcomeActivity;
import com.group12.chatly.viewmodel.ProfileViewModel;

import java.util.Locale;

public class ProfileFragment extends Fragment {

    private static final String PREFS = "chatly_prefs";
    private static final String KEY_NOTIFICATIONS = "notifications_enabled";

    private TextView tvInitials, tvDisplayName, tvBio;
    private ImageView imgAvatar;
    private SwitchCompat swNotifications, swDarkMode;

    private ProfileViewModel profileViewModel;

    public ProfileFragment() {
        super(R.layout.fragment_profile);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        profileViewModel = new ViewModelProvider(this).get(ProfileViewModel.class);

        tvInitials = view.findViewById(R.id.tv_initials);
        tvDisplayName = view.findViewById(R.id.tv_display_name);
        tvBio = view.findViewById(R.id.tv_bio);
        imgAvatar = view.findViewById(R.id.img_avatar);
        swNotifications = view.findViewById(R.id.sw_notifications);
        swDarkMode = view.findViewById(R.id.sw_dark_mode);

        setupInsets(view);
        setupUserObserver();
        setupPreferences(view);
        setupRows(view);

        profileViewModel.loadUserProfile();
    }

    private void setupInsets(View root) {
        View header = root.findViewById(R.id.header_profile);
        final int basePaddingTop = header.getPaddingTop();
        ViewCompat.setOnApplyWindowInsetsListener(header, (v, insets) -> {
            Insets bars = insets.getInsets(
                    WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout());
            v.setPadding(v.getPaddingLeft(), basePaddingTop + bars.top,
                    v.getPaddingRight(), v.getPaddingBottom());
            return insets;
        });
        ViewCompat.requestApplyInsets(header);
    }

    private void setupUserObserver() {
        profileViewModel.getUserProfileState().observe(getViewLifecycleOwner(), state -> {
            if (state == null) return;

            if (state.isSuccess() && state.getData() != null) {
                User user = state.getData();
                String name = (user.getDisplayName() != null && !user.getDisplayName().trim().isEmpty())
                        ? user.getDisplayName()
                        : (user.getEmail() != null ? user.getEmail() : "Chatly User");
                String bio = (user.getBio() != null && !user.getBio().trim().isEmpty())
                        ? user.getBio()
                        : "Here for good conversations.";

                tvDisplayName.setText(name);
                tvBio.setText(bio);
                tvInitials.setText(initialsOf(name));

                if (user.getAvatarUrl() != null && !user.getAvatarUrl().trim().isEmpty() && getContext() != null) {
                    Glide.with(this)
                            .load(user.getAvatarUrl())
                            .circleCrop()
                            .into(imgAvatar);
                    tvInitials.setVisibility(View.GONE);
                } else {
                    tvInitials.setVisibility(View.VISIBLE);
                }
            } else if (state.isError()) {
                // Fallback display if not logged in or offline
                tvDisplayName.setText(R.string.app_name);
                tvBio.setText(state.getErrorMessage());
                tvInitials.setText("?");
            }
        });
    }

    private void setupPreferences(View root) {
        SharedPreferences prefs = requireContext()
                .getSharedPreferences(PREFS, Context.MODE_PRIVATE);

        // Đặt trạng thái trước rồi mới gắn listener để khỏi bắn sự kiện giả
        swNotifications.setChecked(prefs.getBoolean(KEY_NOTIFICATIONS, true));
        swNotifications.setOnCheckedChangeListener((button, checked) ->
                prefs.edit().putBoolean(KEY_NOTIFICATIONS, checked).apply());

        // Dark mode: UI placeholder
        root.findViewById(R.id.row_notifications).setOnClickListener(v -> swNotifications.toggle());
        root.findViewById(R.id.row_dark_mode).setOnClickListener(v -> swDarkMode.toggle());
    }

    private void setupRows(View root) {
        root.findViewById(R.id.btn_change_photo).setOnClickListener(v -> {
            // TODO: mở image picker, upload ảnh đại diện
        });
        root.findViewById(R.id.row_edit_profile).setOnClickListener(v -> {
            // TODO: mở EditProfileActivity khi có màn này
        });
        root.findViewById(R.id.row_email).setOnClickListener(v -> {
            // TODO: đổi email
        });
        root.findViewById(R.id.row_phone).setOnClickListener(v -> {
            // TODO: đổi số điện thoại
        });
        root.findViewById(R.id.row_privacy).setOnClickListener(v -> {
            // TODO: mở màn Privacy & security
        });
        root.findViewById(R.id.row_logout).setOnClickListener(v -> logout());
    }

    private void logout() {
        profileViewModel.logout();

        // Xóa hết back stack để bấm Back không quay lại màn đã đăng nhập
        Intent intent = new Intent(requireContext(), WelcomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    /** "Alex Morgan" -> "AM", "Alex" -> "A", rỗng -> "?" */
    private static String initialsOf(@Nullable String name) {
        if (name == null) return "?";
        String[] parts = name.trim().split("\\s+");
        if (parts[0].isEmpty()) return "?";
        String first = parts[0].substring(0, 1);
        String last = parts.length > 1 ? parts[parts.length - 1].substring(0, 1) : "";
        return (first + last).toUpperCase(Locale.getDefault());
    }
}
