package com.group12.chatly.data.repository;

import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.group12.chatly.data.model.Chat;
import com.group12.chatly.data.model.Message;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.WriteBatch;
import com.group12.chatly.data.model.User;

/**
 * Repository managing Chat rooms (Direct 1-1, Group) and Realtime Messages.
 */
public class ChatRepository {

    public static final String COLLECTION_CHATS = "chats";
    public static final String SUB_COLLECTION_MESSAGES = "messages";

    private static volatile ChatRepository instance;
    private final FirebaseFirestore firestore;
    private final StorageRepository storageRepository;

    public ChatRepository(@NonNull FirebaseFirestore firestore, @NonNull StorageRepository storageRepository) {
        this.firestore = firestore;
        this.storageRepository = storageRepository;
    }

    public static ChatRepository getInstance() {
        if (instance == null) {
            synchronized (ChatRepository.class) {
                if (instance == null) {
                    instance = new ChatRepository(
                            FirebaseFirestore.getInstance(),
                            StorageRepository.getInstance()
                    );
                }
            }
        }
        return instance;
    }

    /**
     * Builds a composite ID for direct 1-1 chat.
     */
    public static String getDirectChatId(@NonNull String userA, @NonNull String userB) {
        return "direct_" + (userA.compareTo(userB) < 0 ? userA + "_" + userB : userB + "_" + userA);
    }

