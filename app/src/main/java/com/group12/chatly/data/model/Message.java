package com.group12.chatly.data.model;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.ServerTimestamp;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Model for a chat message, mapped to Firestore sub-collection "chats/{chatId}/messages".
 * Hiện chỉ dùng TYPE_TEXT; các loại còn lại để dành cho phần multimedia.
 */
public class Message implements Serializable {

    public static final String TYPE_TEXT = "TEXT";
    public static final String TYPE_IMAGE = "IMAGE";
    public static final String TYPE_VOICE = "VOICE";
    public static final String TYPE_VIDEO = "VIDEO";
    public static final String TYPE_FILE = "FILE";
    public static final String TYPE_LOCATION = "LOCATION";

    private String messageId;
    private String senderId;
    private String senderName;
    private String senderAvatar;
    private String type;
    private String content;
    private List<String> readBy;
    @ServerTimestamp
    private Timestamp createdAt;
    // Lưu trên Firestore với tên field "deleted" (do getter isDeleted()).
    private boolean deleted;

    /** Required default public constructor for Firestore deserialization. */
    public Message() {
    }

    /** Tạo tin nhắn text mới; createdAt do server gán (@ServerTimestamp). */
    public static Message newText(String senderId, String senderName, String senderAvatar, String text) {
        Message m = new Message();
        m.senderId = senderId;
        m.senderName = senderName;
        m.senderAvatar = senderAvatar;
        m.type = TYPE_TEXT;
        m.content = text;
        m.readBy = new ArrayList<>();
        m.readBy.add(senderId);
        m.deleted = false;
        return m;
    }

    public String getMessageId() { return messageId; }
    public void setMessageId(String messageId) { this.messageId = messageId; }

    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }

    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }

    public String getSenderAvatar() { return senderAvatar; }
    public void setSenderAvatar(String senderAvatar) { this.senderAvatar = senderAvatar; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public List<String> getReadBy() { return readBy; }
    public void setReadBy(List<String> readBy) { this.readBy = readBy; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public boolean isDeleted() { return deleted; }
    public void setDeleted(boolean deleted) { this.deleted = deleted; }
}