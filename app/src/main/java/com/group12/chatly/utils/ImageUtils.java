package com.group12.chatly.utils;

import android.content.Context;
import android.net.Uri;

import java.io.File;

/**
 * Convenience wrapper for Image operations as outlined in Member 3 Plan.
 */
public final class ImageUtils {

    private ImageUtils() {
    }

    public static File compressImage(Context context, Uri imageUri) {
        return ImageCompressor.compressImage(context, imageUri);
    }

    public static File compressImage(Context context, Uri imageUri, int maxWidth, int maxHeight, int quality) {
        return ImageCompressor.compressImage(context, imageUri, maxWidth, maxHeight, quality);
    }

    public static File compressAvatar(Context context, Uri imageUri) {
        return ImageCompressor.compressAvatar(context, imageUri);
    }
}

