package com.group12.chatly.ui.chat;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.widget.ImageViewCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;
import com.group12.chatly.R;
import com.group12.chatly.data.model.Message;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_SENT = 1;
    private static final int VIEW_TYPE_RECEIVED = 2;
    private static final int VIEW_TYPE_TYPING = 3;

    private final String currentUserId;
    private final List<Message> messageList = new ArrayList<>();
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

    // Thời điểm (ms) người kia đọc chat lần cuối; 0 nếu chưa đọc
    private long otherReadAtMillis = 0;
    // Người kia đang gõ -> thêm một dòng bong bóng ba chấm ở cuối danh sách
    private boolean showTyping = false;

    public MessageAdapter(String currentUserId) {
        this.currentUserId = currentUserId;
    }

    public void setMessages(List<Message> newMessages) {
        this.messageList.clear();
        if (newMessages != null) {
            this.messageList.addAll(newMessages);
        }
        notifyDataSetChanged();
    }

    /** Cập nhật mốc người kia đã đọc, vẽ lại dấu tích. */
    public void setOtherReadAtMillis(long millis) {
        if (this.otherReadAtMillis == millis) return;
        this.otherReadAtMillis = millis;
        notifyDataSetChanged();
    }

    /** Bật/tắt bong bóng "đang nhập" ở cuối danh sách. */
    public void setTyping(boolean typing) {
        if (this.showTyping == typing) return;
        this.showTyping = typing;
        if (typing) {
            notifyItemInserted(messageList.size());
        } else {
            notifyItemRemoved(messageList.size());
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position == messageList.size()) {
            return VIEW_TYPE_TYPING; // chỉ xảy ra khi showTyping = true
        }
        Message msg = messageList.get(position);
        if (msg.getSenderId() != null && msg.getSenderId().equals(currentUserId)) {
            return VIEW_TYPE_SENT;
        } else {
            return VIEW_TYPE_RECEIVED;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == VIEW_TYPE_SENT) {
            return new SentViewHolder(inflater.inflate(R.layout.item_message_sent, parent, false));
        } else if (viewType == VIEW_TYPE_TYPING) {
            return new TypingViewHolder(inflater.inflate(R.layout.item_typing_indicator, parent, false));
        } else {
            return new ReceivedViewHolder(inflater.inflate(R.layout.item_message_received, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof TypingViewHolder) return; // không có dữ liệu để gán

        Message msg = messageList.get(position);
        String timeStr = formatTimestamp(msg.getCreatedAt());

        if (holder instanceof SentViewHolder) {
            SentViewHolder sent = (SentViewHolder) holder;
            sent.tvContent.setText(msg.getContent());
            sent.tvTime.setText(timeStr);
            bindStatus(sent.imgStatus, msg);
        } else if (holder instanceof ReceivedViewHolder) {
            ((ReceivedViewHolder) holder).tvContent.setText(msg.getContent());
            ((ReceivedViewHolder) holder).tvTime.setText(timeStr);
        }
    }

    // Chỉ chạy animation khi dòng đang hiện trên màn hình
    @Override
    public void onViewAttachedToWindow(@NonNull RecyclerView.ViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        if (holder instanceof TypingViewHolder) {
            ((TypingViewHolder) holder).startAnimation();
        }
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull RecyclerView.ViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        if (holder instanceof TypingViewHolder) {
            ((TypingViewHolder) holder).stopAnimation();
        }
    }

    /**
     * ✓ : đã gửi (hoặc server chưa xác nhận giờ) / ✓✓ vàng: người kia đã đọc đến lúc này.
     * Chỉ đúng cho chat 1-1.
     */
    private void bindStatus(ImageView imgStatus, Message msg) {
        long createdMillis = msg.getCreatedAt() == null ? 0 : msg.getCreatedAt().toDate().getTime();
        boolean seen = createdMillis > 0 && createdMillis <= otherReadAtMillis;

        imgStatus.setImageResource(seen ? R.drawable.ic_double_check : R.drawable.ic_single_check);
        int colorRes = seen ? R.color.accent : R.color.msg_tick_sent;
        ImageViewCompat.setImageTintList(imgStatus,
                ColorStateList.valueOf(ContextCompat.getColor(imgStatus.getContext(), colorRes)));
    }

    @Override
    public int getItemCount() {
        return messageList.size() + (showTyping ? 1 : 0);
    }

    private String formatTimestamp(Timestamp timestamp) {
        if (timestamp == null) return "";
        return timeFormat.format(timestamp.toDate());
    }

    static class SentViewHolder extends RecyclerView.ViewHolder {
        TextView tvContent, tvTime;
        ImageView imgStatus;

        SentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvContent = itemView.findViewById(R.id.tvMessageContent);
            tvTime = itemView.findViewById(R.id.tvMessageTime);
            imgStatus = itemView.findViewById(R.id.imgStatus);
        }
    }

    static class ReceivedViewHolder extends RecyclerView.ViewHolder {
        TextView tvContent, tvTime;

        ReceivedViewHolder(@NonNull View itemView) {
            super(itemView);
            tvContent = itemView.findViewById(R.id.tvMessageContent);
            tvTime = itemView.findViewById(R.id.tvMessageTime);
        }
    }

    /** Bong bóng ba chấm: ba chấm nhảy lần lượt, lệch nhau 150ms. */
    static class TypingViewHolder extends RecyclerView.ViewHolder {
        private final View[] dots;
        private final List<ObjectAnimator> animators = new ArrayList<>();

        TypingViewHolder(@NonNull View itemView) {
            super(itemView);
            dots = new View[]{
                    itemView.findViewById(R.id.dot1),
                    itemView.findViewById(R.id.dot2),
                    itemView.findViewById(R.id.dot3)
            };
        }

        void startAnimation() {
            stopAnimation();
            float lift = -4 * itemView.getResources().getDisplayMetrics().density;
            for (int i = 0; i < dots.length; i++) {
                ObjectAnimator animator = ObjectAnimator.ofFloat(dots[i], View.TRANSLATION_Y, 0f, lift, 0f);
                animator.setDuration(600);
                animator.setRepeatCount(ValueAnimator.INFINITE);
                animator.setStartDelay(i * 150L);
                animator.start();
                animators.add(animator);
            }
        }

        void stopAnimation() {
            for (ObjectAnimator animator : animators) {
                animator.cancel();
            }
            animators.clear();
            for (View dot : dots) {
                dot.setTranslationY(0f);
            }
        }
    }
}