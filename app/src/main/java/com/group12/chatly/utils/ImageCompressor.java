package com.group12.chatly.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.util.Log;

import androidx.exifinterface.media.ExifInterface;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

/**
 * Utility for resizing, rotating, and compressing images before uploading
 * to Firebase Storage to optimize bandwidth and storage quota.
 */
public final class ImageCompressor {

    private static final String TAG = "ImageCompressor";
    public static final int DEFAULT_MAX_WIDTH = 1280;
    public static final int DEFAULT_MAX_HEIGHT = 1280;
    public static final int AVATAR_MAX_WIDTH = 512;
    public static final int AVATAR_MAX_HEIGHT = 512;
    public static final int DEFAULT_QUALITY = 80;

    private ImageCompressor() {
        // Private constructor
    }

    /**
     * Compresses an image from a Uri and saves it to a cache JPEG file.
     *
     * @param context    Android Context
     * @param imageUri   Original image Uri
     * @param maxWidth   Maximum allowed width
     * @param maxHeight  Maximum allowed height
     * @param quality    JPEG compression quality (1-100, recommended 75-80)
     * @return File containing compressed image, or null on failure
     */
    public static File compressImage(Context context, Uri imageUri, int maxWidth, int maxHeight, int quality) {
        if (context == null || imageUri == null) return null;

        try {
            // Step 1: Decode image dimensions without loading entire bitmap into memory
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            try (InputStream in = context.getContentResolver().openInputStream(imageUri)) {
                if (in == null) return null;
                BitmapFactory.decodeStream(in, null, options);
            }

            int actualWidth = options.outWidth;
            int actualHeight = options.outHeight;
            if (actualWidth <= 0 || actualHeight <= 0) return null;

            // Step 2: Calculate inSampleSize to downsample efficiently
            options.inSampleSize = calculateInSampleSize(actualWidth, actualHeight, maxWidth, maxHeight);
            options.inJustDecodeBounds = false;
            options.inPreferredConfig = Bitmap.Config.RGB_565; // Saves 50% memory compared to ARGB_8888

            Bitmap decodedBitmap;
            try (InputStream in = context.getContentResolver().openInputStream(imageUri)) {
                if (in == null) return null;
                decodedBitmap = BitmapFactory.decodeStream(in, null, options);
            }

            if (decodedBitmap == null) return null;

            // Step 3: Handle EXIF orientation rotation
            int rotation = getExifRotation(context, imageUri);
            Bitmap finalBitmap = decodedBitmap;
            if (rotation != 0) {
                Matrix matrix = new Matrix();
                matrix.postRotate(rotation);
                Bitmap rotated = Bitmap.createBitmap(decodedBitmap, 0, 0, decodedBitmap.getWidth(), decodedBitmap.getHeight(), matrix, true);
                if (rotated != decodedBitmap) {
                    decodedBitmap.recycle();
                    finalBitmap = rotated;
                }
            }

            // Step 4: Scale down if still slightly larger than target bounds
            if (finalBitmap.getWidth() > maxWidth || finalBitmap.getHeight() > maxHeight) {
                float ratio = Math.min((float) maxWidth / finalBitmap.getWidth(), (float) maxHeight / finalBitmap.getHeight());
                int targetW = Math.round(ratio * finalBitmap.getWidth());
                int targetH = Math.round(ratio * finalBitmap.getHeight());
                Bitmap scaled = Bitmap.createScaledBitmap(finalBitmap, targetW, targetH, true);
                if (scaled != finalBitmap) {
                    finalBitmap.recycle();
                    finalBitmap = scaled;
                }
            }

            // Step 5: Compress to JPEG file in cache directory
            File outputFile = new File(context.getCacheDir(), "compressed_" + System.currentTimeMillis() + ".jpg");
            try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                finalBitmap.compress(Bitmap.CompressFormat.JPEG, quality, fos);
                fos.flush();
            } finally {
                finalBitmap.recycle();
            }

            return outputFile;
        } catch (Exception e) {
            Log.e(TAG, "Error compressing image: " + e.getMessage(), e);
            return null;
        }
    }

    /**
     * Helper to compress image with default dimensions and quality.
     */
    public static File compressImage(Context context, Uri imageUri) {
        return compressImage(context, imageUri, DEFAULT_MAX_WIDTH, DEFAULT_MAX_HEIGHT, DEFAULT_QUALITY);
    }

    /**
     * Compresses image specifically tailored for User Avatar (512x512, 85% quality).
     */
    public static File compressAvatar(Context context, Uri imageUri) {
        return compressImage(context, imageUri, AVATAR_MAX_WIDTH, AVATAR_MAX_HEIGHT, 85);
    }

    /**
     * Calculates the inSampleSize power of 2 for decoding.
     */
    public static int calculateInSampleSize(int actualWidth, int actualHeight, int reqWidth, int reqHeight) {
        int inSampleSize = 1;
        if (actualHeight > reqHeight || actualWidth > reqWidth) {
            final int halfHeight = actualHeight / 2;
            final int halfWidth = actualWidth / 2;
            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        return Math.max(1, inSampleSize);
    }

    /**
     * Reads Exif orientation and returns rotation degrees.
     */
    private static int getExifRotation(Context context, Uri uri) {
        try (InputStream in = context.getContentResolver().openInputStream(uri)) {
            if (in == null) return 0;
            ExifInterface exifInterface = new ExifInterface(in);
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    return 90;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    return 180;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    return 270;
                default:
                    return 0;
            }
        } catch (Exception e) {
            Log.w(TAG, "Could not get EXIF rotation: " + e.getMessage());
            return 0;
        }
    }
}

