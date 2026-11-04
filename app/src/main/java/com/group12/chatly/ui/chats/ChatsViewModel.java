package com.group12.chatly.ui.chats;

import android.app.Application;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.firestore.ListenerRegistration;
import com.group12.chatly.R;
import com.group12.chatly.data.model.Chat;
import com.group12.chatly.data.model.User;
import com.group12.chatly.data.repository.AuthCallback;
import com.group12.chatly.data.repository.AuthRepository;
import com.group12.chatly.data.repository.ChatRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import android.os.Handler;
import android.os.Looper;

public class ChatsViewModel extends AndroidViewModel {

    public enum Filter { ALL, UNREAD, GROUPS }

    private final ChatRepository chatRepository = ChatRepository.getInstance();
    private final String myUid;

    // Không gán giá trị ban đầu: fragment chỉ vẽ sau khi có dữ liệu lần đầu,
    // tránh nháy màn "rỗng" trong lúc đang tải.
    private final MutableLiveData<List<ChatItem>> visibleItems = new MutableLiveData<>();
    private final MutableLiveData<Boolean> hasChats = new MutableLiveData<>();
    private final MutableLiveData<String> error = new MutableLiveData<>();

    private final Map<String, User> userCache = new HashMap<>();

    private static final long TYPING_EXPIRE_MS = 6000;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Map<String, Long> lastTypingMillis = new HashMap<>(); // mốc gõ đã thấy, theo chat
    private final Set<String> typingChats = new HashSet<>();
    private final Map<String, Runnable> typingExpiry = new HashMap<>();

    private final Set<String> pendingUsers = new HashSet<>();
    private List<Chat> latestChats = new ArrayList<>();
    private List<ChatItem> allItems = new ArrayList<>();
    private ListenerRegistration registration;

    private Filter filter = Filter.ALL;
    private String query = "";
    private boolean loaded = false;

    public ChatsViewModel(@NonNull Application application) {
        super(application);
        myUid = AuthRepository.getInstance().getCurrentUserId();
        if (myUid == null) {
            loaded = true;
            applyFilter();
            return;
        }
        startListening();
    }

    /** Danh sách đã lọc theo tab + ô tìm kiếm. */
    public LiveData<List<ChatItem>> getVisibleItems() {
        return visibleItems;
    }

    /** Có ít nhất một cuộc trò chuyện hay không (không phụ thuộc bộ lọc). */
    public LiveData<Boolean> getHasChats() {
        return hasChats;
    }

    public LiveData<String> getError() {
        return error;
    }

    public Filter getFilter() {
        return filter;
    }

    public void setFilter(Filter filter) {
        this.filter = filter;
        applyFilter();
    }

    public void setQuery(String query) {
        this.query = query == null ? "" : query;
        applyFilter();
    }

    private void startListening() {
        registration = chatRepository.listenForChats(myUid, new ChatRepository.ChatsListener() {
            @Override
            public void onChatsUpdated(List<Chat> chats) {
                latestChats = chats;
                updateTyping(chats);
                rebuild();
            }

            @Override
            public void onError(String message) {
                error.setValue(message);
            }
        });
    }

    /**
     * Phát hiện người kia vừa gõ: so mốc `typingAt` với lần trước.
     * Lần đầu thấy một chat chỉ ghi mốc (có thể là giá trị cũ còn sót), không hiện "typing…".
     */
    private void updateTyping(List<Chat> chats) {
        for (Chat chat : chats) {
            String otherId = chat.otherMemberId(myUid);
            if (otherId == null) continue;

            String id = chat.getChatId();
            long millis = chat.typingAtMillisOf(otherId);
            Long previous = lastTypingMillis.put(id, millis);
            if (previous == null) continue;

            if (millis == 0) {
                clearTyping(id);                 // người kia đã ngừng gõ
            } else if (millis != previous) {
                markTyping(id);                  // có lần gõ mới
            }
        }
    }

