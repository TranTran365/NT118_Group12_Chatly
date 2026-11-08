package com.group12.chatly.viewmodel;

import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.group12.chatly.data.model.User;
import com.group12.chatly.data.repository.AuthCallback;
import com.group12.chatly.data.repository.AuthRepository;
import com.group12.chatly.data.repository.StorageCallback;
import com.group12.chatly.data.repository.StorageRepository;

/**
 * ViewModel managing user profile retrieval, updates, avatar upload, and session termination.
 */
public class ProfileViewModel extends ViewModel {

    private final AuthRepository authRepository;
    private final StorageRepository storageRepository;

    private final MutableLiveData<AuthState<User>> userProfileState = new MutableLiveData<>(AuthState.idle());
    private final MutableLiveData<AuthState<String>> avatarUploadState = new MutableLiveData<>(AuthState.idle());
    private final MutableLiveData<Integer> avatarUploadProgress = new MutableLiveData<>(0);
    private final MutableLiveData<AuthState<Void>> profileUpdateState = new MutableLiveData<>(AuthState.idle());

    public ProfileViewModel() {
        this(AuthRepository.getInstance(), StorageRepository.getInstance());
    }

    public ProfileViewModel(@NonNull AuthRepository authRepository) {
        this(authRepository, StorageRepository.getInstance());
    }

    public ProfileViewModel(@NonNull AuthRepository authRepository, @NonNull StorageRepository storageRepository) {
        this.authRepository = authRepository;
        this.storageRepository = storageRepository;
    }

    public LiveData<AuthState<User>> getUserProfileState() {
        return userProfileState;
    }

    public LiveData<AuthState<String>> getAvatarUploadState() {
        return avatarUploadState;
    }

    public LiveData<Integer> getAvatarUploadProgress() {
        return avatarUploadProgress;
    }

    public LiveData<AuthState<Void>> getProfileUpdateState() {
        return profileUpdateState;
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
     * Uploads avatar image to Storage and updates Firestore profile avatarUrl.
     */
    public void uploadAvatar(@NonNull Uri imageUri) {
        String uid = authRepository.getCurrentUserId();
        if (uid == null) {
            avatarUploadState.setValue(AuthState.error("User not authenticated"));
            return;
        }

        avatarUploadState.setValue(AuthState.loading());
        avatarUploadProgress.setValue(0);

        storageRepository.uploadAvatar(uid, imageUri, new StorageCallback<String>() {
            @Override
            public void onProgress(int percentage) {
                avatarUploadProgress.setValue(percentage);
            }

            @Override
            public void onSuccess(String downloadUrl) {
                avatarUploadState.setValue(AuthState.success(downloadUrl));
                // Update Firestore user document with new avatar URL
                authRepository.updateUserProfile(null, null, downloadUrl, new AuthCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        loadUserProfile();
                    }

                    @Override
                    public void onFailure(String errorMessage) {
                        // Avatar uploaded but failed to update Firestore profile document
                    }
                });
            }

            @Override
            public void onFailure(String errorMessage) {
                avatarUploadState.setValue(AuthState.error(errorMessage));
            }
        });
    }

    /**
     * Updates user profile info (name, bio).
     */
    public void updateProfile(@Nullable String displayName, @Nullable String bio) {
        profileUpdateState.setValue(AuthState.loading());
        authRepository.updateUserProfile(displayName, bio, null, new AuthCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                profileUpdateState.setValue(AuthState.success(null));
                loadUserProfile();
            }

            @Override
            public void onFailure(String errorMessage) {
                profileUpdateState.setValue(AuthState.error(errorMessage));
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
