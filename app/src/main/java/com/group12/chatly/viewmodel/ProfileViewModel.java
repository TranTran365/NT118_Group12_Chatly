package com.group12.chatly.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.group12.chatly.data.model.User;
import com.group12.chatly.data.repository.AuthCallback;
import com.group12.chatly.data.repository.AuthRepository;
import com.group12.chatly.data.repository.StorageCallback;
import com.group12.chatly.data.repository.StorageRepository;
import com.group12.chatly.utils.FileUtils;

import java.io.File;

/**
 * ViewModel managing user profile retrieval, avatar upload, and session termination.
 */
public class ProfileViewModel extends ViewModel {

    private final AuthRepository authRepository;
    private final StorageRepository storageRepository;

    private final MutableLiveData<AuthState<User>> userProfileState = new MutableLiveData<>(AuthState.idle());
    private final MutableLiveData<AuthState<String>> avatarUploadState = new MutableLiveData<>(AuthState.idle());

    public ProfileViewModel() {
        this(AuthRepository.getInstance(), StorageRepository.getInstance());
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
     * Uploads a compressed avatar image to Firebase Storage and updates user profile in Firestore.
     * Automatically cleans up the local temporary compressed file.
     *
     * @param compressedFile Local compressed image file
     */
    public void uploadAvatar(@NonNull File compressedFile) {
        String userId = authRepository.getCurrentUserId();
        if (userId == null) {
            FileUtils.deleteFile(compressedFile.getAbsolutePath());
            avatarUploadState.setValue(AuthState.error("User is not authenticated."));
            return;
        }

        avatarUploadState.setValue(AuthState.loading());

        storageRepository.uploadAvatar(userId, compressedFile, new StorageCallback<String>() {
            @Override
            public void onSuccess(String downloadUrl) {
                // Delete local temp compressed file after upload
                FileUtils.deleteFile(compressedFile.getAbsolutePath());

                // Update Firestore and Firebase Auth photo profile
                authRepository.updateAvatarUrl(downloadUrl, new AuthCallback<String>() {
                    @Override
                    public void onSuccess(String updatedUrl) {
                        if (userProfileState.getValue() != null && userProfileState.getValue().getData() != null) {
                            User currentUser = userProfileState.getValue().getData();
                            currentUser.setAvatarUrl(updatedUrl);
                            userProfileState.setValue(AuthState.success(currentUser));
                        } else {
                            loadUserProfile();
                        }
                        avatarUploadState.setValue(AuthState.success(updatedUrl));
                    }

                    @Override
                    public void onFailure(String errorMessage) {
                        avatarUploadState.setValue(AuthState.error(errorMessage));
                    }
                });
            }

            @Override
            public void onFailure(String errorMessage) {
                FileUtils.deleteFile(compressedFile.getAbsolutePath());
                avatarUploadState.setValue(AuthState.error(errorMessage));
            }
        });
    }

    /**
     * Signs out the current user session.
     */
    public void logout() {
        authRepository.logout();
        userProfileState.setValue(AuthState.idle());
        avatarUploadState.setValue(AuthState.idle());
    }
}