    /**
     * Retrieves or creates a Direct 1-1 chat between two users.
     */
    public void getOrCreateDirectChat(@NonNull String currentUserId,
                                      @NonNull String targetUserId,
                                      @NonNull AuthCallback<Chat> callback) {
        String chatId = getDirectChatId(currentUserId, targetUserId);

        firestore.collection(COLLECTION_CHATS)
                .document(chatId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        Chat chat = snapshot.toObject(Chat.class);
                        if (chat != null) {
                            callback.onSuccess(chat);
                            return;
                        }
                    }

                    // Create new direct chat
                    List<String> participants = Arrays.asList(currentUserId, targetUserId);
                    Chat newChat = new Chat(chatId, Chat.TYPE_DIRECT, participants);

                    firestore.collection(COLLECTION_CHATS)
                            .document(chatId)
                            .set(newChat)
                            .addOnSuccessListener(aVoid -> callback.onSuccess(newChat))
                            .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    /**
     * Creates a new Group Chat room.
     */
    public void createGroupChat(@NonNull String currentUserId,
                                @NonNull String groupName,
                                @Nullable String avatarUrl,
                                @NonNull List<String> memberIds,
                                @NonNull AuthCallback<Chat> callback) {
        String chatId = "group_" + UUID.randomUUID().toString();
        List<String> allParticipants = new ArrayList<>(memberIds);
        if (!allParticipants.contains(currentUserId)) {
            allParticipants.add(currentUserId);
        }

        Chat groupChat = new Chat(chatId, Chat.TYPE_GROUP, allParticipants);
        groupChat.setName(groupName);
        groupChat.setAvatarUrl(avatarUrl != null ? avatarUrl : "");
        groupChat.setAdminIds(Collections.singletonList(currentUserId));

        firestore.collection(COLLECTION_CHATS)
                .document(chatId)
                .set(groupChat)
                .addOnSuccessListener(aVoid -> callback.onSuccess(groupChat))
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    /**
     * Updates Group Chat Name.
     */
    public void updateGroupName(@NonNull String chatId, @NonNull String newName, @NonNull AuthCallback<Void> callback) {
        firestore.collection(COLLECTION_CHATS)
                .document(chatId)
                .update("name", newName, "updatedAt", Timestamp.now())
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    /**
     * Updates Group Chat Avatar.
     */
    public void updateGroupAvatar(@NonNull String chatId, @NonNull String avatarUrl, @NonNull AuthCallback<Void> callback) {
        firestore.collection(COLLECTION_CHATS)
                .document(chatId)
                .update("avatarUrl", avatarUrl, "updatedAt", Timestamp.now())
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    /**
     * Adds members to a Group Chat.
     */
    public void addGroupMembers(@NonNull String chatId, @NonNull List<String> newMemberIds, @NonNull AuthCallback<Void> callback) {
        firestore.collection(COLLECTION_CHATS)
                .document(chatId)
                .update("participants", FieldValue.arrayUnion(newMemberIds.toArray()), "updatedAt", Timestamp.now())
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    /**
     * Removes a member from a Group Chat.
     */
    public void removeGroupMember(@NonNull String chatId, @NonNull String memberId, @NonNull AuthCallback<Void> callback) {
        firestore.collection(COLLECTION_CHATS)
                .document(chatId)
                .update("participants", FieldValue.arrayRemove(memberId), "updatedAt", Timestamp.now())
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    /**
     * Base method to send a Message and update Chat's lastMessage.
     */
    public void sendMessage(@NonNull Message message, @NonNull AuthCallback<Message> callback) {
        if (message.getMessageId() == null || message.getMessageId().isEmpty()) {
            message.setMessageId(UUID.randomUUID().toString());
        }
        if (message.getCreatedAt() == null) {
            message.setCreatedAt(Timestamp.now());
        }

        firestore.collection(COLLECTION_CHATS)
                .document(message.getChatId())
                .collection(SUB_COLLECTION_MESSAGES)
                .document(message.getMessageId())
                .set(message)
                .addOnSuccessListener(aVoid -> {
                    // Update lastMessage on Chat document
                    Map<String, Object> lastMsg = new HashMap<>();
                    lastMsg.put("senderId", message.getSenderId());
                    lastMsg.put("text", message.getContent());
                    lastMsg.put("type", message.getType());
                    lastMsg.put("timestamp", message.getCreatedAt());
                    lastMsg.put("isReadBy", message.getReadBy());

                    firestore.collection(COLLECTION_CHATS)
                            .document(message.getChatId())
                            .update("lastMessage", lastMsg, "updatedAt", message.getCreatedAt());

                    callback.onSuccess(message);
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    /**
     * Sends plain text message.
     */
    public void sendTextMessage(@NonNull String chatId,
                                @NonNull String senderId,
                                @NonNull String senderName,
                                @Nullable String senderAvatar,
                                @NonNull String text,
                                @NonNull AuthCallback<Message> callback) {
        Message msg = new Message(UUID.randomUUID().toString(), chatId, senderId, senderName, Message.TYPE_TEXT, text);
        msg.setSenderAvatar(senderAvatar != null ? senderAvatar : "");
        sendMessage(msg, callback);
    }

    /**
     * Sends voice audio message (first uploads .m4a to Firebase Storage).
     */
    public void sendVoiceMessage(@NonNull String chatId,
                                 @NonNull String senderId,
                                 @NonNull String senderName,
                                 @Nullable String senderAvatar,
                                 @NonNull Uri voiceUri,
                                 int durationSec,
                                 @NonNull AuthCallback<Message> callback) {
        storageRepository.uploadVoiceMessage(chatId, voiceUri, new StorageCallback<String>() {
            @Override
            public void onProgress(int percentage) {
            }

            @Override
            public void onSuccess(String downloadUrl) {
                Message msg = new Message(UUID.randomUUID().toString(), chatId, senderId, senderName, Message.TYPE_VOICE, downloadUrl);
                msg.setSenderAvatar(senderAvatar != null ? senderAvatar : "");
                Map<String, Object> meta = new HashMap<>();
                meta.put("duration", durationSec);
                meta.put("mimeType", "audio/mp4");
                msg.setFileMetaData(meta);
                sendMessage(msg, callback);
            }

            @Override
            public void onFailure(String errorMessage) {
                callback.onFailure("Voice upload failed: " + errorMessage);
            }
        });
    }

    /**
     * Sends media message (Image, Video, Document).
     */
    public void sendMediaMessage(@NonNull String chatId,
                                 @NonNull String senderId,
                                 @NonNull String senderName,
                                 @Nullable String senderAvatar,
                                 @NonNull Uri mediaUri,
                                 @NonNull String mediaType,
                                 @NonNull String fileName,
                                 long fileSize,
                                 @NonNull AuthCallback<Message> callback) {
        storageRepository.uploadChatMedia(chatId, mediaUri, mediaType, fileName, new StorageCallback<String>() {
            @Override
            public void onProgress(int percentage) {
            }

            @Override
            public void onSuccess(String downloadUrl) {
                Message msg = new Message(UUID.randomUUID().toString(), chatId, senderId, senderName, mediaType, downloadUrl);
                msg.setSenderAvatar(senderAvatar != null ? senderAvatar : "");
                Map<String, Object> meta = new HashMap<>();
                meta.put("fileName", fileName);
                meta.put("fileSize", fileSize);
                meta.put("mimeType", mediaType);
                msg.setFileMetaData(meta);
                sendMessage(msg, callback);
            }

            @Override
            public void onFailure(String errorMessage) {
                callback.onFailure("Media upload failed: " + errorMessage);
            }
        });
    }

    /**
     * Sends location message (GPS coordinates).
     */
    public void sendLocationMessage(@NonNull String chatId,
                                    @NonNull String senderId,
                                    @NonNull String senderName,
                                    @Nullable String senderAvatar,
                                    double latitude,
                                    double longitude,
                                    @NonNull String address,
                                    @NonNull AuthCallback<Message> callback) {
        String coordContent = latitude + "," + longitude;
        Message msg = new Message(UUID.randomUUID().toString(), chatId, senderId, senderName, Message.TYPE_LOCATION, coordContent);
        msg.setSenderAvatar(senderAvatar != null ? senderAvatar : "");
        Map<String, Object> meta = new HashMap<>();
        meta.put("address", address);
        meta.put("latitude", latitude);
        meta.put("longitude", longitude);
        msg.setFileMetaData(meta);
        sendMessage(msg, callback);
    }

    /**
     * Marks a message as read by current user.
     */
    public void markMessageAsRead(@NonNull String chatId, @NonNull String messageId, @NonNull String currentUserId) {
        firestore.collection(COLLECTION_CHATS)
                .document(chatId)
                .collection(SUB_COLLECTION_MESSAGES)
                .document(messageId)
                .update("readBy", FieldValue.arrayUnion(currentUserId));
    }

    /**
     * Recalls (soft-deletes) a message.
     */
    public void recallMessage(@NonNull String chatId, @NonNull String messageId, @NonNull AuthCallback<Void> callback) {
        firestore.collection(COLLECTION_CHATS)
                .document(chatId)
                .collection(SUB_COLLECTION_MESSAGES)
                .document(messageId)
                .update("deleted", true, "content", "Tin nhắn đã bị thu hồi")
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    /**
     * Realtime listener for messages within a specific Chat room.
     */
    public ListenerRegistration listenMessages(@NonNull String chatId, @NonNull AuthCallback<List<Message>> callback) {
        return firestore.collection(COLLECTION_CHATS)
                .document(chatId)
                .collection(SUB_COLLECTION_MESSAGES)
                .orderBy("createdAt", Query.Direction.ASCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        callback.onFailure(error.getMessage());
                        return;
                    }
                    List<Message> messages = new ArrayList<>();
                    if (value != null) {
                        for (DocumentSnapshot doc : value.getDocuments()) {
                            Message m = doc.toObject(Message.class);
                            if (m != null) messages.add(m);
                        }
                    }
                    callback.onSuccess(messages);
                });
    }

    /**
     * Realtime listener for recent chats of the current user.
     */
    public ListenerRegistration listenRecentChats(@NonNull String currentUserId, @NonNull AuthCallback<List<Chat>> callback) {
        return firestore.collection(COLLECTION_CHATS)
                .whereArrayContains("participants", currentUserId)
                .orderBy("updatedAt", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        callback.onFailure(error.getMessage());
                        return;
                    }
                    List<Chat> chats = new ArrayList<>();
                    if (value != null) {
                        for (DocumentSnapshot doc : value.getDocuments()) {
                            Chat c = doc.toObject(Chat.class);
                            if (c != null) chats.add(c);
                        }
                    }
                    callback.onSuccess(chats);
                });
    }

    // ===== Từ PR #23 (UI chat 1-1) =====

    public interface MessagesListener {
        void onMessagesUpdated(List<Message> messages);
        void onError(String error);
    }

    public interface ChatsListener {
        void onChatsUpdated(List<Chat> chats);
        void onError(String error);
    }

    public interface ChatListener {
        void onChatChanged(Chat chat, boolean fromCache);
    }

    /** Gửi tin trong 1 batch: ghi tin + cập nhật chats/{chatId} (participants, lastMessage, unreadCount). */
    public void sendMessage(String chatId, String receiverId, Message message,
                            AuthCallback<Void> callback) {
        String senderId = message.getSenderId();

        DocumentReference chatRef = firestore.collection(COLLECTION_CHATS).document(chatId);
        DocumentReference msgRef = chatRef.collection(SUB_COLLECTION_MESSAGES).document();

        List<String> participants = Arrays.asList(senderId, receiverId);
        Collections.sort(participants);

        Map<String, Object> lastMessage = new HashMap<>();
        lastMessage.put("senderId", senderId);
        lastMessage.put("text", previewOf(message));
        lastMessage.put("type", message.getType());
        lastMessage.put("timestamp", FieldValue.serverTimestamp());
        lastMessage.put("readBy", Arrays.asList(senderId));

        Map<String, Object> unread = new HashMap<>();
        unread.put(receiverId, FieldValue.increment(1));
        unread.put(senderId, 0);

        Map<String, Object> lastReadAt = new HashMap<>();
        lastReadAt.put(senderId, FieldValue.serverTimestamp());

        Map<String, Object> chatData = new HashMap<>();
        chatData.put("type", Chat.TYPE_DIRECT);
        chatData.put("participants", participants);
        chatData.put("lastMessage", lastMessage);
        chatData.put("unreadCount", unread);
        chatData.put("lastReadAt", lastReadAt);
        chatData.put("updatedAt", FieldValue.serverTimestamp());

        WriteBatch batch = firestore.batch();
        batch.set(chatRef, chatData, SetOptions.merge());
        batch.set(msgRef, message);
        batch.commit()
                .addOnSuccessListener(unused -> callback.onSuccess(null))
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public ListenerRegistration listenForMessages(String chatId, MessagesListener listener) {
        return firestore.collection(COLLECTION_CHATS)
                .document(chatId)
                .collection(SUB_COLLECTION_MESSAGES)
                .orderBy("createdAt", Query.Direction.ASCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        listener.onError(error.getMessage());
                        return;
                    }
                    if (value != null) {
                        List<Message> messages = new ArrayList<>();
                        for (DocumentSnapshot doc : value.getDocuments()) {
                            Message msg = doc.toObject(Message.class, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE);
                            if (msg != null) {
                                msg.setMessageId(doc.getId());
                                messages.add(msg);
                            }
                        }
                        listener.onMessagesUpdated(messages);
                    }
                });
    }

    public ListenerRegistration listenForChats(String userId, ChatsListener listener) {
        return firestore.collection(COLLECTION_CHATS)
                .whereArrayContains("participants", userId)
                .orderBy("updatedAt", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        listener.onError(error.getMessage());
                        return;
                    }
                    if (value != null) {
                        List<Chat> chats = new ArrayList<>();
                        for (DocumentSnapshot doc : value.getDocuments()) {
                            Chat chat = doc.toObject(Chat.class, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE);
                            if (chat != null) {
                                chat.setChatId(doc.getId());
                                chats.add(chat);
                            }
                        }
                        listener.onChatsUpdated(chats);
                    }
                });
    }

    public ListenerRegistration listenForChat(String chatId, ChatListener listener) {
        return firestore.collection(COLLECTION_CHATS).document(chatId)
                .addSnapshotListener((doc, error) -> {
                    if (error != null || doc == null) {
                        listener.onChatChanged(null, false);
                        return;
                    }
                    boolean fromCache = doc.getMetadata().isFromCache();
                    if (!doc.exists()) {
                        listener.onChatChanged(null, fromCache);
                        return;
                    }
                    Chat chat = doc.toObject(Chat.class, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE);
                    if (chat != null) chat.setChatId(doc.getId());
                    listener.onChatChanged(chat, fromCache);
                });
    }

    public void setTyping(String chatId, String myUid, boolean typing) {
        Object value = typing ? FieldValue.serverTimestamp() : FieldValue.delete();
        firestore.collection(COLLECTION_CHATS).document(chatId)
                .update("typingAt." + myUid, value);
    }

    public void markAsRead(String chatId, String myUid) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("unreadCount." + myUid, 0);
        updates.put("lastReadAt." + myUid, FieldValue.serverTimestamp());
        firestore.collection(COLLECTION_CHATS).document(chatId).update(updates);
    }

    public void getAllUsers(AuthCallback<List<User>> callback) {
        firestore.collection("users").get()
                .addOnSuccessListener(snapshot -> {
                    List<User> users = new ArrayList<>();
                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        User user = doc.toObject(User.class);
                        if (user != null) users.add(user);
                    }
                    callback.onSuccess(users);
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public void getUserById(String uid, AuthCallback<User> callback) {
        firestore.collection("users").document(uid).get()
                .addOnSuccessListener(doc -> {
                    User user = doc.toObject(User.class);
                    if (user != null) {
                        callback.onSuccess(user);
                    } else {
                        callback.onFailure("User not found");
                    }
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public void deleteMessage(String chatId, String messageId, AuthCallback<Void> callback) {
        firestore.collection(COLLECTION_CHATS)
                .document(chatId)
                .collection(SUB_COLLECTION_MESSAGES)
                .document(messageId)
                .update("deleted", true, "content", "")
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    /** emoji = null để gỡ reaction của mình. */
    public void setReaction(String chatId, String messageId, String userId, String emoji, AuthCallback<Void> callback) {
        Map<String, Object> reactionData = new HashMap<>();
        Map<String, Object> userReaction = new HashMap<>();
        userReaction.put(userId, emoji == null ? FieldValue.delete() : emoji);
        reactionData.put("reactions", userReaction);

        firestore.collection(COLLECTION_CHATS)
                .document(chatId)
                .collection(SUB_COLLECTION_MESSAGES)
                .document(messageId)
                .set(reactionData, SetOptions.merge())
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    private String previewOf(Message message) {
        if (Message.TYPE_TEXT.equals(message.getType())) {
            return message.getContent();
        }
        return message.getType();
    }
}

