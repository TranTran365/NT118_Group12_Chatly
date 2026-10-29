package com.group12.chatly.ui.chats;

import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.group12.chatly.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import androidx.core.content.ContextCompat;

public class ChatAdapter extends ListAdapter<ChatItem, ChatAdapter.ChatViewHolder> {

    public interface OnChatClickListener {
        void onChatClick(ChatItem item);
    }

    private final OnChatClickListener listener;

    public ChatAdapter(OnChatClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<ChatItem> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<ChatItem>() {
                @Override
                public boolean areItemsTheSame(@NonNull ChatItem a, @NonNull ChatItem b) {
                    return a.chatId.equals(b.chatId);
                }

                @Override
                public boolean areContentsTheSame(@NonNull ChatItem a, @NonNull ChatItem b) {
                    return TextUtils.equals(a.name, b.name)
                            && TextUtils.equals(a.avatarUrl, b.avatarUrl)
                            && TextUtils.equals(a.preview, b.preview)
                            && a.timeMillis == b.timeMillis
                            && a.unread == b.unread
                            && a.typing == b.typing;

                }
            };

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chat, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        holder.bind(getItem(position), listener);
    }

    static class ChatViewHolder extends RecyclerView.ViewHolder {

        private final ImageView ivAvatar;
        private final TextView tvInitials, tvName, tvPreview, tvTime, tvUnread;

        ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.iv_chat_avatar);
            tvInitials = itemView.findViewById(R.id.tv_chat_initials);
            tvName = itemView.findViewById(R.id.tv_chat_name);
            tvPreview = itemView.findViewById(R.id.tv_chat_preview);
            tvTime = itemView.findViewById(R.id.tv_chat_time);
            tvUnread = itemView.findViewById(R.id.tv_chat_unread);
        }

        void bind(ChatItem item, OnChatClickListener listener) {
            tvName.setText(item.name);
//            tvPreview.setText(item.preview);
            // Người kia đang gõ: thay tin cuối bằng "typing…" màu xanh
            if (item.typing) {
                tvPreview.setText(R.string.chats_typing);
                tvPreview.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.background));
            } else {
                tvPreview.setText(item.preview);
                tvPreview.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.register_text_muted));
            }
            tvTime.setText(formatTime(item.timeMillis));

            // Avatar: có URL thì tải ảnh, không thì hiện chữ cái đầu
            if (!TextUtils.isEmpty(item.avatarUrl)) {
                tvInitials.setVisibility(View.GONE);
                ivAvatar.setVisibility(View.VISIBLE);
                Glide.with(ivAvatar).load(item.avatarUrl).circleCrop().into(ivAvatar);
            } else {
                Glide.with(ivAvatar).clear(ivAvatar);
                ivAvatar.setVisibility(View.GONE);
                tvInitials.setVisibility(View.VISIBLE);
                tvInitials.setText(initialsOf(item.name));
            }

            // Badge chưa đọc
            if (item.unread > 0) {
                tvUnread.setVisibility(View.VISIBLE);
                tvUnread.setText(item.unread > 99
                        ? itemView.getContext().getString(R.string.chats_unread_overflow)
                        : String.valueOf(item.unread));
            } else {
                tvUnread.setVisibility(View.GONE);
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onChatClick(item);
            });
        }

        /** Hôm nay hiện giờ HH:mm, ngày khác hiện dd/MM. */
        private static String formatTime(long millis) {
            if (millis <= 0) return "";
            String pattern = DateUtils.isToday(millis) ? "HH:mm" : "dd/MM";
            return new SimpleDateFormat(pattern, Locale.getDefault()).format(new Date(millis));
        }

        /** Lấy tối đa 2 chữ cái đầu: "Minh Anh" -> "MA", "Linh" -> "L". */
        private static String initialsOf(String name) {
            if (TextUtils.isEmpty(name)) return "?";
            String[] parts = name.trim().split("\\s+");
            StringBuilder sb = new StringBuilder();
            sb.append(Character.toUpperCase(parts[0].charAt(0)));
            if (parts.length > 1) {
                sb.append(Character.toUpperCase(parts[parts.length - 1].charAt(0)));
            }
            return sb.toString();
        }
    }
}