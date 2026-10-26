package com.group12.chatly.viewmodel;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * State wrapper representing UI asynchronous state (Idle, Loading, Success, Error).
 *
 * @param <T> Payload data type
 */
public class AuthState<T> {

    public enum Status {
        IDLE,
        LOADING,
        SUCCESS,
        ERROR
    }

    private final Status status;
    private final T data;
    private final String errorMessage;

    private AuthState(@NonNull Status status, @Nullable T data, @Nullable String errorMessage) {
        this.status = status;
        this.data = data;
        this.errorMessage = errorMessage;
    }

    public static <T> AuthState<T> idle() {
        return new AuthState<>(Status.IDLE, null, null);
    }

    public static <T> AuthState<T> loading() {
        return new AuthState<>(Status.LOADING, null, null);
    }

    public static <T> AuthState<T> success(@Nullable T data) {
        return new AuthState<>(Status.SUCCESS, data, null);
    }

    public static <T> AuthState<T> error(@NonNull String errorMessage) {
        return new AuthState<>(Status.ERROR, null, errorMessage);
    }

    @NonNull
    public Status getStatus() {
        return status;
    }

    @Nullable
    public T getData() {
        return data;
    }

    @Nullable
    public String getErrorMessage() {
        return errorMessage;
    }

    public boolean isLoading() {
        return status == Status.LOADING;
    }

    public boolean isSuccess() {
        return status == Status.SUCCESS;
    }

    public boolean isError() {
        return status == Status.ERROR;
    }
}
