package com.group12.chatly.data.repository;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.group12.chatly.multimedia.call.ZegoCallHelper;
import com.zegocloud.uikit.prebuilt.call.invite.widget.ZegoSendCallInvitationButton;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Repository managing RTC Audio/Video calls and Firestore call rooms.
 */
public class CallRepository {

    public static final String COLLECTION_CALL_ROOMS = "call_rooms";

    public static final String CALL_TYPE_VOICE = "VOICE";
    public static final String CALL_TYPE_VIDEO = "VIDEO";

    public static final String STATUS_RINGING = "RINGING";
    public static final String STATUS_CONNECTED = "CONNECTED";
    public static final String STATUS_ENDED = "ENDED";
    public static final String STATUS_REJECTED = "REJECTED";

    private static volatile CallRepository instance;
    private final FirebaseFirestore firestore;

    public CallRepository(@NonNull FirebaseFirestore firestore) {
        this.firestore = firestore;
    }

    public static CallRepository getInstance() {
        if (instance == null) {
            synchronized (CallRepository.class) {
                if (instance == null) {
                    instance = new CallRepository(FirebaseFirestore.getInstance());
                }
            }
        }
        return instance;
    }

    /**
     * Initializes Zego Call Kit service.
     */
    public void initCallService(@NonNull Application application, @NonNull String userId, @NonNull String userName) {
        ZegoCallHelper.initZegoCallService(application, userId, userName);
    }

    /**
     * Uninitializes Zego Call Kit service.
     */
    public void uninitCallService() {
        ZegoCallHelper.uninitZegoCallService();
    }

    public boolean isCallServiceInitialized() {
        return ZegoCallHelper.isInitialized();
    }

    /**
     * Sets up voice call invitation button.
     */
    public void setupVoiceCallButton(@NonNull ZegoSendCallInvitationButton button, @NonNull String targetUserId, @NonNull String targetUserName) {
        ZegoCallHelper.setupVoiceCallButton(button, targetUserId, targetUserName);
    }

    /**
     * Sets up video call invitation button.
     */
    public void setupVideoCallButton(@NonNull ZegoSendCallInvitationButton button, @NonNull String targetUserId, @NonNull String targetUserName) {
        ZegoCallHelper.setupVideoCallButton(button, targetUserId, targetUserName);
    }

    /**
     * Creates a call room record in Firestore to track call status and logs.
     */
    public void createCallRoom(@NonNull String callerId,
                               @NonNull String callerName,
                               @NonNull String receiverId,
                               @NonNull String receiverName,
                               @NonNull String callType,
                               @NonNull AuthCallback<String> callback) {
        String callRoomId = "call_" + UUID.randomUUID().toString();
        Map<String, Object> room = new HashMap<>();
        room.put("callRoomId", callRoomId);
        room.put("callerId", callerId);
        room.put("callerName", callerName);
        room.put("receiverId", receiverId);
        room.put("receiverName", receiverName);
        room.put("callType", callType);
        room.put("callStatus", STATUS_RINGING);
        room.put("createdAt", Timestamp.now());
        room.put("updatedAt", Timestamp.now());

        firestore.collection(COLLECTION_CALL_ROOMS)
                .document(callRoomId)
                .set(room)
                .addOnSuccessListener(aVoid -> callback.onSuccess(callRoomId))
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    /**
     * Updates call status (RINGING, CONNECTED, ENDED, REJECTED).
     */
    public void updateCallStatus(@NonNull String callRoomId, @NonNull String status, @Nullable AuthCallback<Void> callback) {
        firestore.collection(COLLECTION_CALL_ROOMS)
                .document(callRoomId)
                .update("callStatus", status, "updatedAt", Timestamp.now())
                .addOnSuccessListener(aVoid -> {
                    if (callback != null) callback.onSuccess(null);
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onFailure(e.getMessage());
                });
    }
}

