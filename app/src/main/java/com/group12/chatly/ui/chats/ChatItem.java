package com.group12.chatly.ui.chats;

/** Dữ liệu một dòng trong danh sách chat (đã ghép thông tin người kia + tin cuối). */
public class ChatItem {

    public final String chatId;
    public final String otherUserId;
    public final String name;
    public final String avatarUrl;
    public final String preview;
    public final long timeMillis;
    public final long unread;
    public final boolean isGroup;
    public final boolean typing; // người kia đang gõ cho mình

    public ChatItem(String chatId, String otherUserId, String name, String avatarUrl,
                    String preview, long timeMillis, long unread, boolean isGroup,
                    boolean typing) {
        this.chatId = chatId;
        this.otherUserId = otherUserId;
        this.name = name;
        this.avatarUrl = avatarUrl;
        this.preview = preview;
        this.timeMillis = timeMillis;
        this.unread = unread;
        this.isGroup = isGroup;
        this.typing = typing;
    }

    /** Dành cho nơi không cần biết trạng thái gõ (vd. màn chọn người). */
    public ChatItem(String chatId, String otherUserId, String name, String avatarUrl,
                    String preview, long timeMillis, long unread, boolean isGroup) {
        this(chatId, otherUserId, name, avatarUrl, preview, timeMillis, unread, isGroup, false);
    }
}