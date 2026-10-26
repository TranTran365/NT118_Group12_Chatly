package com.group12.chatly.data.repository;

import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Repository responsible for uploading and managing files in Firebase Storage.
 */
public class StorageRepository {

    private static volatile StorageRepository instance;
    private final FirebaseStorage firebaseStorage;

    public StorageRepository(@NonNull FirebaseStorage firebaseStorage) {
        this.firebaseStorage = firebaseStorage;
    }

    public static StorageRepository getInstance() {
        if (instance == null) {
            synchronized (StorageRepository.class) {
                if (instance == null) {
                    instance = new StorageRepository(FirebaseStorage.getInstance());
                }
            }
        }
        return instance;
    }

    /**
     * Uploads user avatar image to Firebase Storage at `/users/{userId}/avatar.jpg`.
     *
     * @param userId   User ID
     * @param file     Compressed avatar file (JPEG)
     * @param callback Callback delivering the public download URL on success
     */
    public void uploadAvatar(@NonNull String userId,
                             @NonNull File file,
                             @NonNull StorageCallback<String> callback) {
        String path = "users/" + userId + "/avatar.jpg";
        uploadFile(path, file, "image/jpeg", callback);
    }

    /**
     * Uploads a file to a specific Firebase Storage path with optional MIME type and progress tracking.
     *
     * @param storagePath Target path in Firebase Storage
     * @param file        Local file to upload
     * @param mimeType    MIME type (e.g., "image/jpeg", "audio/mp4")
     * @param callback    Callback for progress, success (download URL), and error
     */
    public void uploadFile(@NonNull String storagePath,
                           @NonNull File file,
                           @Nullable String mimeType,
                           @NonNull StorageCallback<String> callback) {
        final AtomicBoolean callbackDispatched = new AtomicBoolean(false);

        if (!file.exists()) {
            callback.onFailure("File does not exist: " + file.getAbsolutePath());
            return;
        }

        StorageReference fileRef = firebaseStorage.getReference().child(storagePath);

        StorageMetadata.Builder metaBuilder = new StorageMetadata.Builder();
        if (mimeType != null && !mimeType.trim().isEmpty()) {
            metaBuilder.setContentType(mimeType);
        }

        Uri fileUri = Uri.fromFile(file);
        UploadTask uploadTask = fileRef.putFile(fileUri, metaBuilder.build());

        uploadTask.addOnProgressListener(snapshot -> {
            long total = snapshot.getTotalByteCount();
            if (total > 0) {
                int progress = (int) ((snapshot.getBytesTransferred() * 100) / total);
                callback.onProgress(progress);
            }
        }).addOnSuccessListener(taskSnapshot -> {
            fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                if (callbackDispatched.compareAndSet(false, true)) {
                    callback.onSuccess(uri.toString());
                }
            }).addOnFailureListener(e -> {
                String errorMsg = e.getMessage() != null
                        ? "Failed to retrieve download URL: " + e.getMessage()
                        : "Failed to retrieve download URL.";
                if (callbackDispatched.compareAndSet(false, true)) {
                    callback.onFailure(errorMsg);
                }
            });
        }).addOnFailureListener(e -> {
            String errorMsg = e.getMessage() != null
                    ? "Upload failed: " + e.getMessage()
                    : "Upload to Firebase Storage failed.";
            if (callbackDispatched.compareAndSet(false, true)) {
                callback.onFailure(errorMsg);
            }
        });
    }
}

