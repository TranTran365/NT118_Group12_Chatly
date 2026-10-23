package com.group12.chatly.data.repository;

/**
 * Generic callback interface for asynchronous authentication operations.
 *
 * @param <T> Result payload type
 */
public interface AuthCallback<T> {
    void onSuccess(T result);
    void onFailure(String errorMessage);
}
