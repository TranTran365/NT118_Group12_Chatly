package com.group12.chatly.data.model;

import com.google.firebase.firestore.Exclude;

import java.io.Serializable;

public class Contact implements Serializable {

    private String userId;
    private String displayName;
    private String statusText;
    private boolean isOnline;
    private String avatarUrl;
    private String groupLetter;
    private boolean showGroupHeader;

    public Contact() {
    }

    public Contact(String userId, String displayName, String statusText, boolean isOnline, String avatarUrl, String groupLetter, boolean showGroupHeader) {
        this.userId = userId;
        this.displayName = displayName;
        this.statusText = statusText;
        this.isOnline = isOnline;
        this.avatarUrl = avatarUrl;
        this.groupLetter = groupLetter;
        this.showGroupHeader = showGroupHeader;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getStatusText() {
        return statusText;
    }

    public void setStatusText(String statusText) {
        this.statusText = statusText;
    }

    public boolean isOnline() {
        return isOnline;
    }

    public void setOnline(boolean online) {
        isOnline = online;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getGroupLetter() {
        return groupLetter;
    }

    public void setGroupLetter(String groupLetter) {
        this.groupLetter = groupLetter;
    }

    public boolean isShowGroupHeader() {
        return showGroupHeader;
    }

    public void setShowGroupHeader(boolean showGroupHeader) {
        this.showGroupHeader = showGroupHeader;
    }

    @Exclude
    public String getInitials() {
        if (displayName == null || displayName.trim().isEmpty()) {
            return "?";
        }
        String[] parts = displayName.trim().split("\\s+");
        if (parts.length == 1) {
            return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
        }
        return (parts[0].substring(0, 1) + parts[parts.length - 1].substring(0, 1)).toUpperCase();
    }
}
