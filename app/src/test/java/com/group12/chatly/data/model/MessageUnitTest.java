package com.group12.chatly.data.model;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class MessageUnitTest {

    @Test
    public void testMessageInitialization() {
        Message msg = new Message("msg1", "chat1", "senderA", "Sender Name", Message.TYPE_TEXT, "Hello Chatly!");

        assertEquals("msg1", msg.getMessageId());
        assertEquals("chat1", msg.getChatId());
        assertEquals("senderA", msg.getSenderId());
        assertEquals("Sender Name", msg.getSenderName());
        assertEquals(Message.TYPE_TEXT, msg.getType());
        assertEquals("Hello Chatly!", msg.getContent());
        assertFalse(msg.isDeleted());
        assertTrue(msg.getReadBy().contains("senderA"));
        assertNotNull(msg.getCreatedAt());
    }

    @Test
    public void testRecallMessage() {
        Message msg = new Message();
        msg.setMessageId("msg2");
        msg.setContent("Secret message");
        msg.setDeleted(true);

        assertTrue(msg.isDeleted());
    }
}
