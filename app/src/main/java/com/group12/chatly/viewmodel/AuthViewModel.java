package com.group12.chatly.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.group12.chatly.data.model.User;
import com.group12.chatly.data.repository.AuthCallback;
import com.group12.chatly.data.repository.AuthRepository;

/**
 * ViewModel managing user authentication flows (Registration, Login, Session status).
 */
public class AuthViewModel extends ViewModel {

    private final AuthRepository authRepository;

    private final MutableLiveData<AuthState<User>> registerState = new MutableLiveData<>(AuthState.idle());
    private final MutableLiveData<AuthState<User>> loginState = new MutableLiveData<>(AuthState.idle());

    public AuthViewModel() {
        this(AuthRepository.getInstance());
    }

    public AuthViewModel(@NonNull AuthRepository authRepository) {
        this.authRepository = authRepository;
    }

    public LiveData<AuthState<User>> getRegisterState() {
        return registerState;
    }

    public LiveData<AuthState<User>> getLoginState() {
        return loginState;
    }

    public boolean isUserLoggedIn() {
        return authRepository.isUserLoggedIn();
    }

    /**
     * Executes registration with full name, email, and password.
     */
    public void register(@NonNull String fullName, @NonNull String email, @NonNull String password) {
        registerState.setValue(AuthState.loading());
        authRepository.registerWithEmail(fullName, email, password, new AuthCallback<User>() {
            @Override
            public void onSuccess(User result) {
                registerState.setValue(AuthState.success(result));
            }

            @Override
            public void onFailure(String errorMessage) {
                registerState.setValue(AuthState.error(errorMessage));
            }
        });
    }

    /**
     * Executes login with email and password.
     */
    public void login(@NonNull String email, @NonNull String password) {
        loginState.setValue(AuthState.loading());
        authRepository.loginWithEmail(email, password, new AuthCallback<User>() {
            @Override
            public void onSuccess(User result) {
                loginState.setValue(AuthState.success(result));
            }

            @Override
            public void onFailure(String errorMessage) {
                loginState.setValue(AuthState.error(errorMessage));
            }
        });
    }

    public void resetStates() {
        registerState.setValue(AuthState.idle());
        loginState.setValue(AuthState.idle());
    }
}
