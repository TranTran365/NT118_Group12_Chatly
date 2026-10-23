package com.group12.chatly.data.repository;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class AuthRepositoryUnitTest {

    @Mock
    private FirebaseAuth mockAuth;

    @Mock
    private FirebaseFirestore mockFirestore;

    @Mock
    private FirebaseUser mockUser;

    private AuthRepository authRepository;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        authRepository = new AuthRepository(mockAuth, mockFirestore);
    }

    @Test
    public void testIsUserLoggedIn_whenUserExists_returnsTrue() {
        when(mockAuth.getCurrentUser()).thenReturn(mockUser);

        assertTrue(authRepository.isUserLoggedIn());
    }

    @Test
    public void testIsUserLoggedIn_whenNoUser_returnsFalse() {
        when(mockAuth.getCurrentUser()).thenReturn(null);

        assertFalse(authRepository.isUserLoggedIn());
    }

    @Test
    public void testGetCurrentUserId_whenLoggedIn_returnsUid() {
        when(mockAuth.getCurrentUser()).thenReturn(mockUser);
        when(mockUser.getUid()).thenReturn("user_abc_123");

        assertEquals("user_abc_123", authRepository.getCurrentUserId());
    }

    @Test
    public void testGetCurrentUserId_whenLoggedOut_returnsNull() {
        when(mockAuth.getCurrentUser()).thenReturn(null);

        assertNull(authRepository.getCurrentUserId());
    }

    @Test
    public void testLogout_invokesFirebaseAuthSignOut() {
        authRepository.logout();

        verify(mockAuth).signOut();
    }
}

