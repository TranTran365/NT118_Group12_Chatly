package com.group12.chatly.ui.contacts;

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

public class AddFriendActivity extends AppCompatActivity {

    private TextView tvTabEmail, tvTabPhone, tvFieldLabel, tvHelper;
    private TextInputLayout tilAddFriend;
    private TextInputEditText etAddFriend;

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

        setupInsets();

        findViewById(R.id.btnAddFriendBack).setOnClickListener(v -> finish());
        tvTabEmail.setOnClickListener(v -> selectTab(true));
        tvTabPhone.setOnClickListener(v -> selectTab(false));

        findViewById(R.id.btnAddFriendSend).setOnClickListener(v -> {
            // TODO (bước sau): gửi lời mời kết bạn
        });
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
}