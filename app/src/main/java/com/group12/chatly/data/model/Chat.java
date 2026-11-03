package com.group12.chatly.data.model;

import com.google.firebase.Timestamp;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Data Model for Chat Room (Direct 1-1 or Group) mapped to Firestore collection "chats".
 */
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

    public Chat() {
    }

    public Chat(String chatId, String type, List<String> participants) {
        this.chatId = chatId;
        this.type = type;
        this.participants = participants != null ? participants : new ArrayList<>();
        this.name = "";
        this.avatarUrl = "";
        this.adminIds = new ArrayList<>();
        this.lastMessage = new HashMap<>();
        this.createdAt = Timestamp.now();
        this.updatedAt = Timestamp.now();
    }

    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public List<String> getParticipants() {
        return participants;
    }

    public void setParticipants(List<String> participants) {
        this.participants = participants;
    }

    public List<String> getAdminIds() {
        return adminIds;
    }

    public void setAdminIds(List<String> adminIds) {
        this.adminIds = adminIds;
    }

    public Map<String, Object> getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(Map<String, Object> lastMessage) {
        this.lastMessage = lastMessage;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }
}
