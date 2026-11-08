package com.group12.chatly.data.model;

import com.google.firebase.Timestamp;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Chat implements Serializable {

    public static final String TYPE_DIRECT = "DIRECT";
    public static final String TYPE_GROUP = "GROUP";

    private String chatId;
    private String type;
    private String name;
    private String avatarUrl;
    private List<String> participants = new ArrayList<>();
    private List<String> adminIds = new ArrayList<>();
    private Map<String, Object> lastMessage = new HashMap<>();
    private Timestamp createdAt;
    private Timestamp updatedAt;
    // Thêm từ #23
    private Map<String, Long> unreadCount = new HashMap<>();
    private Map<String, Timestamp> lastReadAt = new HashMap<>();
    private Map<String, Timestamp> typingAt = new HashMap<>();

    public Chat() {
    }

    public Chat(String chatId, String type, List<String> participants) {
        this.chatId = chatId;
        this.type = type;
        this.participants = participants != null ? participants : new ArrayList<>();
        this.name = "";
        this.avatarUrl = "";
        this.createdAt = Timestamp.now();
        this.updatedAt = Timestamp.now();
    }

    public String getChatId() { return chatId; }
    public void setChatId(String chatId) { this.chatId = chatId; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
    public List<String> getParticipants() { return participants; }
    public void setParticipants(List<String> participants) { this.participants = participants; }
    public List<String> getAdminIds() { return adminIds; }
    public void setAdminIds(List<String> adminIds) { this.adminIds = adminIds; }
    public Map<String, Object> getLastMessage() { return lastMessage; }
    public void setLastMessage(Map<String, Object> lastMessage) { this.lastMessage = lastMessage; }
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }

    public Map<String, Long> getUnreadCount() { return unreadCount; }
    public void setUnreadCount(Map<String, Long> unreadCount) { this.unreadCount = unreadCount; }
    public Map<String, Timestamp> getLastReadAt() { return lastReadAt; }
    public void setLastReadAt(Map<String, Timestamp> lastReadAt) { this.lastReadAt = lastReadAt; }
    public Map<String, Timestamp> getTypingAt() { return typingAt; }
    public void setTypingAt(Map<String, Timestamp> typingAt) { this.typingAt = typingAt; }



    /** UID của người còn lại trong chat 1-1. */
    public String otherMemberId(String myUid) {
        if (participants == null) return null;
        for (String id : participants) {
            if (!id.equals(myUid)) return id;
        }
        return null;
    }

    public long unreadFor(String uid) {
        if (unreadCount == null) return 0;
        Long n = unreadCount.get(uid);
        return n == null ? 0 : n;
    }

    public long readAtMillisOf(String uid) {
        if (lastReadAt == null) return 0;
        Timestamp t = lastReadAt.get(uid);
        return t == null ? 0 : t.toDate().getTime();
    }

    public long typingAtMillisOf(String uid) {
        if (typingAt == null) return 0;
        Timestamp t = typingAt.get(uid);
        return t == null ? 0 : t.toDate().getTime();
    }

    public LastMessage lastMessageInfo() {
        if (lastMessage == null || lastMessage.isEmpty()) return null;
        LastMessage info = new LastMessage();
        Object s = lastMessage.get("senderId");
        Object t = lastMessage.get("text");
        Object ty = lastMessage.get("type");
        Object ts = lastMessage.get("timestamp");
        info.senderId = s instanceof String ? (String) s : null;
        info.text = t instanceof String ? (String) t : null;
        info.type = ty instanceof String ? (String) ty : null;
        info.timestamp = ts instanceof Timestamp ? (Timestamp) ts : null;
        return info;
    }

    public static class LastMessage implements Serializable {
        private String senderId;
        private String text;
        private String type;
        private Timestamp timestamp;

        public String getSenderId() { return senderId; }
        public String getText() { return text; }
        public String getType() { return type; }
        public Timestamp getTimestamp() { return timestamp; }
    }
}