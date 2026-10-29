package com.group12.chatly.ui.chat;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.ListenerRegistration;
import com.group12.chatly.R;
import com.group12.chatly.data.model.Message;

import com.group12.chatly.data.repository.AuthCallback;
import com.group12.chatly.data.repository.AuthRepository;
import com.group12.chatly.data.repository.ChatRepository;

import java.util.List;

public class ChatActivity extends AppCompatActivity {

    public static final String EXTRA_TARGET_USER_ID = "extra_target_user_id";
    public static final String EXTRA_TARGET_USER_NAME = "extra_target_user_name";

    private static final long TYPING_THROTTLE_MS = 2500; // đang gõ: ghi tối đa 1 lần / 2,5s
    private static final long TYPING_IDLE_MS = 3000;     // ngừng gõ 3s thì tắt
    private static final long TYPING_EXPIRE_MS = 6000;   // người kia im 6s thì tự ẩn "typing…"

    private String currentUserId;
    private String targetUserId;
    private String targetUserName;
    private String chatId;

    private RecyclerView rcvMessages;
    private EditText etMessage;
    private ImageButton btnSend, btnBack;
    private TextView tvUserName, tvUserStatus;

    private MessageAdapter messageAdapter;
    private ChatRepository chatRepository;
    private ListenerRegistration messageListenerRegistration;
    private ListenerRegistration chatListenerRegistration;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable stopTypingRunnable = this::stopTyping;
    private final Runnable hideTypingRunnable = () -> showTypingStatus(false);

    // Trạng thái gõ của mình
    private boolean iAmTyping = false;
    private long lastTypingSentAt = 0;

    // Trạng thái gõ của người kia
    private boolean typingBaselineSet = false;
    private long lastOtherTypingMillis = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        currentUserId = AuthRepository.getInstance().getCurrentUserId();
        targetUserId = getIntent().getStringExtra(EXTRA_TARGET_USER_ID);
        targetUserName = getIntent().getStringExtra(EXTRA_TARGET_USER_NAME);

        if (currentUserId == null || targetUserId == null) {
            Toast.makeText(this, "User information missing", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        chatId = ChatRepository.generateChatId(currentUserId, targetUserId);
        chatRepository = ChatRepository.getInstance();

        initViews();
        setupRecyclerView();
        setupListeners();
        listenMessages();
        listenChatState();
    }

    private void initViews() {
        rcvMessages = findViewById(R.id.rcvMessages);
        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);
        btnBack = findViewById(R.id.btnBack);
        tvUserName = findViewById(R.id.tvUserName);
        tvUserStatus = findViewById(R.id.tvUserStatus);

        tvUserName.setText(targetUserName != null ? targetUserName : "User");
        tvUserStatus.setText(R.string.chat_status_online);
    }

