package com.group12.chatly.ui.contacts;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.group12.chatly.R;
import com.group12.chatly.data.model.Friendship;
import com.group12.chatly.data.model.User;
import com.group12.chatly.ui.chat.ChatActivity;
import com.group12.chatly.viewmodel.AuthState;
import com.group12.chatly.viewmodel.ContactViewModel;

import java.util.List;
import java.util.Locale;

public class AddFriendActivity extends AppCompatActivity {

    private static final long SEARCH_DEBOUNCE_MS = 400;
    private static final int MIN_QUERY_LENGTH = 3;

    private static final String[] COUNTRY_CODES = new String[]{
            "+84 (Vietnam)",
            "+1 (US/Canada)",
            "+44 (UK)",
            "+81 (Japan)",
            "+82 (South Korea)",
            "+65 (Singapore)",
            "+61 (Australia)",
            "+49 (Germany)",
            "+33 (France)",
            "+86 (China)"
    };

    private TextView tvTabEmail, tvTabPhone, tvFieldLabel, tvHelper;
    private TextView tvAvatar, tvName, tvResultEmail, tvFound, tvSendText;
    private View layoutCountryCode;
    private TextView tvCountryCode;
    private TextInputLayout tilAddFriend;
    private TextInputEditText etAddFriend;
    private View layoutResult, btnSend, btnCancel;
    private ImageView ivStatusCheck;

    private ContactViewModel contactViewModel;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable pendingSearch;

