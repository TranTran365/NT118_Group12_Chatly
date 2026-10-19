package com.group12.chatly.multimedia;

import android.content.Context;
import android.util.Log;

import androidx.emoji2.text.EmojiCompat;
import androidx.emoji2.text.DefaultEmojiCompatConfig;

/**
 * Helper class for EmojiCompat initialization and emoji chat utilities.
 */
public final class EmojiHelper {

    private static final String TAG = "EmojiHelper";

    private EmojiHelper() {
    }

    /**
     * Initializes EmojiCompat using Google Play Services font provider if available.
     */
    public static void initEmojiCompat(Context context) {
        if (context == null) return;
        try {
            EmojiCompat.Config config = DefaultEmojiCompatConfig.create(context);
            if (config != null) {
                EmojiCompat.init(config);
                Log.d(TAG, "EmojiCompat initialized with DefaultEmojiCompatConfig");
            }
        } catch (Exception e) {
            Log.w(TAG, "Could not initialize EmojiCompat: " + e.getMessage());
        }
    }

    /**
     * Common chat reaction emojis.
     */
    public static final String[] QUICK_REACTIONS = {
            "👍", "❤️", "😂", "😮", "😢", "🔥", "🎉", "👏"
    };

    /**
     * Checks if a string contains emoji characters.
     */
    public static boolean containsEmoji(CharSequence sequence) {
        if (sequence == null) return false;
        try {
            if (EmojiCompat.isConfigured()) {
                EmojiCompat compat = EmojiCompat.get();
                return compat.hasEmojiGlyph(sequence);
            }
        } catch (Exception ignored) {
        }
        return false;
    }
}

