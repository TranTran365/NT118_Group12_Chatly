package com.group12.chatly.data.model;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class UserUnitTest {

    @Test
    public void testUserInitialization() {
        User user = new User("uid123", "Alex Morgan", "alex@example.com");

        assertEquals("uid123", user.getUserId());
        assertEquals("Alex Morgan", user.getDisplayName());
        assertEquals("alex@example.com", user.getEmail());
        assertTrue(user.isOnline());
        assertNotNull(user.getBio());
        assertNotNull(user.getCreatedAt());
        assertNotNull(user.getLastSeen());
    }

    @Test
    public void testSettersAndGetters() {
        User user = new User();
        user.setUserId("test_id");
        user.setDisplayName("Test User");
        user.setEmail("test@chatly.com");
        user.setPhoneNumber("+84123456789");
        user.setAvatarUrl("https://example.com/avatar.jpg");
        user.setBio("My custom status");
        user.setOnline(false);
        user.setFcmToken("fcm_sample_token");

        assertEquals("test_id", user.getUserId());
        assertEquals("Test User", user.getDisplayName());
        assertEquals("test@chatly.com", user.getEmail());
        assertEquals("+84123456789", user.getPhoneNumber());
        assertEquals("https://example.com/avatar.jpg", user.getAvatarUrl());
        assertEquals("My custom status", user.getBio());
        assertEquals(false, user.isOnline());
        assertEquals("fcm_sample_token", user.getFcmToken());
    }
}

