package com.group12.chatly.data.repository;

/**
 * Callback for Firebase Storage upload operations with progress tracking.
 *
 * @param <T> Result payload type (e.g. String downloadUrl)
 */
public interface StorageCallback<T> {
    void onProgress(int percentage);
    void onSuccess(T result);
    void onFailure(String errorMessage);
}