    private boolean emailMode = true;
    private boolean isAlreadyFriend = false;
    private User currentTarget;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friend);

        tvTabEmail = findViewById(R.id.tvAddFriendTabEmail);
        tvTabPhone = findViewById(R.id.tvAddFriendTabPhone);
        tvFieldLabel = findViewById(R.id.tvAddFriendFieldLabel);
        tvHelper = findViewById(R.id.tvAddFriendHelper);
        layoutCountryCode = findViewById(R.id.layoutCountryCode);
        tvCountryCode = findViewById(R.id.tvCountryCode);
        tilAddFriend = findViewById(R.id.tilAddFriend);
        etAddFriend = findViewById(R.id.etAddFriend);
        layoutResult = findViewById(R.id.layoutAddFriendResult);
        tvAvatar = findViewById(R.id.tvAddFriendAvatar);
        tvName = findViewById(R.id.tvAddFriendName);
        tvResultEmail = findViewById(R.id.tvAddFriendResultEmail);
        tvFound = findViewById(R.id.tvAddFriendFound);
        tvSendText = findViewById(R.id.tvAddFriendSend);
        ivStatusCheck = findViewById(R.id.ivAddFriendStatusCheck);
        btnSend = findViewById(R.id.btnAddFriendSend);
        btnCancel = findViewById(R.id.btnAddFriendCancel);

        contactViewModel = new ViewModelProvider(this).get(ContactViewModel.class);

        setupInsets();
        hideResult(); // layout đang chứa dữ liệu mẫu, ẩn đi cho tới khi có kết quả thật

        findViewById(R.id.btnAddFriendBack).setOnClickListener(v -> finish());
        tvTabEmail.setOnClickListener(v -> selectTab(true));
        tvTabPhone.setOnClickListener(v -> selectTab(false));
        if (layoutCountryCode != null) {
            layoutCountryCode.setOnClickListener(v -> showCountryCodePicker());
        }

        btnSend.setOnClickListener(v -> {
            if (currentTarget != null && !TextUtils.isEmpty(currentTarget.getUserId())) {
                if (isAlreadyFriend) {
                    Intent intent = new Intent(this, ChatActivity.class);
                    intent.putExtra(ChatActivity.EXTRA_TARGET_USER_ID, currentTarget.getUserId());
                    intent.putExtra(ChatActivity.EXTRA_TARGET_USER_NAME, currentTarget.getDisplayName());
                    startActivity(intent);
                } else {
                    contactViewModel.sendFriendRequest(currentTarget.getUserId());
                }
            }
        });

        if (btnCancel != null) {
            btnCancel.setOnClickListener(v -> {
                if (currentTarget != null && !TextUtils.isEmpty(currentTarget.getUserId())) {
                    contactViewModel.cancelFriendRequest(currentTarget.getUserId());
                }
            });
        }

        setupSearchInput();
        observeViewModel();
    }

    private void showCountryCodePicker() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Select Country Code")
                .setItems(COUNTRY_CODES, (dialog, which) -> {
                    String selected = COUNTRY_CODES[which];
                    String code = selected.split(" ")[0];
                    if (tvCountryCode != null) {
                        tvCountryCode.setText(code);
                    }
                    if (etAddFriend != null && etAddFriend.getText() != null) {
                        scheduleSearch(etAddFriend.getText().toString());
                    }
                })
                .show();
    }

    private void setupSearchInput() {
        etAddFriend.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                scheduleSearch(s == null ? "" : s.toString());
            }
        });
    }

    private void scheduleSearch(String raw) {
        cancelPendingSearch();
        String input = raw.trim();
        if (input.length() < MIN_QUERY_LENGTH) {
            hideResult();
            return;
        }
        final String searchParam;
        if (emailMode) {
            searchParam = input.toLowerCase(Locale.ROOT);
        } else {
            String countryCode = (tvCountryCode != null && tvCountryCode.getText() != null)
                    ? tvCountryCode.getText().toString() : "+84";
            searchParam = countryCode + input;
        }
        pendingSearch = () -> contactViewModel.searchUsers(searchParam);
        handler.postDelayed(pendingSearch, SEARCH_DEBOUNCE_MS);
    }

    private void cancelPendingSearch() {
        if (pendingSearch != null) {
            handler.removeCallbacks(pendingSearch);
            pendingSearch = null;
        }
    }

    private void observeViewModel() {
        contactViewModel.getSearchResultState().observe(this, state -> {
            if (state == null) return;
            if (state.getStatus() == AuthState.Status.SUCCESS) {
                showMatchOrHide(state.getData());
            } else if (state.getStatus() == AuthState.Status.ERROR) {
                hideResult();
            }
        });

        contactViewModel.getFriendshipStatusState().observe(this, state -> {
            if (state == null) return;
            if (state.getStatus() == AuthState.Status.SUCCESS) {
                updateFriendshipStateUI(state.getData());
            }
        });

        contactViewModel.getSendRequestState().observe(this, state -> {
            if (state == null) return;
            switch (state.getStatus()) {
                case LOADING:
                    setSending(true);
                    break;
                case SUCCESS:
                    setSending(false);
                    Friendship f = state.getData();
                    boolean nowFriends = f != null && Friendship.STATUS_ACCEPTED.equals(f.getStatus());
                    Toast.makeText(this,
                            nowFriends ? R.string.add_friend_now_friends : R.string.add_friend_sent,
                            Toast.LENGTH_SHORT).show();
                    updateFriendshipStateUI(f);
                    break;
                case ERROR:
                    setSending(false);
                    if (state.getErrorMessage() != null) {
                        Toast.makeText(this, state.getErrorMessage(), Toast.LENGTH_SHORT).show();
                    }
                    break;
                default:
                    break;
            }
        });

        contactViewModel.getActionState().observe(this, state -> {
            if (state == null) return;
            if (state.getStatus() == AuthState.Status.SUCCESS) {
                Toast.makeText(this, R.string.add_friend_request_cancelled, Toast.LENGTH_SHORT).show();
                updateFriendshipStateUI(null);
            }
        });
    }

    /** UI chỉ hiện "exact match": chọn user khớp đúng email/số điện thoại đang nhập. */
    private void showMatchOrHide(List<User> users) {
        String input = etAddFriend.getText() == null ? "" : etAddFriend.getText().toString().trim();
        User match = null;
        if (users != null) {
            for (User u : users) {
                if (isExactMatch(u, input)) {
                    match = u;
                    break;
                }
            }
        }
        if (match == null) {
            hideResult();
            return;
        }
        currentTarget = match;
        String name = TextUtils.isEmpty(match.getDisplayName())
                ? getString(R.string.chats_unknown_user) : match.getDisplayName();
        tvName.setText(name);
        tvAvatar.setText(initialsOf(name));

        if (emailMode) {
            tvResultEmail.setText(match.getEmail() == null ? "" : match.getEmail());
        } else {
            String countryCode = (tvCountryCode != null && tvCountryCode.getText() != null)
                    ? tvCountryCode.getText().toString() : "+84";
            tvResultEmail.setText(formatPhoneDisplay(match.getPhoneNumber(), countryCode, input));
        }
        layoutResult.setVisibility(View.VISIBLE);

        // Kiểm tra xem đã là bạn bè / gửi lời mời kết bạn chưa
        if (!TextUtils.isEmpty(match.getUserId())) {
            contactViewModel.checkFriendshipStatus(match.getUserId());
        }
    }

    private void updateFriendshipStateUI(Friendship friendship) {
        if (friendship != null && Friendship.STATUS_ACCEPTED.equals(friendship.getStatus())) {
            // Trạng thái: ĐÃ LÀ BẠN BÈ
            isAlreadyFriend = true;
            if (ivStatusCheck != null) ivStatusCheck.setVisibility(View.GONE);
            if (tvFound != null) {
                tvFound.setText(R.string.add_friend_already_status);
                tvFound.setTextColor(ContextCompat.getColor(this, R.color.surface_deep));
            }
            if (btnSend != null) btnSend.setVisibility(View.VISIBLE);
            if (tvSendText != null) tvSendText.setText(R.string.add_friend_message);
            if (btnCancel != null) btnCancel.setVisibility(View.GONE);
        } else if (friendship != null && Friendship.STATUS_PENDING.equals(friendship.getStatus())) {
            // Trạng thái: ĐÃ GỬI LỜI MỜI (WAITING)
            isAlreadyFriend = false;
            if (ivStatusCheck != null) ivStatusCheck.setVisibility(View.VISIBLE);
            if (tvFound != null) {
                tvFound.setText(R.string.add_friend_requested_status);
                tvFound.setTextColor(ContextCompat.getColor(this, R.color.register_blue));
            }
            if (btnSend != null) btnSend.setVisibility(View.GONE);
            if (btnCancel != null) btnCancel.setVisibility(View.VISIBLE);
        } else {
            // Trạng thái: CHƯA KẾT BẠN
            isAlreadyFriend = false;
            if (ivStatusCheck != null) ivStatusCheck.setVisibility(View.GONE);
            if (tvFound != null) {
                tvFound.setText(R.string.add_friend_found);
                tvFound.setTextColor(ContextCompat.getColor(this, R.color.surface_deep));
            }
            if (btnSend != null) btnSend.setVisibility(View.VISIBLE);
            if (tvSendText != null) tvSendText.setText(R.string.add_friend_send);
            if (btnCancel != null) btnCancel.setVisibility(View.GONE);
        }
    }

    private static String formatPhoneDisplay(String dbPhone, String countryCode, String input) {
        if (!TextUtils.isEmpty(dbPhone)) {
            String digits = dbPhone.replaceAll("\\D", "");
            if (dbPhone.startsWith("+")) {
                // e.g. +84912345678 -> +84 912 345 678
                if (digits.length() >= 11 && digits.startsWith("84")) {
                    return "+84 " + digits.substring(2, 5) + " " + digits.substring(5, 8) + " " + digits.substring(8);
                }
                return dbPhone;
            } else if (digits.length() == 10 && digits.startsWith("0")) {
                return countryCode + " " + digits.substring(1, 4) + " " + digits.substring(4, 7) + " " + digits.substring(7);
            }
            return dbPhone;
        }
        // Fallback display
        String rawDigits = input.replaceAll("\\D", "");
        if (rawDigits.length() == 9) {
            return countryCode + " " + rawDigits.substring(0, 3) + " " + rawDigits.substring(3, 6) + " " + rawDigits.substring(6);
        }
        return countryCode + " " + input;
    }

    private boolean isExactMatch(User u, String input) {
        if (u == null || input.isEmpty()) return false;
        if (emailMode) {
            return u.getEmail() != null && u.getEmail().trim().equalsIgnoreCase(input);
        }
        String countryCode = (tvCountryCode != null && tvCountryCode.getText() != null)
                ? tvCountryCode.getText().toString() : "+84";
        return samePhone(u.getPhoneNumber(), countryCode + input) || samePhone(u.getPhoneNumber(), input);
    }

    /** So sánh số điện thoại theo 9 số cuối, bỏ qua 0 đầu / +84 / khoảng trắng. */
    private static boolean samePhone(String a, String b) {
        if (a == null || b == null) return false;
        String da = a.replaceAll("\\D", "");
        String db = b.replaceAll("\\D", "");
        if (da.isEmpty() || db.isEmpty()) return false;
        if (da.length() < 9 || db.length() < 9) return da.equals(db);
        return da.substring(da.length() - 9).equals(db.substring(db.length() - 9));
    }

    private static String initialsOf(String name) {
        if (name == null || name.trim().isEmpty()) return "?";
        String[] parts = name.trim().split("\\s+");
        if (parts.length == 1) {
            return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
        }
        return (parts[0].substring(0, 1) + parts[parts.length - 1].substring(0, 1)).toUpperCase();
    }

    private void hideResult() {
        currentTarget = null;
        layoutResult.setVisibility(View.GONE);
        updateFriendshipStateUI(null);
    }

    private void setSending(boolean sending) {
        btnSend.setEnabled(!sending);
        btnSend.setAlpha(sending ? 0.5f : 1f);
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
        emailMode = email;
        cancelPendingSearch();
        hideResult();

        setBackgroundKeepPadding(tvTabEmail, email ? R.drawable.bg_filter_active : 0);
        setBackgroundKeepPadding(tvTabPhone, email ? 0 : R.drawable.bg_filter_active);

        if (layoutCountryCode != null) {
            layoutCountryCode.setVisibility(email ? View.GONE : View.VISIBLE);
        }

        tvFieldLabel.setText(email ? R.string.add_friend_label_email : R.string.add_friend_label_phone);
        tvHelper.setText(email ? R.string.add_friend_helper_email : R.string.add_friend_helper_phone);
        etAddFriend.setHint(email ? R.string.add_friend_hint_email : R.string.add_friend_hint_phone);
        tilAddFriend.setStartIconDrawable(email ? R.drawable.ic_reg_mail : R.drawable.ic_prof_phone);
        etAddFriend.setInputType(email
                ? InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
                : InputType.TYPE_CLASS_PHONE);
        etAddFriend.setText("");
    }

    private static void setBackgroundKeepPadding(View view, int resId) {
        if (view == null) return;
        int left = view.getPaddingLeft();
        int top = view.getPaddingTop();
        int right = view.getPaddingRight();
        int bottom = view.getPaddingBottom();
        view.setBackgroundResource(resId);
        view.setPadding(left, top, right, bottom);
    }

    @Override
    protected void onDestroy() {
        cancelPendingSearch();
        super.onDestroy();
    }
}
