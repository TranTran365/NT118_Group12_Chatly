package com.group12.chatly.data.repository;

/**
 * Generic callback for Firebase Storage operations (upload/download).
 * Supports progress tracking.
 *
 * @param <T> Result payload type (typically download URL as String)
 */
public interface StorageCallback<T> {

    void onSuccess(T result);

    void onFailure(String errorMessage);

    default void onProgress(int progressPercent) {
        // Optional progress hook
    }
}

