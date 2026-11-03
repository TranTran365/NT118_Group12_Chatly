package com.group12.chatly.viewmodel;

import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.firestore.ListenerRegistration;
import com.group12.chatly.data.model.Chat;
import com.group12.chatly.data.model.Message;
import com.group12.chatly.data.model.User;
import com.group12.chatly.data.repository.AuthCallback;
import com.group12.chatly.data.repository.AuthRepository;
import com.group12.chatly.data.repository.ChatRepository;

import java.util.List;

/**
 * ViewModel managing Chat sessions, Realtime Messages, and Group Chat operations.
 */
public class ChatViewModel extends ViewModel {

    private final ChatRepository chatRepository;
    private final AuthRepository authRepository;

    private final MutableLiveData<AuthState<Chat>> activeChatState = new MutableLiveData<>(AuthState.idle());
    private final MutableLiveData<AuthState<List<Message>>> messagesState = new MutableLiveData<>(AuthState.idle());
    private final MutableLiveData<AuthState<List<Chat>>> recentChatsState = new MutableLiveData<>(AuthState.idle());
    private final MutableLiveData<AuthState<Message>> sendMessageState = new MutableLiveData<>(AuthState.idle());
    private final MutableLiveData<AuthState<Void>> chatActionState = new MutableLiveData<>(AuthState.idle());

    private ListenerRegistration messagesListener;
    private ListenerRegistration recentChatsListener;
    private User cachedCurrentUser;

    public ChatViewModel() {
        this(ChatRepository.getInstance(), AuthRepository.getInstance());
    }

    public ChatViewModel(@NonNull ChatRepository chatRepository, @NonNull AuthRepository authRepository) {
        this.chatRepository = chatRepository;
        this.authRepository = authRepository;
        loadCurrentUser();
    }

    private void loadCurrentUser() {
        authRepository.getCurrentUserProfile(new AuthCallback<User>() {
            @Override
            public void onSuccess(User result) {
                cachedCurrentUser = result;
            }

            @Override
            public void onFailure(String errorMessage) {
            }
        });
    }

    public LiveData<AuthState<Chat>> getActiveChatState() {
        return activeChatState;
    }

    public LiveData<AuthState<List<Message>>> getMessagesState() {
        return messagesState;
    }

    public LiveData<AuthState<List<Chat>>> getRecentChatsState() {
        return recentChatsState;
    }

    public LiveData<AuthState<Message>> getSendMessageState() {
        return sendMessageState;
    }

    public LiveData<AuthState<Void>> getChatActionState() {
        return chatActionState;
    }

    /**
     * Initializes or retrieves 1-1 Chat with another user.
     */
    public void openDirectChat(@NonNull String targetUserId) {
        String currentUid = authRepository.getCurrentUserId();
        if (currentUid == null) {
            activeChatState.setValue(AuthState.error("User is not authenticated."));
            return;
        }

        activeChatState.setValue(AuthState.loading());
        chatRepository.getOrCreateDirectChat(currentUid, targetUserId, new AuthCallback<Chat>() {
            @Override
            public void onSuccess(Chat result) {
                activeChatState.setValue(AuthState.success(result));
                startListeningMessages(result.getChatId());
            }

            @Override
            public void onFailure(String errorMessage) {
                activeChatState.setValue(AuthState.error(errorMessage));
            }
        });
    }

    /**
     * Listens to messages in real-time for given chatId.
     */
    public void startListeningMessages(@NonNull String chatId) {
        stopMessagesListener();
        messagesState.setValue(AuthState.loading());

        messagesListener = chatRepository.listenMessages(chatId, new AuthCallback<List<Message>>() {
            @Override
            public void onSuccess(List<Message> result) {
                messagesState.setValue(AuthState.success(result));
            }

            @Override
            public void onFailure(String errorMessage) {
                messagesState.setValue(AuthState.error(errorMessage));
            }
        });
    }

    /**
     * Listens to recent chats for current user (Chats Tab).
     */
    public void startListeningRecentChats() {
        String currentUid = authRepository.getCurrentUserId();
        if (currentUid == null) return;

        stopRecentChatsListener();
        recentChatsState.setValue(AuthState.loading());

        recentChatsListener = chatRepository.listenRecentChats(currentUid, new AuthCallback<List<Chat>>() {
            @Override
            public void onSuccess(List<Chat> result) {
                recentChatsState.setValue(AuthState.success(result));
            }

            @Override
            public void onFailure(String errorMessage) {
                recentChatsState.setValue(AuthState.error(errorMessage));
            }
        });
    }

    /**
     * Sends plain text message.
     */
    public void sendTextMessage(@NonNull String chatId, @NonNull String text) {
        String currentUid = authRepository.getCurrentUserId();
        if (currentUid == null) return;

        String senderName = cachedCurrentUser != null ? cachedCurrentUser.getDisplayName() : "User";
        String senderAvatar = cachedCurrentUser != null ? cachedCurrentUser.getAvatarUrl() : "";

        sendMessageState.setValue(AuthState.loading());
        chatRepository.sendTextMessage(chatId, currentUid, senderName, senderAvatar, text, new AuthCallback<Message>() {
            @Override
            public void onSuccess(Message result) {
                sendMessageState.setValue(AuthState.success(result));
            }

            @Override
            public void onFailure(String errorMessage) {
                sendMessageState.setValue(AuthState.error(errorMessage));
            }
        });
    }

