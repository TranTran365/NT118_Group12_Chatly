package com.group12.chatly.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.group12.chatly.data.model.User;
import com.group12.chatly.data.repository.AuthCallback;
import com.group12.chatly.data.repository.AuthRepository;

/**
 * ViewModel managing user profile retrieval and session termination.
 */
public class ProfileViewModel extends ViewModel {

    private final AuthRepository authRepository;
    private final MutableLiveData<AuthState<User>> userProfileState = new MutableLiveData<>(AuthState.idle());

    public ProfileViewModel() {
        this(AuthRepository.getInstance());
    }

    public ProfileViewModel(@NonNull AuthRepository authRepository) {
        this.authRepository = authRepository;
    }

    public LiveData<AuthState<User>> getUserProfileState() {
        return userProfileState;
    }

    /**
     * Loads the authenticated user profile from repository.
     */
    public void loadUserProfile() {
        userProfileState.setValue(AuthState.loading());
        authRepository.getCurrentUserProfile(new AuthCallback<User>() {
            @Override
            public void onSuccess(User result) {
                userProfileState.setValue(AuthState.success(result));
            }

            @Override
            public void onFailure(String errorMessage) {
                userProfileState.setValue(AuthState.error(errorMessage));
            }
        });
    }

    /**
     * Signs out the current user session.
     */
    public void logout() {
        authRepository.logout();
        userProfileState.setValue(AuthState.idle());
    }
}
