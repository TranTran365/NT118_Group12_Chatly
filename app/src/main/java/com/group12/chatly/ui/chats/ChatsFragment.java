package com.group12.chatly.ui.chats;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.group12.chatly.ui.chat.NewChatActivity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.group12.chatly.R;
import com.group12.chatly.ui.chat.ChatActivity;

public class ChatsFragment extends Fragment {

    private ChatsViewModel viewModel;
    private ChatAdapter adapter;

    private RecyclerView rcvChats;
    private View svChatsEmpty;
    private View btnLetsYap;
    private TextView tvFilterAll, tvFilterUnread, tvFilterGroups;

    public ChatsFragment() {
        super(R.layout.fragment_chats);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rcvChats = view.findViewById(R.id.rcv_chats);
        svChatsEmpty = view.findViewById(R.id.sv_chats_empty);
        btnLetsYap = view.findViewById(R.id.btnLetsYap);
        tvFilterAll = view.findViewById(R.id.tvFilterAll);
        tvFilterUnread = view.findViewById(R.id.tvFilterUnread);
        tvFilterGroups = view.findViewById(R.id.tvFilterGroups);
        EditText etSearch = view.findViewById(R.id.etSearch);

        // Ẩn màn rỗng cho tới khi có dữ liệu lần đầu (layout để mặc định là hiện)
        svChatsEmpty.setVisibility(View.GONE);

        adapter = new ChatAdapter(this::openChat);
        rcvChats.setLayoutManager(new LinearLayoutManager(requireContext()));
        rcvChats.setAdapter(adapter);

        viewModel = new ViewModelProvider(this).get(ChatsViewModel.class);

        // Có chat -> danh sách + nút "let's yap."; chưa có -> màn rỗng
        viewModel.getHasChats().observe(getViewLifecycleOwner(), has -> {
            boolean hasChats = Boolean.TRUE.equals(has);
            rcvChats.setVisibility(hasChats ? View.VISIBLE : View.GONE);
            btnLetsYap.setVisibility(hasChats ? View.VISIBLE : View.GONE);
            svChatsEmpty.setVisibility(hasChats ? View.GONE : View.VISIBLE);
        });

        viewModel.getVisibleItems().observe(getViewLifecycleOwner(), adapter::submitList);

        // Lỗi (rules, thiếu index...) -> báo lỗi và không để màn hình trắng
        viewModel.getError().observe(getViewLifecycleOwner(), message -> {
            if (message == null) return;
            Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
            if (rcvChats.getVisibility() != View.VISIBLE) {
                svChatsEmpty.setVisibility(View.VISIBLE);
            }
        });

        // Tab lọc
        tvFilterAll.setOnClickListener(v -> selectFilter(ChatsViewModel.Filter.ALL));
        tvFilterUnread.setOnClickListener(v -> selectFilter(ChatsViewModel.Filter.UNREAD));
        tvFilterGroups.setOnClickListener(v -> selectFilter(ChatsViewModel.Filter.GROUPS));
        renderFilterTabs(viewModel.getFilter());

        // Ô tìm kiếm: lọc theo tên người trong danh sách
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                viewModel.setQuery(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // Bắt đầu chat mới (tạm: mở chat demo; sẽ đổi sang màn chọn người ở bước sau)
        View btnSayHey = view.findViewById(R.id.btnSayHey);
        if (btnSayHey != null) {
            btnSayHey.setOnClickListener(v -> openNewChat());
        }
        btnLetsYap.setOnClickListener(v -> openNewChat());
    }

    private void selectFilter(ChatsViewModel.Filter filter) {
        viewModel.setFilter(filter);
        renderFilterTabs(filter);
    }

    private void renderFilterTabs(ChatsViewModel.Filter filter) {
        styleTab(tvFilterAll, filter == ChatsViewModel.Filter.ALL);
        styleTab(tvFilterUnread, filter == ChatsViewModel.Filter.UNREAD);
        styleTab(tvFilterGroups, filter == ChatsViewModel.Filter.GROUPS);
    }

    /** Tab đang chọn: nền vàng bo tròn + đệm hai bên; tab còn lại: chữ trơn. */
    private void styleTab(TextView tab, boolean active) {
        int horizontal = active ? Math.round(14 * getResources().getDisplayMetrics().density) : 0;
        tab.setBackgroundResource(active ? R.drawable.bg_filter_active : 0);
        tab.setPadding(horizontal, tab.getPaddingTop(), horizontal, tab.getPaddingBottom());
    }

    /** Bấm một dòng trong danh sách -> mở đúng cuộc trò chuyện đó. */
    private void openChat(ChatItem item) {
        Intent intent = new Intent(requireContext(), ChatActivity.class);
        intent.putExtra(ChatActivity.EXTRA_TARGET_USER_ID, item.otherUserId);
        intent.putExtra(ChatActivity.EXTRA_TARGET_USER_NAME, item.name);
        startActivity(intent);
    }

    /** TẠM THỜI: mở chat với user demo. Bước sau thay bằng màn chọn người. */
    /** Bắt đầu chat mới: mở màn chọn người. */
    private void openNewChat() {
        startActivity(new Intent(requireContext(), NewChatActivity.class));
    }
}