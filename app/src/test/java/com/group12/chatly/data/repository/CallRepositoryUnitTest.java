package com.group12.chatly.data.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.group12.chatly.multimedia.call.ZegoConfig;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class CallRepositoryUnitTest {

    @Mock
    private FirebaseFirestore mockFirestore;
    @Mock
    private CollectionReference mockCollectionReference;
    @Mock
    private DocumentReference mockDocumentReference;
    @Mock
    private Task<Void> mockVoidTask;

    @Captor
    private ArgumentCaptor<OnSuccessListener<Void>> successCaptor;
    @Captor
    private ArgumentCaptor<OnFailureListener> failureCaptor;

    private CallRepository callRepository;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        callRepository = new CallRepository(mockFirestore);

        when(mockFirestore.collection(CallRepository.COLLECTION_CALL_ROOMS)).thenReturn(mockCollectionReference);
        when(mockCollectionReference.document(anyString())).thenReturn(mockDocumentReference);
    }

    @Test
    public void testConstants() {
        assertEquals("call_rooms", CallRepository.COLLECTION_CALL_ROOMS);
        assertEquals("VOICE", CallRepository.CALL_TYPE_VOICE);
        assertEquals("VIDEO", CallRepository.CALL_TYPE_VIDEO);
        assertEquals("RINGING", CallRepository.STATUS_RINGING);
        assertEquals("CONNECTED", CallRepository.STATUS_CONNECTED);
        assertEquals("ENDED", CallRepository.STATUS_ENDED);
        assertEquals("REJECTED", CallRepository.STATUS_REJECTED);
    }

    @Test
    public void testZegoConfigDynamicCredentials() {
        String test64Sign = "1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef";
        ZegoConfig.setCredentials(987654321L, test64Sign);
        assertTrue(ZegoConfig.isConfigured());
        assertEquals(987654321L, ZegoConfig.getAppId());
        assertEquals(test64Sign, ZegoConfig.getAppSign());
        ZegoConfig.resetToDefault();
    }

    @Test
    public void testCreateCallRoomSuccess() {
        when(mockDocumentReference.set(anyMap())).thenReturn(mockVoidTask);
        when(mockVoidTask.addOnSuccessListener(any())).thenReturn(mockVoidTask);
        when(mockVoidTask.addOnFailureListener(any())).thenReturn(mockVoidTask);

        AtomicReference<String> createdRoomId = new AtomicReference<>();
        callRepository.createCallRoom(
                "user_caller",
                "Alice",
                "user_receiver",
                "Bob",
                CallRepository.CALL_TYPE_VIDEO,
                new AuthCallback<String>() {
                    @Override
                    public void onSuccess(String result) {
                        createdRoomId.set(result);
                    }

                    @Override
                    public void onFailure(String errorMessage) {
                    }
                }
        );

        verify(mockDocumentReference).set(anyMap());
        verify(mockVoidTask).addOnSuccessListener(successCaptor.capture());
        successCaptor.getValue().onSuccess(null);

        assertNotNull(createdRoomId.get());
        assertTrue(createdRoomId.get().startsWith("call_"));
    }

    @Test
    public void testUpdateCallStatusSuccess() {
        when(mockDocumentReference.update(eq("callStatus"), eq(CallRepository.STATUS_CONNECTED), eq("updatedAt"), any()))
                .thenReturn(mockVoidTask);
        when(mockVoidTask.addOnSuccessListener(any())).thenReturn(mockVoidTask);
        when(mockVoidTask.addOnFailureListener(any())).thenReturn(mockVoidTask);

        AtomicBoolean success = new AtomicBoolean(false);
        callRepository.updateCallStatus("call_123", CallRepository.STATUS_CONNECTED, new AuthCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                success.set(true);
            }

            @Override
            public void onFailure(String errorMessage) {
            }
        });

        verify(mockVoidTask).addOnSuccessListener(successCaptor.capture());
        successCaptor.getValue().onSuccess(null);

        assertTrue(success.get());
    }
}
