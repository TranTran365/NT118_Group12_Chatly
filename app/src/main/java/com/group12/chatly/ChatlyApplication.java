package com.group12.chatly;

import android.app.Application;
import android.util.Log;

import com.group12.chatly.multimedia.call.ZegoCallHelper;
import com.group12.chatly.utils.FileUtils;

/**
 * Chatly Application class.
 * Central entry point for initializing third-party SDKs (e.g., ZegoCloud Call UIKit)
 * and managing application-level resources and cache cleanup.
 */
public class ChatlyApplication extends Application {

    private static final String TAG = "ChatlyApplication";
    private static ChatlyApplication instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        // Initialize EmojiCompat
        com.group12.chatly.multimedia.EmojiHelper.initEmojiCompat(this);

        // Perform initial temporary cache cleanup on app launch
        try {
            FileUtils.cleanupOldTempFiles(this);
        } catch (Exception e) {
            Log.w(TAG, "Cache cleanup error on startup: " + e.getMessage());
        }
    }

    public static ChatlyApplication getInstance() {
        return instance;
    }

    /**
     * Initializes ZegoCloud call service for the authenticated user.
     *
     * @param userId   Current user's unique ID
     * @param userName Current user's display name
     */
    public void initCallService(String userId, String userName) {
        ZegoCallHelper.initZegoCallService(this, userId, userName);
    }

    /**
     * Uninitializes ZegoCloud call service on logout.
     */
    public void uninitCallService() {
        ZegoCallHelper.uninitZegoCallService();
    }
}

