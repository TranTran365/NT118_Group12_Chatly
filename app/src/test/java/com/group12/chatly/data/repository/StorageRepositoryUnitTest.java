package com.group12.chatly.data.repository;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

public class StorageRepositoryUnitTest {

    @Mock
    private FirebaseStorage mockStorage;

    @Mock
    private StorageReference mockRootRef;

    @Mock
    private StorageReference mockChildRef;

    private StorageRepository storageRepository;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(mockStorage.getReference()).thenReturn(mockRootRef);
        when(mockRootRef.child(anyString())).thenReturn(mockChildRef);
        storageRepository = new StorageRepository(mockStorage);
    }

    @Test
    public void testInitialization() {
        assertNotNull(storageRepository);
        assertNotNull(storageRepository.getFirebaseStorage());
    }
}
