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
                .update("isDeleted", true, "content", "Tin nhắn đã bị thu hồi")
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
}
