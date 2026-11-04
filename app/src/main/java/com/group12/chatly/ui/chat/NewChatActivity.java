package com.group12.chatly.ui.chat;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.group12.chatly.R;
import com.group12.chatly.data.model.User;
import com.group12.chatly.data.repository.AuthCallback;
import com.group12.chatly.data.repository.AuthRepository;
import com.group12.chatly.data.repository.ChatRepository;
import com.group12.chatly.ui.chats.ChatAdapter;
import com.group12.chatly.ui.chats.ChatItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Màn chọn người để bắt đầu một cuộc trò chuyện 1-1. */
public class NewChatActivity extends AppCompatActivity {

    private String myUid;
    private final List<ChatItem> allUsers = new ArrayList<>();
    private String query = "";

    private ChatAdapter adapter;
    private TextView tvEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_chat);

        myUid = AuthRepository.getInstance().getCurrentUserId();
        if (myUid == null) {
            finish();
            return;
        }

        RecyclerView rcvUsers = findViewById(R.id.rcv_users);
        EditText etSearch = findViewById(R.id.etSearch);
        ImageButton btnBack = findViewById(R.id.btnBack);
        tvEmpty = findViewById(R.id.tv_new_chat_empty);

        adapter = new ChatAdapter(this::openChat);
        rcvUsers.setLayoutManager(new LinearLayoutManager(this));
        rcvUsers.setAdapter(adapter);

        btnBack.setOnClickListener(v -> finish());

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                query = s.toString();
                applyFilter();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        loadUsers();
    }

    private void loadUsers() {
        ChatRepository.getInstance().getAllUsers(new AuthCallback<List<User>>() {
            @Override
            public void onSuccess(List<User> users) {
                allUsers.clear();
                for (User user : users) {
                    String uid = user.getUserId();
                    if (TextUtils.isEmpty(uid) || uid.equals(myUid)) continue; // bỏ chính mình

                    String name = TextUtils.isEmpty(user.getDisplayName())
                            ? getString(R.string.chats_unknown_user) : user.getDisplayName();
                    // Dòng phụ: ưu tiên bio, không có thì hiện email
                    String sub = !TextUtils.isEmpty(user.getBio()) ? user.getBio() : user.getEmail();

                    allUsers.add(new ChatItem(
                            ChatRepository.generateChatId(myUid, uid), uid, name,
                            user.getAvatarUrl(), sub == null ? "" : sub, 0, 0, false));
                }
                applyFilter();
            }

            @Override
            public void onFailure(String errorMessage) {
                Toast.makeText(NewChatActivity.this, errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void applyFilter() {
        String q = query.trim().toLowerCase(Locale.getDefault());
        List<ChatItem> out = new ArrayList<>();
        for (ChatItem item : allUsers) {
            if (q.isEmpty() || item.name.toLowerCase(Locale.getDefault()).contains(q)) {
                out.add(item);
            }
        }
        adapter.submitList(out);
        tvEmpty.setVisibility(out.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void openChat(ChatItem item) {
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra(ChatActivity.EXTRA_TARGET_USER_ID, item.otherUserId);
        intent.putExtra(ChatActivity.EXTRA_TARGET_USER_NAME, item.name);
        startActivity(intent);
        finish(); // quay lại sẽ về tab Chats, không phải màn chọn người
    }
}