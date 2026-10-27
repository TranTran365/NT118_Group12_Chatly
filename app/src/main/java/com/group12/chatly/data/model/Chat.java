package com.group12.chatly.data.model;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.ServerTimestamp;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Data Model for a conversation, mapped to Firestore collection "chats".
 * Hiện chỉ dùng TYPE_DIRECT (chat 1-1); group để dành cho sau.
 */
public class Chat implements Serializable {

    public static final String TYPE_DIRECT = "DIRECT";
    public static final String TYPE_GROUP = "GROUP";

    private String chatId;
    private String type;
    private String name;
    private String avatarUrl;
    private List<String> members;
    private LastMessage lastMessage;
    @ServerTimestamp
    private Timestamp updatedAt;

    /** Required default public constructor for Firestore deserialization. */
    public Chat() {
    }

    /**
     * ChatId cho chat 1-1: hai UID sắp xếp rồi nối bằng "_",
     * để hai người mở chat nhiều lần vẫn chỉ ra đúng một document.
     */
    public static String directChatId(String uidA, String uidB) {
        return uidA.compareTo(uidB) <= 0 ? uidA + "_" + uidB : uidB + "_" + uidA;
    }

    public static Chat newDirect(String uidA, String uidB) {
        Chat c = new Chat();
        c.chatId = directChatId(uidA, uidB);
        c.type = TYPE_DIRECT;
        c.name = "";
        c.avatarUrl = "";
        c.members = new ArrayList<>(Arrays.asList(uidA, uidB));
        return c;
    }

    /** UID của người còn lại trong chat 1-1 (null nếu không tìm thấy). */
    public String otherMemberId(String myUid) {
        if (members == null) return null;
        for (String id : members) {
            if (!id.equals(myUid)) return id;
        }
        return null;
    }

    public String getChatId() { return chatId; }
    public void setChatId(String chatId) { this.chatId = chatId; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

    public List<String> getMembers() { return members; }
    public void setMembers(List<String> members) { this.members = members; }

    public LastMessage getLastMessage() { return lastMessage; }
    public void setLastMessage(LastMessage lastMessage) { this.lastMessage = lastMessage; }

    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }


    public static class LastMessage implements Serializable {
        private String senderId;
        private String text;
        private String type;
        private Timestamp timestamp;
        private List<String> readBy;

        public LastMessage() {
        }

        public LastMessage(String senderId, String text, String type, List<String> readBy) {
            this.senderId = senderId;
            this.text = text;
            this.type = type;
            this.readBy = readBy;
        }

        public String getSenderId() { return senderId; }
        public void setSenderId(String senderId) { this.senderId = senderId; }

        public String getText() { return text; }
        public void setText(String text) { this.text = text; }

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }

        public Timestamp getTimestamp() { return timestamp; }
        public void setTimestamp(Timestamp timestamp) { this.timestamp = timestamp; }

        public List<String> getReadBy() { return readBy; }
        public void setReadBy(List<String> readBy) { this.readBy = readBy; }
    }
}