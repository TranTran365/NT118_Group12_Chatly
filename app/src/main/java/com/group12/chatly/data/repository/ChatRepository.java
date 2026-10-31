package com.group12.chatly.data.repository;

import com.group12.chatly.data.model.User;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.WriteBatch;
import com.group12.chatly.data.model.Chat;
import com.group12.chatly.data.model.Message;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatRepository {

    private static final String COLLECTION_CHATS = "chats";
    private static final String SUB_COLLECTION_MESSAGES = "messages";

    private static volatile ChatRepository instance;
    private final FirebaseFirestore firestore;

    public ChatRepository() {
        this.firestore = FirebaseFirestore.getInstance();
    }

    public static ChatRepository getInstance() {
        if (instance == null) {
            synchronized (ChatRepository.class) {
                if (instance == null) {
                    instance = new ChatRepository();
                }
            }
        }
        return instance;
    }

    public static String generateChatId(String userId1, String userId2) {
        List<String> ids = Arrays.asList(userId1, userId2);
        Collections.sort(ids);
        return ids.get(0) + "_" + ids.get(1);
    }

    /**
     * Gửi tin nhắn: trong 1 batch vừa ghi tin vào chats/{chatId}/messages,
     * vừa tạo/cập nhật document chats/{chatId} (members, lastMessage, unreadCount, updatedAt).
     */
    public void sendMessage(String chatId, String receiverId, Message message,
                            AuthCallback<Void> callback) {
        String senderId = message.getSenderId();

        DocumentReference chatRef = firestore.collection(COLLECTION_CHATS).document(chatId);
        DocumentReference msgRef = chatRef.collection(SUB_COLLECTION_MESSAGES).document();

        // members luôn sắp xếp để hai phía ghi ra cùng một thứ tự (rule so sánh members không đổi)
        List<String> members = Arrays.asList(senderId, receiverId);
        Collections.sort(members);

        Map<String, Object> lastMessage = new HashMap<>();
        lastMessage.put("senderId", senderId);
        lastMessage.put("text", previewOf(message));
        lastMessage.put("type", message.getType());
        lastMessage.put("timestamp", FieldValue.serverTimestamp());
        lastMessage.put("readBy", Arrays.asList(senderId));

        // Người nhận +1 chưa đọc, người gửi về 0
        Map<String, Object> unread = new HashMap<>();
        unread.put(receiverId, FieldValue.increment(1));
        unread.put(senderId, 0);

        Map<String, Object> lastReadAt = new HashMap<>();
        lastReadAt.put(senderId, FieldValue.serverTimestamp());

        Map<String, Object> chatData = new HashMap<>();
        chatData.put("type", Chat.TYPE_DIRECT);
        chatData.put("members", members);
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

    /** Lắng nghe realtime danh sách chat của mình, mới nhất lên đầu. */
    public ListenerRegistration listenForChats(String userId, ChatsListener listener) {
        return firestore.collection(COLLECTION_CHATS)
                .whereArrayContains("members", userId)
                .orderBy("updatedAt", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        listener.onError(error.getMessage());
                        return;
                    }
                    if (value != null) {
                        List<Chat> chats = new ArrayList<>();
                        for (DocumentSnapshot doc : value.getDocuments()) {
                            Chat chat = doc.toObject(Chat.class,
                                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE);
                            if (chat != null) {
                                chat.setChatId(doc.getId());
                                chats.add(chat);
                            }
                        }
                        listener.onChatsUpdated(chats);
                    }
                });
    }

    /** Lắng nghe realtime một cuộc trò chuyện (đã đọc đến đâu, ai đang gõ). */
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
                    Chat chat = doc.toObject(Chat.class,
                            DocumentSnapshot.ServerTimestampBehavior.ESTIMATE);
                    if (chat != null) chat.setChatId(doc.getId());
                    listener.onChatChanged(chat, fromCache);
                });
    }

    public interface ChatsListener {
        void onChatsUpdated(List<Chat> chats);
        void onError(String error);
    }

    /** Bật/tắt trạng thái đang gõ của mình. */
    public void setTyping(String chatId, String myUid, boolean typing) {
        Object value = typing ? FieldValue.serverTimestamp() : FieldValue.delete();
        firestore.collection(COLLECTION_CHATS).document(chatId)
                .update("typingAt." + myUid, value);
    }

    /** Đánh dấu mình đã đọc chat: đưa số chưa đọc về 0 và ghi giờ đọc lần cuối. */
    public void markAsRead(String chatId, String myUid) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("unreadCount." + myUid, 0);
        updates.put("lastReadAt." + myUid, FieldValue.serverTimestamp());
        firestore.collection(COLLECTION_CHATS).document(chatId).update(updates);
    }

    /** Lấy tất cả user  */
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

    /** Lấy hồ sơ một user (để hiện tên, avatar trong danh sách chat). */
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

    private String previewOf(Message message) {
        if (Message.TYPE_TEXT.equals(message.getType())) {
            return message.getContent();
        }
        return message.getType(); // IMAGE/VOICE...: tạm để tên loại, sẽ đổi thành "Photo"/"Voice message" sau
    }

    public interface MessagesListener {
        void onMessagesUpdated(List<Message> messages);
        void onError(String error);
    }

    public interface ChatListener {
        void onChatChanged(Chat chat, boolean fromCache);
    }
}