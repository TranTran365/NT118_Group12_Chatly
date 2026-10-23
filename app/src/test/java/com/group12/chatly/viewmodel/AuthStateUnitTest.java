package com.group12.chatly.viewmodel;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class AuthStateUnitTest {

    @Test
    public void testIdleState() {
        AuthState<String> state = AuthState.idle();

        assertEquals(AuthState.Status.IDLE, state.getStatus());
        assertFalse(state.isLoading());
        assertFalse(state.isSuccess());
        assertFalse(state.isError());
        assertNull(state.getData());
        assertNull(state.getErrorMessage());
    }

    @Test
    public void testLoadingState() {
        AuthState<String> state = AuthState.loading();

        assertEquals(AuthState.Status.LOADING, state.getStatus());
        assertTrue(state.isLoading());
        assertFalse(state.isSuccess());
        assertFalse(state.isError());
    }

    @Test
    public void testSuccessState() {
        AuthState<String> state = AuthState.success("Welcome");

        assertEquals(AuthState.Status.SUCCESS, state.getStatus());
        assertFalse(state.isLoading());
        assertTrue(state.isSuccess());
        assertFalse(state.isError());
        assertEquals("Welcome", state.getData());
    }

    @Test
    public void testErrorState() {
        AuthState<String> state = AuthState.error("Authentication failed");

        assertEquals(AuthState.Status.ERROR, state.getStatus());
        assertFalse(state.isLoading());
        assertFalse(state.isSuccess());
        assertTrue(state.isError());
        assertEquals("Authentication failed", state.getErrorMessage());
    }
}

