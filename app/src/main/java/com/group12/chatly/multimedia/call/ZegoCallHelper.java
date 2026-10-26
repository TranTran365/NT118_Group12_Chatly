package com.group12.chatly.multimedia.call;

import android.app.Application;
import android.util.Log;

import com.zegocloud.uikit.prebuilt.call.ZegoUIKitPrebuiltCallService;
import com.zegocloud.uikit.prebuilt.call.invite.ZegoUIKitPrebuiltCallInvitationConfig;
import com.zegocloud.uikit.prebuilt.call.invite.widget.ZegoSendCallInvitationButton;
import com.zegocloud.uikit.service.defines.ZegoUIKitUser;

import java.util.Collections;

/**
 * Helper class for initializing and managing ZegoCloud Audio and Video Call Kit.
 */
public final class ZegoCallHelper {

    private static final String TAG = "ZegoCallHelper";
    private static boolean isInitialized = false;

    private ZegoCallHelper() {
    }

    /**
     * Initializes ZegoUIKitPrebuiltCallService for the logged-in user.
     */
    public static void initZegoCallService(Application application, String userId, String userName) {
        if (application == null || userId == null || userName == null) {
            Log.w(TAG, "Cannot init ZegoCallService: invalid arguments");
            return;
        }

        try {
            ZegoUIKitPrebuiltCallInvitationConfig callInvitationConfig = new ZegoUIKitPrebuiltCallInvitationConfig();
            ZegoUIKitPrebuiltCallService.init(application, ZegoConfig.APP_ID, ZegoConfig.APP_SIGN, userId, userName, callInvitationConfig);
            isInitialized = true;
            Log.i(TAG, "ZegoCallService initialized successfully for user: " + userId);
        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize ZegoCallService: " + e.getMessage(), e);
        }
    }

    /**
     * De-initializes ZegoUIKitPrebuiltCallService on user logout.
     */
    public static void uninitZegoCallService() {
        try {
            if (isInitialized) {
                ZegoUIKitPrebuiltCallService.unInit();
                isInitialized = false;
                Log.i(TAG, "ZegoCallService uninitialized.");
            }
        } catch (Exception e) {
            Log.w(TAG, "Error uninitializing ZegoCallService: " + e.getMessage());
        }
    }

    /**
     * Configures a ZegoSendCallInvitationButton for 1-1 Audio Call.
     */
    public static void setupVoiceCallButton(ZegoSendCallInvitationButton button, String targetUserId, String targetUserName) {
        if (button == null) return;
        button.setIsVideoCall(false);
        button.setInvitees(Collections.singletonList(new ZegoUIKitUser(targetUserId, targetUserName)));
    }

    /**
     * Configures a ZegoSendCallInvitationButton for 1-1 Video Call.
     */
    public static void setupVideoCallButton(ZegoSendCallInvitationButton button, String targetUserId, String targetUserName) {
        if (button == null) return;
        button.setIsVideoCall(true);
        button.setInvitees(Collections.singletonList(new ZegoUIKitUser(targetUserId, targetUserName)));
    }

    public static boolean isInitialized() {
        return isInitialized;
    }
}

