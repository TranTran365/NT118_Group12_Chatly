package com.group12.chatly.data.repository;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

public class ContactRepositoryUnitTest {

    @Mock
    private FirebaseFirestore mockFirestore;

    @Mock
    private CollectionReference mockCollection;

    private ContactRepository contactRepository;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(mockFirestore.collection(anyString())).thenReturn(mockCollection);
        contactRepository = new ContactRepository(mockFirestore);
    }

    @Test
    public void testGetCompositeFriendshipId() {
        String id1 = ContactRepository.getCompositeFriendshipId("alice", "bob");
        String id2 = ContactRepository.getCompositeFriendshipId("bob", "alice");

        assertEquals("alice_bob", id1);
        assertEquals("alice_bob", id2);
    }
}
