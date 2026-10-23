package com.group12.chatly.ui.profile;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

import com.group12.chatly.R;
import com.group12.chatly.ui.welcome.WelcomeActivity;

import java.util.Locale;

public class ProfileFragment extends Fragment {

    private static final String PREFS = "chatly_prefs";
    private static final String KEY_NOTIFICATIONS = "notifications_enabled";

    private TextView tvInitials, tvDisplayName, tvBio;
    private SwitchCompat swNotifications, swDarkMode;

    public ProfileFragment() {
        super(R.layout.fragment_profile);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvInitials = view.findViewById(R.id.tv_initials);
        tvDisplayName = view.findViewById(R.id.tv_display_name);
        tvBio = view.findViewById(R.id.tv_bio);
        swNotifications = view.findViewById(R.id.sw_notifications);
        swDarkMode = view.findViewById(R.id.sw_dark_mode);

        setupInsets(view);
        bindUser();
        setupPreferences(view);
        setupRows(view);
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

    private void bindUser() {
        // TODO: thay bằng dữ liệu thật từ Firebase khi có AuthRepository.
        //   FirebaseUser u = FirebaseAuth.getInstance().getCurrentUser();
        //   name = u.getDisplayName(); email = u.getEmail(); photo = u.getPhotoUrl();
        String name = "Alex Morgan";
        String bio = "Here for good conversations.";

        tvDisplayName.setText(name);
        tvBio.setText(bio);
        tvInitials.setText(initialsOf(name));
        // TODO: có ảnh đại diện thì load vào img_avatar (Glide) và ẩn tv_initials.
    }

    private void setupPreferences(View root) {
        SharedPreferences prefs = requireContext()
                .getSharedPreferences(PREFS, Context.MODE_PRIVATE);

        // Đặt trạng thái trước rồi mới gắn listener để khỏi bắn sự kiện giả
        swNotifications.setChecked(prefs.getBoolean(KEY_NOTIFICATIONS, true));
        swNotifications.setOnCheckedChangeListener((button, checked) ->
                prefs.edit().putBoolean(KEY_NOTIFICATIONS, checked).apply());

        // TODO: Dark mode mới là UI, chưa nối logic. App đang khóa theme Light,
        // chưa có bảng màu tối nên bật lên sẽ vỡ giao diện.

        // Bấm cả dòng cũng gạt được switch
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
        // TODO: FirebaseAuth.getInstance().signOut();

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