package com.group12.chatly.ui.contacts;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.group12.chatly.R;

import java.util.Locale;

public class AddFriendActivity extends AppCompatActivity {

    /** Các trạng thái của khối kết quả (khớp các frame Figma). */
    private enum ResultState { FOUND, ALREADY_FRIEND, SELF, NOT_FOUND }

    // TODO: bỏ khi làm logic tìm người. Đổi giá trị để xem từng frame trên máy.
    private static final ResultState PREVIEW_STATE = ResultState.FOUND;

    private TextView tvTabEmail, tvTabPhone, tvFieldLabel, tvHelper;
    private TextInputLayout tilAddFriend;
    private TextInputEditText etAddFriend;

    private View layoutResult, layoutEmpty;
    private TextView tvAvatar, tvName, tvResultEmail, tvFound, tvSendLabel;
    private View btnSend, tvSelfTitle, tvSelfNote, tvSelfButton;

    private ResultState resultState = ResultState.FOUND;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friend);

        tvTabEmail = findViewById(R.id.tvAddFriendTabEmail);
        tvTabPhone = findViewById(R.id.tvAddFriendTabPhone);
        tvFieldLabel = findViewById(R.id.tvAddFriendFieldLabel);
        tvHelper = findViewById(R.id.tvAddFriendHelper);
        tilAddFriend = findViewById(R.id.tilAddFriend);
        etAddFriend = findViewById(R.id.etAddFriend);

        layoutResult = findViewById(R.id.layoutAddFriendResult);
        layoutEmpty = findViewById(R.id.layoutAddFriendEmpty);
        tvAvatar = findViewById(R.id.tvAddFriendAvatar);
        tvName = findViewById(R.id.tvAddFriendName);
        tvResultEmail = findViewById(R.id.tvAddFriendResultEmail);
        tvFound = findViewById(R.id.tvAddFriendFound);
        tvSendLabel = findViewById(R.id.tvAddFriendSendLabel);
        btnSend = findViewById(R.id.btnAddFriendSend);
        tvSelfTitle = findViewById(R.id.tvAddFriendSelfTitle);
        tvSelfNote = findViewById(R.id.tvAddFriendSelfNote);
        tvSelfButton = findViewById(R.id.tvAddFriendSelfButton);

        setupInsets();
        setupClicks();
        showPreview();
    }



    private void setupInsets() {
        View root = findViewById(R.id.rootAddFriend);
        View header = findViewById(R.id.layoutAddFriendHeader);
        View content = findViewById(R.id.layoutAddFriendContent);
        final int headerTop = header.getPaddingTop();
        final int contentBottom = content.getPaddingBottom();

        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets bars = insets.getInsets(
                    WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout());
            header.setPadding(header.getPaddingLeft(), headerTop + bars.top,
                    header.getPaddingRight(), header.getPaddingBottom());
            content.setPadding(content.getPaddingLeft(), content.getPaddingTop(),
                    content.getPaddingRight(), contentBottom + bars.bottom);
            return insets;
        });
        ViewCompat.requestApplyInsets(root);
    }

    private void setupClicks() {
        findViewById(R.id.btnAddFriendBack).setOnClickListener(v -> finish());
        tvTabEmail.setOnClickListener(v -> selectTab(true));
        tvTabPhone.setOnClickListener(v -> selectTab(false));

        btnSend.setOnClickListener(v -> {
            if (resultState == ResultState.ALREADY_FRIEND) {
                // TODO: mở ChatActivity với người này
            } else if (resultState == ResultState.FOUND) {
                // TODO: gửi lời mời kết bạn
            }
        });

        findViewById(R.id.btnAddFriendInvite).setOnClickListener(v -> inviteFriends());
    }

    /** Mời bạn bè qua app chia sẻ của máy, cùng nội dung với nút ở màn rỗng của Contacts. */
    private void inviteFriends() {
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("text/plain");
        share.putExtra(Intent.EXTRA_TEXT, getString(R.string.contacts_invite_text));
        startActivity(Intent.createChooser(share, getString(R.string.contacts_empty_btn_invite)));
    }

    /** Dữ liệu mẫu để so giao diện với Figma. Thay bằng kết quả tìm thật ở bước logic. */
    private void showPreview() {
        switch (PREVIEW_STATE) {
            case ALREADY_FRIEND:
                bindResult("Minh Anh", "minh.anh@email.com", ResultState.ALREADY_FRIEND);
                break;
            case SELF:
                bindResult("Alex Morgan", "alex.morgan@email.com", ResultState.SELF);
                break;
            case NOT_FOUND:
                showNotFound();
                break;
            default:
                bindResult("An Võ", "an.vo@email.com", ResultState.FOUND);
        }
    }

    // ---------------------------------------------------------------- UI

    /** Đổi giữa tab email và phone: nền tab, nhãn, icon, kiểu bàn phím, dòng phụ. */
    private void selectTab(boolean email) {
        tvTabEmail.setBackgroundResource(email ? R.drawable.bg_filter_active : android.R.color.transparent);
        tvTabPhone.setBackgroundResource(email ? android.R.color.transparent : R.drawable.bg_filter_active);

        tvFieldLabel.setText(email ? R.string.add_friend_label_email : R.string.add_friend_label_phone);
        tvHelper.setText(email ? R.string.add_friend_helper_email : R.string.add_friend_helper_phone);
        etAddFriend.setHint(email ? R.string.add_friend_hint_email : R.string.add_friend_hint_phone);
        tilAddFriend.setStartIconDrawable(email ? R.drawable.ic_reg_mail : R.drawable.ic_prof_phone);
        etAddFriend.setInputType(email
                ? InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
                : InputType.TYPE_CLASS_PHONE);
        etAddFriend.setText("");
    }

    /** Hiện kết quả tìm được (FOUND / ALREADY_FRIEND / SELF). */
    private void bindResult(String name, String email, ResultState state) {
        resultState = state;
        layoutResult.setVisibility(View.VISIBLE);
        layoutEmpty.setVisibility(View.GONE);

        boolean self = state == ResultState.SELF;

        tvAvatar.setText(initialsOf(name));
        tvName.setText(name);
        tvResultEmail.setText(email);


        tvFound.setVisibility(self ? View.GONE : View.VISIBLE);
        btnSend.setVisibility(self ? View.GONE : View.VISIBLE);
        if (!self) {
            boolean friend = state == ResultState.ALREADY_FRIEND;
            tvFound.setText(friend ? R.string.add_friend_already : R.string.add_friend_found);
            tvSendLabel.setText(friend ? R.string.add_friend_message : R.string.add_friend_send);
        }

        int selfVisibility = self ? View.VISIBLE : View.GONE;
        tvSelfTitle.setVisibility(selfVisibility);
        tvSelfNote.setVisibility(selfVisibility);
        tvSelfButton.setVisibility(selfVisibility);
    }

    /** Không tìm thấy ai: ẩn khối kết quả, hiện khối "no one here just yet". */
    private void showNotFound() {
        resultState = ResultState.NOT_FOUND;
        layoutResult.setVisibility(View.GONE);
        layoutEmpty.setVisibility(View.VISIBLE);
    }

    private static String initialsOf(String name) {
        if (name == null || name.trim().isEmpty()) return "?";
        String[] parts = name.trim().split("\\s+");
        String first = parts[0].substring(0, 1);
        String last = parts.length > 1 ? parts[parts.length - 1].substring(0, 1) : "";
        return (first + last).toUpperCase(Locale.getDefault());
    }
}