package com.group12.chatly.multimedia.call;

/**
 * ZEGOCLOUD configuration parameters.
 * Obtain your AppID and AppSign from the ZEGOCLOUD Admin Console:
 * https://console.zegocloud.com
 */
public final class ZegoConfig {

    private ZegoConfig() {
    }

    /**
     * Replace with your valid AppID from ZEGOCLOUD Console.
     */
    public static final long APP_ID = 1234567890L;

    /**
     * Replace with your valid 64-character AppSign from ZEGOCLOUD Console.
     */
    public static final String APP_SIGN = "0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef";

    public static boolean isConfigured() {
        return APP_ID != 1234567890L && APP_SIGN.length() == 64;
    }
}

