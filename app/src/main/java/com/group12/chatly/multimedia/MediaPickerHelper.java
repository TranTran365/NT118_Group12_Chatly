package com.group12.chatly.multimedia;

import android.content.Context;
import android.net.Uri;

import androidx.activity.result.ActivityResultCaller;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Utility helper facilitating Android 13+ Photo Picker API (single & multiple)
 * as well as Camera capture intents.
 */
public class MediaPickerHelper {

    public interface SingleMediaCallback {
        void onMediaPicked(Uri uri);
    }

    public interface MultipleMediaCallback {
        void onMediaListPicked(List<Uri> uris);
    }

    public interface CameraCallback {
        void onPhotoCaptured(Uri photoUri, File photoFile);
    }

    /**
     * Registers a launcher for picking a single image (e.g. Avatar).
     * Works with both ComponentActivity and Fragment (implements ActivityResultCaller).
     */
    public static ActivityResultLauncher<PickVisualMediaRequest> registerSingleImagePicker(
            @NonNull ActivityResultCaller caller,
            @NonNull SingleMediaCallback callback) {
        return caller.registerForActivityResult(
                new ActivityResultContracts.PickVisualMedia(),
                uri -> {
                    if (uri != null) {
                        callback.onMediaPicked(uri);
                    }
                }
        );
    }

    /**
     * Launches the single image Photo Picker.
     */
    public static void launchSingleImagePicker(@NonNull ActivityResultLauncher<PickVisualMediaRequest> launcher) {
        launcher.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                .build());
    }

    /**
     * Registers a launcher for picking multiple images/videos (Media sharing).
     * Works with both ComponentActivity and Fragment (implements ActivityResultCaller).
     */
    public static ActivityResultLauncher<PickVisualMediaRequest> registerMultipleMediaPicker(
            @NonNull ActivityResultCaller caller,
            int maxItems,
            @NonNull MultipleMediaCallback callback) {
        return caller.registerForActivityResult(
                new ActivityResultContracts.PickMultipleVisualMedia(maxItems),
                uris -> {
                    if (uris != null && !uris.isEmpty()) {
                        callback.onMediaListPicked(uris);
                    }
                }
        );
    }

    /**
     * Launches the multiple visual media picker (images & videos).
     */
    public static void launchMultipleMediaPicker(@NonNull ActivityResultLauncher<PickVisualMediaRequest> launcher) {
        launcher.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageAndVideo.INSTANCE)
                .build());
    }

    /**
     * Prepares a temporary photo file and content Uri for capturing camera photos.
     */
    public static Uri createTempCameraImageUri(Context context) throws IOException {
        File cacheDir = new File(context.getCacheDir(), "camera");
        if (!cacheDir.exists()) {
            //noinspection ResultOfMethodCallIgnored
            cacheDir.mkdirs();
        }
        File photoFile = new File(cacheDir, "camera_" + System.currentTimeMillis() + ".jpg");
        return FileProvider.getUriForFile(
                context,
                context.getPackageName() + ".fileprovider",
                photoFile
        );
    }
}
