package com.group12.chatly.ui.profile;

import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.group12.chatly.R;
import com.group12.chatly.data.model.User;
import com.group12.chatly.viewmodel.ProfileViewModel;

import java.util.Locale;

public class EditProfileActivity extends AppCompatActivity {

    private static final int BIO_MAX = 120;

    private ProfileViewModel profileViewModel;
    private TextView tvInitials, tvBioCounter;
    private TextInputLayout tilFullName, tilPhone;
    private TextInputEditText etFullName, etBio, etEmail, etPhone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        profileViewModel = new ViewModelProvider(this).get(ProfileViewModel.class);

        tvInitials = findViewById(R.id.tv_initials);
        tvBioCounter = findViewById(R.id.tv_bio_counter);
        tilFullName = findViewById(R.id.til_full_name);
        tilPhone = findViewById(R.id.til_phone);
        etFullName = findViewById(R.id.et_full_name);
        etBio = findViewById(R.id.et_bio);
        etEmail = findViewById(R.id.et_email);
        etPhone = findViewById(R.id.et_phone);
        MaterialButton btnSave = findViewById(R.id.btn_save);

        setupInsets();

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        findViewById(R.id.btn_change_photo).setOnClickListener(v -> showChangePhotoSheet());
        findViewById(R.id.tv_change_photo_link).setOnClickListener(v -> showChangePhotoSheet());
        btnSave.setOnClickListener(v -> save());

        etBio.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                updateBioCounter(s.length());
            }
        });
        updateBioCounter(0);

        observeUser();
        profileViewModel.loadUserProfile();
    }

    private void setupInsets() {
        View header = findViewById(R.id.header_edit_profile);
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

    private void observeUser() {
        profileViewModel.getUserProfileState().observe(this, state -> {
            if (state == null || !state.isSuccess() || state.getData() == null) return;
            User u = state.getData();
            String name = u.getDisplayName() != null ? u.getDisplayName() : "";
            etFullName.setText(name);
            etBio.setText(u.getBio() != null ? u.getBio() : "");
            etEmail.setText(u.getEmail() != null ? u.getEmail() : "");
            etPhone.setText(u.getPhoneNumber() != null ? u.getPhoneNumber() : "");
            tvInitials.setText(initialsOf(name));
        });
    }

    private void updateBioCounter(int length) {
        tvBioCounter.setText(getString(R.string.edit_bio_counter, length, BIO_MAX));
    }

    private void save() {
        tilFullName.setError(null);
        tilPhone.setError(null);

        String name = String.valueOf(etFullName.getText()).trim();
        String phone = String.valueOf(etPhone.getText()).trim();

        if (name.isEmpty()) {
            tilFullName.setError(getString(R.string.edit_error_name));
            return;
        }
        if (!phone.isEmpty() && !Patterns.PHONE.matcher(phone).matches()) {
            tilPhone.setError(getString(R.string.edit_error_phone));
            return;
        }
        // TODO: gọi profileViewModel.updateProfile(name, bio, phone)
        Toast.makeText(this, "Saved (demo)", Toast.LENGTH_SHORT).show();
    }

    private void showChangePhotoSheet() {
        BottomSheetDialog sheet = new BottomSheetDialog(this);
        View content = getLayoutInflater().inflate(R.layout.sheet_change_photo, null, false);
        sheet.setContentView(content);

        View parent = (View) content.getParent();
        parent.setBackgroundColor(Color.TRANSPARENT);

        content.findViewById(R.id.btn_sheet_close).setOnClickListener(v -> sheet.dismiss());
        content.findViewById(R.id.row_take_photo).setOnClickListener(v -> {
            sheet.dismiss();
            // TODO: mở camera
        });
        content.findViewById(R.id.row_choose_gallery).setOnClickListener(v -> {
            sheet.dismiss();
            // TODO: mở thư viện ảnh
        });
        content.findViewById(R.id.row_remove_photo).setOnClickListener(v -> {
            sheet.dismiss();
            // TODO: xóa ảnh đại diện
        });
        sheet.show();
    }

    private static String initialsOf(String name) {
        if (name == null) return "?";
        String[] parts = name.trim().split("\\s+");
        if (parts[0].isEmpty()) return "?";
        String first = parts[0].substring(0, 1);
        String last = parts.length > 1 ? parts[parts.length - 1].substring(0, 1) : "";
        return (first + last).toUpperCase(Locale.getDefault());
    }

    private abstract static class SimpleTextWatcher implements TextWatcher {
        @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
        @Override public void onTextChanged(CharSequence s, int start, int before, int count) { }
    }
}