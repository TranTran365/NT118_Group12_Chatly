package com.group12.chatly.ui.contacts;


import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.group12.chatly.R;
import com.group12.chatly.data.model.Contact;
import com.group12.chatly.data.model.User;
import com.group12.chatly.data.repository.AuthCallback;
import com.group12.chatly.data.repository.ContactRepository;

import java.util.Collections;
import java.util.List;

public class ContactProfileBottomSheet extends BottomSheetDialogFragment {

    public static final String TAG = "ContactProfileSheet";
    private static final String ARG_CONTACT = "arg_contact";

    private Contact contact;
    private TextView tvBio, tvEmail, tvPhone;

    public static ContactProfileBottomSheet newInstance(Contact contact) {
        ContactProfileBottomSheet sheet = new ContactProfileBottomSheet();
        Bundle args = new Bundle();
        args.putSerializable(ARG_CONTACT, contact);
        sheet.setArguments(args);
        return sheet;
    }

    @Override
    public int getTheme() {
        return R.style.ThemeOverlay_Chatly_ContactSheet;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            contact = (Contact) getArguments().getSerializable(ARG_CONTACT);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.sheet_contact_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (contact == null) {
            dismiss();
            return;
        }

        TextView tvInitials = view.findViewById(R.id.tvSheetInitials);
        ImageView ivAvatar = view.findViewById(R.id.ivSheetAvatar);
        View vOnlineDot = view.findViewById(R.id.vSheetOnlineDot);
        TextView tvName = view.findViewById(R.id.tvSheetName);
        TextView tvStatus = view.findViewById(R.id.tvSheetStatus);
        tvBio = view.findViewById(R.id.tvSheetBio);
        tvEmail = view.findViewById(R.id.tvSheetEmail);
        tvPhone = view.findViewById(R.id.tvSheetPhone);

        // Phần có sẵn trong Contact
        tvName.setText(contact.getDisplayName());
        tvStatus.setText(contact.isOnline() ? "Online" : contact.getStatusText());
        tvStatus.setVisibility(TextUtils.isEmpty(tvStatus.getText()) ? View.GONE : View.VISIBLE);
        vOnlineDot.setVisibility(contact.isOnline() ? View.VISIBLE : View.GONE);

        if (!TextUtils.isEmpty(contact.getAvatarUrl())) {
            ivAvatar.setVisibility(View.VISIBLE);
            tvInitials.setVisibility(View.GONE);
            Glide.with(this).load(contact.getAvatarUrl()).circleCrop().into(ivAvatar);
        } else {
            ivAvatar.setVisibility(View.GONE);
            tvInitials.setVisibility(View.VISIBLE);
            tvInitials.setText(contact.getInitials());
        }

        // Nút message / call -> đẩy về ContactsFragment xử lý
        view.findViewById(R.id.btnSheetMessage).setOnClickListener(v -> forward(ContactActionsDialogFragment.Action.MESSAGE));
        view.findViewById(R.id.btnSheetCall).setOnClickListener(v -> forward(ContactActionsDialogFragment.Action.CALL));

        loadExtraInfo();
    }

    private void forward(ContactActionsDialogFragment.Action action) {
        Fragment parent = getParentFragment();
        if (parent instanceof ContactActionsDialogFragment.Listener) {
            ((ContactActionsDialogFragment.Listener) parent).onContactAction(contact, action);
        }
        dismiss();
    }

    /** Bio, email, sđt phải query thêm từ bảng users. */
    private void loadExtraInfo() {
        if (TextUtils.isEmpty(contact.getUserId())) return;

        ContactRepository.getInstance().getUsersByIds(
                Collections.singletonList(contact.getUserId()),
                new AuthCallback<List<User>>() {
                    @Override
                    public void onSuccess(List<User> users) {
                        if (!isAdded() || users == null || users.isEmpty()) return;
                        User u = users.get(0);
                        show(tvBio, u.getBio());
                        show(tvEmail, u.getEmail());
                        show(tvPhone, u.getPhoneNumber());
                    }

                    @Override
                    public void onFailure(String errorMessage) {
                        // Không load được thì thôi, vẫn còn tên + avatar
                    }
                });
    }

    private void show(TextView tv, String text) {
        if (tv == null || TextUtils.isEmpty(text)) return;
        tv.setText(text);
        tv.setVisibility(View.VISIBLE);
    }
}
