package com.group12.chatly.data.model;

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class ChatUnitTest {

    @Test
    public void testChatInitialization() {
        Chat chat = new Chat("chat123", Chat.TYPE_DIRECT, Arrays.asList("user1", "user2"));

        assertEquals("chat123", chat.getChatId());
        assertEquals(Chat.TYPE_DIRECT, chat.getType());
        assertEquals(2, chat.getParticipants().size());
        assertTrue(chat.getParticipants().contains("user1"));
        assertNotNull(chat.getCreatedAt());
        assertNotNull(chat.getUpdatedAt());
    }

    @Test
    public void testGroupChatSetters() {
        Chat group = new Chat();
        group.setChatId("group_abc");
        group.setType(Chat.TYPE_GROUP);
        group.setName("Nhóm 12");
        group.setAvatarUrl("https://chatly.com/group.jpg");
        group.setAdminIds(Arrays.asList("admin1"));

        assertEquals("Nhóm 12", group.getName());
        assertEquals(Chat.TYPE_GROUP, group.getType());
        assertEquals("https://chatly.com/group.jpg", group.getAvatarUrl());
        assertEquals(1, group.getAdminIds().size());
    }
}
