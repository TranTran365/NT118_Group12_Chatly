package com.group12.chatly.data.repository;

import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.assertEquals;

public class ChatRepositoryUnitTest {

    @Mock
    private FirebaseFirestore mockFirestore;

    @Mock
    private StorageRepository mockStorage;

    private ChatRepository chatRepository;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        chatRepository = new ChatRepository(mockFirestore, mockStorage);
    }

    @Test
    public void testGetDirectChatId_isDeterministic() {
        String id1 = ChatRepository.getDirectChatId("userA", "userB");
        String id2 = ChatRepository.getDirectChatId("userB", "userA");

        assertEquals("direct_userA_userB", id1);
        assertEquals("direct_userA_userB", id2);
    }
}