    private void markTyping(String chatId) {
        typingChats.add(chatId);

        Runnable old = typingExpiry.get(chatId);
        if (old != null) handler.removeCallbacks(old);

        // Tự gỡ sau vài giây, phòng khi máy người kia tắt đột ngột không báo "ngừng gõ"
        Runnable expire = () -> {
            typingChats.remove(chatId);
            typingExpiry.remove(chatId);
            rebuild();
        };
        typingExpiry.put(chatId, expire);
        handler.postDelayed(expire, TYPING_EXPIRE_MS);
    }

    private void clearTyping(String chatId) {
        typingChats.remove(chatId);
        Runnable old = typingExpiry.remove(chatId);
        if (old != null) handler.removeCallbacks(old);
    }

    /**
     * Ghép Chat + thông tin người kia thành ChatItem.
     * Nếu còn thiếu hồ sơ ai đó thì tải về rồi build lại, chưa phát dữ liệu dở dang.
     */
    private void rebuild() {
        List<ChatItem> result = new ArrayList<>();
        boolean waiting = false;

        for (Chat chat : latestChats) {
            String otherId = chat.otherMemberId(myUid);
            if (otherId == null) continue;

            User other = userCache.get(otherId);
            if (other == null) {
                fetchUser(otherId);
                waiting = true;
                continue;
            }
            result.add(toItem(chat, otherId, other));
        }

        if (!waiting) {
            allItems = result;
            loaded = true;
            applyFilter();
        }
    }

    /** Áp dụng tab (all/unread/groups) và từ khóa tìm kiếm lên toàn bộ danh sách. */
    private void applyFilter() {
        if (!loaded) return;

        String q = query.trim().toLowerCase(Locale.getDefault());
        List<ChatItem> out = new ArrayList<>();
        for (ChatItem item : allItems) {
            if (filter == Filter.UNREAD && item.unread <= 0) continue;
            if (filter == Filter.GROUPS && !item.isGroup) continue;
            if (!q.isEmpty() && !item.name.toLowerCase(Locale.getDefault()).contains(q)) continue;
            out.add(item);
        }

        hasChats.setValue(!allItems.isEmpty());
        visibleItems.setValue(out);
    }

    private void fetchUser(String uid) {
        if (!pendingUsers.add(uid)) return; // đang tải rồi

        chatRepository.getUserById(uid, new AuthCallback<User>() {
            @Override
            public void onSuccess(User user) {
                pendingUsers.remove(uid);
                userCache.put(uid, user);
                rebuild();
            }

            @Override
            public void onFailure(String errorMessage) {
                // Lỗi thì dùng hồ sơ trống để dòng chat vẫn hiện (tên mặc định)
                pendingUsers.remove(uid);
                userCache.put(uid, new User());
                rebuild();
            }
        });
    }

    private ChatItem toItem(Chat chat, String otherId, User other) {
        Application app = getApplication();

        String name = other.getDisplayName();
        if (TextUtils.isEmpty(name)) {
            name = app.getString(R.string.chats_unknown_user);
        }

        String preview = "";
        long time = 0;
        Chat.LastMessage last = chat.getLastMessage();
        if (last != null) {
            preview = last.getText() == null ? "" : last.getText();
            if (myUid.equals(last.getSenderId())) {
                preview = app.getString(R.string.chats_preview_you) + preview;
            }
            if (last.getTimestamp() != null) {
                time = last.getTimestamp().toDate().getTime();
            }
        }
        if (time == 0 && chat.getUpdatedAt() != null) {
            time = chat.getUpdatedAt().toDate().getTime();
        }

        return new ChatItem(chat.getChatId(), otherId, name, other.getAvatarUrl(),
                preview, time, chat.unreadFor(myUid),
                Chat.TYPE_GROUP.equals(chat.getType()),
                typingChats.contains(chat.getChatId()));
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (registration != null) {
            registration.remove();
        }
        handler.removeCallbacksAndMessages(null);
    }
}