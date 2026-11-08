package com.group12.chatly.data.model;

import com.google.firebase.Timestamp;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Data Model for Chat Message mapped to Firestore sub-collection "chats/{chatId}/messages".
 */
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
    private boolean isDeleted = false;
    private Timestamp createdAt;

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
        if (senderId != null) {
            this.readBy.add(senderId);
        }
        this.createdAt = Timestamp.now();
        this.isDeleted = false;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getSenderAvatar() {
        return senderAvatar;
    }

    public void setSenderAvatar(String senderAvatar) {
        this.senderAvatar = senderAvatar;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Map<String, Object> getFileMetaData() {
        return fileMetaData;
    }

    public void setFileMetaData(Map<String, Object> fileMetaData) {
        this.fileMetaData = fileMetaData;
    }

    public List<String> getReadBy() {
        return readBy;
    }

    public void setReadBy(List<String> readBy) {
        this.readBy = readBy;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
}

