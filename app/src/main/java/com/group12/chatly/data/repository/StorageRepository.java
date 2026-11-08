package com.group12.chatly.data.repository;

import android.net.Uri;

import androidx.annotation.NonNull;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Repository handling file uploads and downloads with Firebase Storage.
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
     * Uploads user avatar to /users/{userId}/avatar.jpg.
     *
     * @param userId   User UID
     * @param fileUri  Local image URI
     * @param callback Progress and result callback
     */
    public void uploadAvatar(@NonNull String userId, @NonNull Uri fileUri, @NonNull StorageCallback<String> callback) {
        String path = "users/" + userId + "/avatar.jpg";
        uploadFile(path, fileUri, callback);
    }

    /**
     * Uploads voice audio message (.m4a) to /chats/{chatId}/voices/{uniqueId}.m4a.
     */
    public void uploadVoiceMessage(@NonNull String chatId, @NonNull Uri fileUri, @NonNull StorageCallback<String> callback) {
        String fileName = "voice_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8) + ".m4a";
        String path = "chats/" + chatId + "/voices/" + fileName;
        uploadFile(path, fileUri, callback);
    }

    /**
     * Uploads media file (image, video, document) to /chats/{chatId}/{mediaType}/{fileName}.
     */
    public void uploadChatMedia(@NonNull String chatId,
                                @NonNull Uri fileUri,
                                @NonNull String mediaType,
                                @NonNull String originalFileName,
                                @NonNull StorageCallback<String> callback) {
        String sanitizedType = mediaType.toLowerCase().trim();
        String uniquePrefix = System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 6) + "_";
        String path = "chats/" + chatId + "/" + sanitizedType + "/" + uniquePrefix + originalFileName;
        uploadFile(path, fileUri, callback);
    }

    /**
     * General upload handler with percentage progress tracking.
     */
    public void uploadFile(@NonNull String storagePath, @NonNull Uri fileUri, @NonNull StorageCallback<String> callback) {
        final AtomicBoolean callbackDispatched = new AtomicBoolean(false);

        StorageReference ref = firebaseStorage.getReference().child(storagePath);
        UploadTask uploadTask = ref.putFile(fileUri);

        uploadTask.addOnProgressListener(snapshot -> {
            long totalBytes = snapshot.getTotalByteCount();
            if (totalBytes > 0) {
                int progress = (int) ((100.0 * snapshot.getBytesTransferred()) / totalBytes);
                callback.onProgress(progress);
            }
        });

        uploadTask.addOnSuccessListener(taskSnapshot -> {
            ref.getDownloadUrl()
                    .addOnSuccessListener(uri -> {
                        if (callbackDispatched.compareAndSet(false, true)) {
                            callback.onSuccess(uri.toString());
                        }
                    })
                    .addOnFailureListener(e -> {
                        if (callbackDispatched.compareAndSet(false, true)) {
                            callback.onFailure("Upload succeeded but failed to obtain download URL: " + e.getMessage());
                        }
                    });
        }).addOnFailureListener(e -> {
            if (callbackDispatched.compareAndSet(false, true)) {
                callback.onFailure("File upload failed: " + e.getMessage());
            }
        });
    }

    public FirebaseStorage getFirebaseStorage() {
        return firebaseStorage;
    }
}

