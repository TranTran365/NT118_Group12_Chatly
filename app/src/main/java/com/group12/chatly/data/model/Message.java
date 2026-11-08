package com.group12.chatly.data.model;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.ServerTimestamp;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Message implements Serializable {

    public static final String TYPE_TEXT = "TEXT";
    public static final String TYPE_IMAGE = "IMAGE";
    public static final String TYPE_VIDEO = "VIDEO";
    public static final String TYPE_VOICE = "VOICE";
    public static final String TYPE_FILE = "FILE";
    public static final String TYPE_LOCATION = "LOCATION";

    private String messageId;
    private String chatId;
    private String senderId;
    private String senderName;
    private String senderAvatar;
    private String receiverId;
    private String type = TYPE_TEXT;
    private String content = "";
    private Map<String, Object> fileMetaData = new HashMap<>();
    private List<String> readBy = new ArrayList<>();
    @ServerTimestamp
    private Timestamp createdAt;
    // Firestore lưu tên field là "deleted" (do getter isDeleted())
    private boolean deleted = false;
    // Thêm từ #23
    private Map<String, String> reactions = new HashMap<>(); // userId -> emoji
    private String caption;

    public Message() {
    }

    public Message(String messageId, String chatId, String senderId, String senderName, String type, String content) {
        this.messageId = messageId;
        this.chatId = chatId;
        this.senderId = senderId;
        this.senderName = senderName;
        this.type = type;
        this.content = content;
        this.readBy = new ArrayList<>();
        if (senderId != null) this.readBy.add(senderId);
        this.createdAt = Timestamp.now();
        this.deleted = false;
    }

    /** Từ #23: tin text mới, createdAt null để server gán. */
    public static Message newText(String senderId, String senderName, String senderAvatar, String text) {
        Message m = new Message();
        m.senderId = senderId;
        m.senderName = senderName;
        m.senderAvatar = senderAvatar;
        m.type = TYPE_TEXT;
        m.content = text;
        m.readBy = new ArrayList<>();
        m.readBy.add(senderId);
        return m;
    }

    public String getMessageId() { return messageId; }
    public void setMessageId(String messageId) { this.messageId = messageId; }
    public String getChatId() { return chatId; }
    public void setChatId(String chatId) { this.chatId = chatId; }
    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }
    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }
    public String getSenderAvatar() { return senderAvatar; }
    public void setSenderAvatar(String senderAvatar) { this.senderAvatar = senderAvatar; }
    public String getReceiverId() { return receiverId; }
    public void setReceiverId(String receiverId) { this.receiverId = receiverId; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public Map<String, Object> getFileMetaData() { return fileMetaData; }
    public void setFileMetaData(Map<String, Object> fileMetaData) { this.fileMetaData = fileMetaData; }
    public List<String> getReadBy() { return readBy; }
    public void setReadBy(List<String> readBy) { this.readBy = readBy; }
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
    public boolean isDeleted() { return deleted; }
    public void setDeleted(boolean deleted) { this.deleted = deleted; }
    public Map<String, String> getReactions() { return reactions; }
    public void setReactions(Map<String, String> reactions) { this.reactions = reactions; }
    public String getCaption() { return caption; }
    public void setCaption(String caption) { this.caption = caption; }
}