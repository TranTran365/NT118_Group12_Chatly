package com.group12.chatly.ui.chat;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Lifecycle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.ListenerRegistration;
import com.group12.chatly.R;
import com.group12.chatly.data.model.Message;

import com.group12.chatly.data.repository.AuthCallback;
import com.group12.chatly.data.repository.AuthRepository;
import com.group12.chatly.data.repository.ChatRepository;

import java.util.List;
public class ChatActivity extends AppCompatActivity implements AttachmentBottomSheet.Listener {

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
    private ImageButton btnSend, btnBack, btnMic;
    private ImageView btnCamera;
    private TextView tvUserName, tvUserStatus;
    private View btnScrollBottom;

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

    private ImageView btnAttachment;

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
        btnMic = findViewById(R.id.btnMic);
        btnCamera = findViewById(R.id.btnCamera);
        tvUserName = findViewById(R.id.tvUserName);
        tvUserStatus = findViewById(R.id.tvUserStatus);
        btnAttachment = findViewById(R.id.btnAttachment);
        btnScrollBottom = findViewById(R.id.btnScrollBottom);

        tvUserName.setText(targetUserName != null ? targetUserName : "User");
        tvUserStatus.setText(R.string.chat_status_online);

        ImageView imgAvatar = findViewById(R.id.imgAvatar);
        chatRepository.getUserById(targetUserId, new AuthCallback<com.group12.chatly.data.model.User>() {
            @Override
            public void onSuccess(com.group12.chatly.data.model.User user) {
                if (user != null && !isFinishing() && !isDestroyed()) {
                    if (!TextUtils.isEmpty(user.getDisplayName())) {
                        tvUserName.setText(user.getDisplayName());
                    }
                    if (!TextUtils.isEmpty(user.getAvatarUrl()) && imgAvatar != null) {
                        com.bumptech.glide.Glide.with(ChatActivity.this)
                                .load(user.getAvatarUrl())
                                .circleCrop()
                                .into(imgAvatar);
                    }
                }
            }

            @Override
            public void onFailure(String errorMessage) {
            }
        });
    }

    private void setupRecyclerView() {
        messageAdapter = new MessageAdapter(currentUserId);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        rcvMessages.setLayoutManager(layoutManager);
        rcvMessages.setAdapter(messageAdapter);

        if (btnScrollBottom != null) {
            btnScrollBottom.setOnClickListener(v -> {
                rcvMessages.smoothScrollToPosition(messageAdapter.getItemCount() - 1);
                btnScrollBottom.setVisibility(View.GONE);
                if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED)) {
                    chatRepository.markAsRead(chatId, currentUserId);
                }
            });
        }

        rcvMessages.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (btnScrollBottom != null && !recyclerView.canScrollVertically(1)) {
                    // Chỉ gọi markAsRead nếu nút "New message" đang hiện
                    if (btnScrollBottom.getVisibility() == View.VISIBLE) {
                        btnScrollBottom.setVisibility(View.GONE);
                        if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED)) {
                            chatRepository.markAsRead(chatId, currentUserId);
                        }
                    }
                }
            }
        });
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnSend.setOnClickListener(v -> {
            String text = etMessage.getText().toString().trim();
            if (!TextUtils.isEmpty(text)) {
                sendMessageText(text);
            }
        });

        btnAttachment.setOnClickListener(v ->
                new AttachmentBottomSheet().show(getSupportFragmentManager(), AttachmentBottomSheet.TAG));

        // Gõ chữ -> báo "đang gõ" + hiện nút Send, giấu Camera & Mic; xóa hết/ô trống -> hiện Camera & Mic, giấu Send
        etMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                boolean hasText = s.toString().trim().length() > 0;
                btnSend.setVisibility(hasText ? View.VISIBLE : View.GONE);
                if (btnMic != null) btnMic.setVisibility(hasText ? View.GONE : View.VISIBLE);
                if (btnCamera != null) btnCamera.setVisibility(hasText ? View.GONE : View.VISIBLE);

                if (hasText) {
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
                etMessage.setText(text);
                etMessage.setSelection(text.length());
            }
        });
    }

    private void listenMessages() {
        messageListenerRegistration = chatRepository.listenForMessages(chatId, new ChatRepository.MessagesListener() {
            @Override
            public void onMessagesUpdated(List<Message> messages) {
                boolean atBottom = !rcvMessages.canScrollVertically(1);
                boolean isFirstLoad = messageAdapter.getItemCount() == 0;

                messageAdapter.setMessages(messages);

                if (!messages.isEmpty()) {
                    Message last = messages.get(messages.size() - 1);
                    boolean isMyMessage = currentUserId.equals(last.getSenderId());

                    if (isFirstLoad) {
                        rcvMessages.scrollToPosition(messageAdapter.getItemCount() - 1);
                    } else if (atBottom || isMyMessage) {
                        rcvMessages.smoothScrollToPosition(messageAdapter.getItemCount() - 1);
                        if (btnScrollBottom != null) btnScrollBottom.setVisibility(View.GONE);
                    } else {
                        // Đang đọc tin cũ mà có tin mới tới -> hiện nút "New message ↓"
                        if (btnScrollBottom != null) btnScrollBottom.setVisibility(View.VISIBLE);
                    }

                    // Tin cuối là của người kia: coi như đã đọc nếu màn hình đang hiển thị VÀ đang ở đáy
                    if (!isMyMessage) {
                        if ((atBottom || isFirstLoad) && getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED)) {
                            chatRepository.markAsRead(chatId, currentUserId);
                        }
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

    @Override
    public void onAttachmentOption(AttachmentBottomSheet.Option option) {
        // TODO: nối với MediaPickerHelper / ghi âm / vị trí
        Toast.makeText(this, option.name(), Toast.LENGTH_SHORT).show();
    }
}