    private void setupRecyclerView() {
        messageAdapter = new MessageAdapter(currentUserId);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        rcvMessages.setLayoutManager(layoutManager);
        rcvMessages.setAdapter(messageAdapter);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnSend.setOnClickListener(v -> {
            String text = etMessage.getText().toString().trim();
            if (!TextUtils.isEmpty(text)) {
                sendMessageText(text);
            }
        });

        // Gõ chữ -> báo "đang gõ"; xóa hết hoặc gửi xong (ô trống) -> tắt
        etMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    onUserTyping();
                } else {
                    stopTyping();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void sendMessageText(String text) {
        etMessage.setText(""); // ô trống -> TextWatcher tự tắt trạng thái gõ
        Message msg = Message.newText(currentUserId, "", "", text);

        chatRepository.sendMessage(chatId, targetUserId, msg, new AuthCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                // Sent successfully
            }

            @Override
            public void onFailure(String errorMessage) {
                Toast.makeText(ChatActivity.this, "Failed: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void listenMessages() {
        messageListenerRegistration = chatRepository.listenForMessages(chatId, new ChatRepository.MessagesListener() {
            @Override
            public void onMessagesUpdated(List<Message> messages) {
                messageAdapter.setMessages(messages);
                if (!messages.isEmpty()) {
                    rcvMessages.smoothScrollToPosition(messages.size() - 1);

                    // Tin cuối là của người kia: coi như đã đọc, và họ đã gửi xong nên bỏ "typing…"
                    Message last = messages.get(messages.size() - 1);
                    if (!currentUserId.equals(last.getSenderId())) {
                        chatRepository.markAsRead(chatId, currentUserId);
                        handler.removeCallbacks(hideTypingRunnable);
                        showTypingStatus(false);
                    }
                }
            }

            @Override
            public void onError(String error) {
                Toast.makeText(ChatActivity.this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /** Theo dõi document chat: người kia đã đọc đến đâu (✓✓) và có đang gõ không. */
    private void listenChatState() {
        chatListenerRegistration = chatRepository.listenForChat(chatId, (chat, fromCache) -> {
            long otherReadAt = chat == null ? 0 : chat.readAtMillisOf(targetUserId);
            messageAdapter.setOtherReadAtMillis(otherReadAt);

            // Dữ liệu từ bộ nhớ đệm có thể là "typing" cũ -> bỏ qua, chờ dữ liệu từ server
            if (!fromCache) {
                handleOtherTyping(chat == null ? 0 : chat.typingAtMillisOf(targetUserId));
            }
        });
    }

    // ---------- Typing: phía người gõ ----------

    private void onUserTyping() {
        long now = SystemClock.elapsedRealtime();
        if (!iAmTyping || now - lastTypingSentAt >= TYPING_THROTTLE_MS) {
            chatRepository.setTyping(chatId, currentUserId, true);
            lastTypingSentAt = now;
            iAmTyping = true;
        }
        handler.removeCallbacks(stopTypingRunnable);
        handler.postDelayed(stopTypingRunnable, TYPING_IDLE_MS);
    }

    private void stopTyping() {
        handler.removeCallbacks(stopTypingRunnable);
        if (chatRepository == null) return;
        if (iAmTyping) {
            iAmTyping = false;
            chatRepository.setTyping(chatId, currentUserId, false);
        }
    }

    // ---------- Typing: phía người xem ----------

    private void handleOtherTyping(long typingMillis) {
        // Lần đầu: chỉ ghi nhận mốc ban đầu, không hiện (có thể là giá trị cũ)
        if (!typingBaselineSet) {
            typingBaselineSet = true;
            lastOtherTypingMillis = typingMillis;
            return;
        }

        if (typingMillis == 0) { // người kia đã tắt
            lastOtherTypingMillis = 0;
            handler.removeCallbacks(hideTypingRunnable);
            showTypingStatus(false);
            return;
        }

        if (typingMillis != lastOtherTypingMillis) { // có lần "đang gõ" mới
            lastOtherTypingMillis = typingMillis;
            showTypingStatus(true);
            handler.removeCallbacks(hideTypingRunnable);
            handler.postDelayed(hideTypingRunnable, TYPING_EXPIRE_MS);
        }
    }

    private void showTypingStatus(boolean typing) {
        tvUserStatus.setText(typing
                ? R.string.chat_status_typing
                : R.string.chat_status_online);

        if (messageAdapter == null) return;
        // Đang ở cuối danh sách thì cuộn theo để thấy bong bóng; đang đọc tin cũ thì không kéo đi
        boolean atBottom = !rcvMessages.canScrollVertically(1);
        messageAdapter.setTyping(typing);
        if (typing && atBottom) {
            rcvMessages.scrollToPosition(messageAdapter.getItemCount() - 1);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        chatRepository.markAsRead(chatId, currentUserId);
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopTyping(); // rời màn hình là tắt "đang gõ"
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
        if (messageListenerRegistration != null) {
            messageListenerRegistration.remove();
        }
        if (chatListenerRegistration != null) {
            chatListenerRegistration.remove();
        }
    }
}