    /**
     * Sends recorded voice message.
     */
    public void sendVoiceMessage(@NonNull String chatId, @NonNull Uri voiceUri, int durationSec) {
        String currentUid = authRepository.getCurrentUserId();
        if (currentUid == null) return;

        String senderName = cachedCurrentUser != null ? cachedCurrentUser.getDisplayName() : "User";
        String senderAvatar = cachedCurrentUser != null ? cachedCurrentUser.getAvatarUrl() : "";

        sendMessageState.setValue(AuthState.loading());
        chatRepository.sendVoiceMessage(chatId, currentUid, senderName, senderAvatar, voiceUri, durationSec, new AuthCallback<Message>() {
            @Override
            public void onSuccess(Message result) {
                sendMessageState.setValue(AuthState.success(result));
            }

            @Override
            public void onFailure(String errorMessage) {
                sendMessageState.setValue(AuthState.error(errorMessage));
            }
        });
    }

    /**
     * Sends image or video or document message.
     */
    public void sendMediaMessage(@NonNull String chatId,
                                 @NonNull Uri mediaUri,
                                 @NonNull String mediaType,
                                 @NonNull String fileName,
                                 long fileSize) {
        String currentUid = authRepository.getCurrentUserId();
        if (currentUid == null) return;

        String senderName = cachedCurrentUser != null ? cachedCurrentUser.getDisplayName() : "User";
        String senderAvatar = cachedCurrentUser != null ? cachedCurrentUser.getAvatarUrl() : "";

        sendMessageState.setValue(AuthState.loading());
        chatRepository.sendMediaMessage(chatId, currentUid, senderName, senderAvatar, mediaUri, mediaType, fileName, fileSize, new AuthCallback<Message>() {
            @Override
            public void onSuccess(Message result) {
                sendMessageState.setValue(AuthState.success(result));
            }

            @Override
            public void onFailure(String errorMessage) {
                sendMessageState.setValue(AuthState.error(errorMessage));
            }
        });
    }

    /**
     * Sends GPS location message.
     */
    public void sendLocationMessage(@NonNull String chatId, double latitude, double longitude, @NonNull String address) {
        String currentUid = authRepository.getCurrentUserId();
        if (currentUid == null) return;

        String senderName = cachedCurrentUser != null ? cachedCurrentUser.getDisplayName() : "User";
        String senderAvatar = cachedCurrentUser != null ? cachedCurrentUser.getAvatarUrl() : "";

        sendMessageState.setValue(AuthState.loading());
        chatRepository.sendLocationMessage(chatId, currentUid, senderName, senderAvatar, latitude, longitude, address, new AuthCallback<Message>() {
            @Override
            public void onSuccess(Message result) {
                sendMessageState.setValue(AuthState.success(result));
            }

            @Override
            public void onFailure(String errorMessage) {
                sendMessageState.setValue(AuthState.error(errorMessage));
            }
        });
    }

    /**
     * Marks message as read by current user.
     */
    public void markAsRead(@NonNull String chatId, @NonNull String messageId) {
        String currentUid = authRepository.getCurrentUserId();
        if (currentUid != null) {
            chatRepository.markMessageAsRead(chatId, messageId, currentUid);
        }
    }

    /**
     * Recalls message.
     */
    public void recallMessage(@NonNull String chatId, @NonNull String messageId) {
        chatActionState.setValue(AuthState.loading());
        chatRepository.recallMessage(chatId, messageId, new AuthCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                chatActionState.setValue(AuthState.success(null));
            }

            @Override
            public void onFailure(String errorMessage) {
                chatActionState.setValue(AuthState.error(errorMessage));
            }
        });
    }

    /**
     * Creates new Group Chat.
     */
    public void createGroupChat(@NonNull String groupName, @Nullable String avatarUrl, @NonNull List<String> memberIds) {
        String currentUid = authRepository.getCurrentUserId();
        if (currentUid == null) return;

        activeChatState.setValue(AuthState.loading());
        chatRepository.createGroupChat(currentUid, groupName, avatarUrl, memberIds, new AuthCallback<Chat>() {
            @Override
            public void onSuccess(Chat result) {
                activeChatState.setValue(AuthState.success(result));
                startListeningMessages(result.getChatId());
            }

            @Override
            public void onFailure(String errorMessage) {
                activeChatState.setValue(AuthState.error(errorMessage));
            }
        });
    }

    public void stopMessagesListener() {
        if (messagesListener != null) {
            messagesListener.remove();
            messagesListener = null;
        }
    }

    public void stopRecentChatsListener() {
        if (recentChatsListener != null) {
            recentChatsListener.remove();
            recentChatsListener = null;
        }
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        stopMessagesListener();
        stopRecentChatsListener();
    }
}
