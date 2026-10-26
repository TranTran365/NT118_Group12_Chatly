package com.group12.chatly.viewmodel;

import androidx.annotation.NonNull;
import androidx.arch.core.executor.ArchTaskExecutor;
import androidx.arch.core.executor.TaskExecutor;

import com.group12.chatly.data.repository.AuthCallback;
import com.group12.chatly.data.repository.AuthRepository;
import com.group12.chatly.data.repository.StorageCallback;
import com.group12.chatly.data.repository.StorageRepository;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ProfileViewModelUnitTest {

    @Mock
    private AuthRepository mockAuthRepository;

    @Mock
    private StorageRepository mockStorageRepository;

    private ProfileViewModel profileViewModel;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        ArchTaskExecutor.getInstance().setDelegate(new TaskExecutor() {
            @Override
            public void executeOnDiskIO(@NonNull Runnable runnable) {
                runnable.run();
            }

            @Override
            public void postToMainThread(@NonNull Runnable runnable) {
                runnable.run();
            }

            @Override
            public boolean isMainThread() {
                return true;
            }
        });
        profileViewModel = new ProfileViewModel(mockAuthRepository, mockStorageRepository);
    }

    @After
    public void tearDown() {
        ArchTaskExecutor.getInstance().setDelegate(null);
    }

    @Test
    public void testUploadAvatar_whenUserNotLoggedIn_setsErrorState() {
        when(mockAuthRepository.getCurrentUserId()).thenReturn(null);

        File fakeFile = new File("fake_avatar.jpg");
        profileViewModel.uploadAvatar(fakeFile);

        assertNotNull(profileViewModel.getAvatarUploadState().getValue());
        assertTrue(profileViewModel.getAvatarUploadState().getValue().isError());
        assertEquals("User is not authenticated.", profileViewModel.getAvatarUploadState().getValue().getErrorMessage());
    }

    @Test
    public void testUploadAvatar_whenStorageSucceedsAndAuthSucceeds_updatesState() {
        when(mockAuthRepository.getCurrentUserId()).thenReturn("user_123");

        File fakeFile = new File("fake_avatar.jpg");
        profileViewModel.uploadAvatar(fakeFile);

        // Verify storage upload called
        @SuppressWarnings("unchecked")
        ArgumentCaptor<StorageCallback<String>> storageCaptor = ArgumentCaptor.forClass(StorageCallback.class);
        verify(mockStorageRepository).uploadAvatar(eq("user_123"), eq(fakeFile), storageCaptor.capture());

        // Simulate storage success
        String downloadUrl = "https://firebasestorage.googleapis.com/avatar.jpg";
        storageCaptor.getValue().onSuccess(downloadUrl);

        // Verify auth update called
        @SuppressWarnings("unchecked")
        ArgumentCaptor<AuthCallback<String>> authCaptor = ArgumentCaptor.forClass(AuthCallback.class);
        verify(mockAuthRepository).updateAvatarUrl(eq(downloadUrl), authCaptor.capture());

        // Simulate auth update success
        authCaptor.getValue().onSuccess(downloadUrl);

        assertTrue(profileViewModel.getAvatarUploadState().getValue().isSuccess());
        assertEquals(downloadUrl, profileViewModel.getAvatarUploadState().getValue().getData());
    }

    @Test
    public void testUploadAvatar_whenStorageFails_setsErrorState() {
        when(mockAuthRepository.getCurrentUserId()).thenReturn("user_123");

        File fakeFile = new File("fake_avatar.jpg");
        profileViewModel.uploadAvatar(fakeFile);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<StorageCallback<String>> storageCaptor = ArgumentCaptor.forClass(StorageCallback.class);
        verify(mockStorageRepository).uploadAvatar(eq("user_123"), eq(fakeFile), storageCaptor.capture());

        storageCaptor.getValue().onFailure("Storage network error");

        assertTrue(profileViewModel.getAvatarUploadState().getValue().isError());
        assertEquals("Storage network error", profileViewModel.getAvatarUploadState().getValue().getErrorMessage());
    }

    @Test
    public void testLogout_resetsStateAndCallsAuthLogout() {
        profileViewModel.logout();

        verify(mockAuthRepository).logout();
        assertEquals(AuthState.Status.IDLE, profileViewModel.getUserProfileState().getValue().getStatus());
        assertEquals(AuthState.Status.IDLE, profileViewModel.getAvatarUploadState().getValue().getStatus());
    }
}
