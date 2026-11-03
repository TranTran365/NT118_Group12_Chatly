package com.group12.chatly.data.model;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class FriendshipUnitTest {

    @Test
    public void testFriendshipModel() {
        Friendship friendship = new Friendship("userA_userB", "userA", "userB", Friendship.STATUS_PENDING);

        assertEquals("userA_userB", friendship.getFriendshipId());
        assertEquals("userA", friendship.getRequesterId());
        assertEquals("userB", friendship.getReceiverId());
        assertEquals(Friendship.STATUS_PENDING, friendship.getStatus());
        assertNotNull(friendship.getCreatedAt());
        assertNotNull(friendship.getUpdatedAt());
    }

    @Test
    public void testSetters() {
        Friendship f = new Friendship();
        f.setFriendshipId("id1");
        f.setRequesterId("req1");
        f.setReceiverId("rec1");
        f.setStatus(Friendship.STATUS_ACCEPTED);

        assertEquals("id1", f.getFriendshipId());
        assertEquals("req1", f.getRequesterId());
        assertEquals("rec1", f.getReceiverId());
        assertEquals(Friendship.STATUS_ACCEPTED, f.getStatus());
    }
}

