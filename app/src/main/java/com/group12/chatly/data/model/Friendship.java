package com.group12.chatly.data.model;

import com.google.firebase.Timestamp;

import java.io.Serializable;

/**
 * Data Model for Friendship / Contact relationship mapped to Firestore collection "friendships".
 */
public class Friendship implements Serializable {

    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_ACCEPTED = "ACCEPTED";
    public static final String STATUS_BLOCKED = "BLOCKED";

    private String friendshipId;
    private String requesterId;
    private String receiverId;
    private String status;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    /**
     * Required default public constructor for Firestore deserialization.
     */
    public Friendship() {
    }

    public Friendship(String friendshipId, String requesterId, String receiverId, String status) {
        this.friendshipId = friendshipId;
        this.requesterId = requesterId;
        this.receiverId = receiverId;
        this.status = status;
        this.createdAt = Timestamp.now();
        this.updatedAt = Timestamp.now();
    }

    public String getFriendshipId() {
        return friendshipId;
    }

    public void setFriendshipId(String friendshipId) {
        this.friendshipId = friendshipId;
    }

    public String getRequesterId() {
        return requesterId;
    }

    public void setRequesterId(String requesterId) {
        this.requesterId = requesterId;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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
