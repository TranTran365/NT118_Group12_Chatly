package com.group12.chatly.data.model;

import com.google.firebase.Timestamp;

import java.io.Serializable;

/**
 * Data Model for User, mapped to Firestore collection "users".
 */
public class User implements Serializable {

    private String userId;
    private String displayName;
    private String email;
    private String phoneNumber;
    private String avatarUrl;
    private String bio;
    private boolean isOnline;
    private Timestamp lastSeen;
    private String fcmToken;
    private Timestamp createdAt;

    /**
     * Required default public constructor for Firebase Firestore deserialization.
     */
    public User() {
    }

    public User(String userId, String displayName, String email) {
        this.userId = userId;
        this.displayName = displayName;
        this.email = email;
        this.phoneNumber = "";
        this.avatarUrl = "";
        this.bio = "Hey there! I am using Chatly.";
        this.isOnline = true;
        this.lastSeen = Timestamp.now();
        this.fcmToken = "";
        this.createdAt = Timestamp.now();
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public boolean isOnline() {
        return isOnline;
    }

    public void setOnline(boolean online) {
        isOnline = online;
    }

    public Timestamp getLastSeen() {
        return lastSeen;
    }

    public void setLastSeen(Timestamp lastSeen) {
        this.lastSeen = lastSeen;
    }

    public String getFcmToken() {
        return fcmToken;
    }

    public void setFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
}
