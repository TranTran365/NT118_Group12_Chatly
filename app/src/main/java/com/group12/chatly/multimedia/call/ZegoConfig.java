package com.group12.chatly.multimedia.call;

/**
 * ZEGOCLOUD configuration parameters.
 * Supports static defaults and runtime configuration.
 */
public final class ZegoConfig {

    public static final long DEFAULT_APP_ID = 1234567890L;
    public static final String DEFAULT_APP_SIGN = "0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef";

    private static long currentAppId = DEFAULT_APP_ID;
    private static String currentAppSign = DEFAULT_APP_SIGN;

    private ZegoConfig() {
    }

    public static synchronized void setCredentials(long appId, String appSign) {
        if (appId > 0 && appSign != null && !appSign.trim().isEmpty()) {
            currentAppId = appId;
            currentAppSign = appSign.trim();
        }
    }

    public static synchronized long getAppId() {
        return currentAppId;
    }

    public static synchronized String getAppSign() {
        return currentAppSign;
    }

    public static synchronized boolean isConfigured() {
        return currentAppId != DEFAULT_APP_ID && currentAppSign != null && currentAppSign.length() == 64;
    }

    public static synchronized void resetToDefault() {
        currentAppId = DEFAULT_APP_ID;
        currentAppSign = DEFAULT_APP_SIGN;
    }
}
