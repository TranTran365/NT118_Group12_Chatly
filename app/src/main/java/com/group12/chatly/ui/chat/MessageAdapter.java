package com.group12.chatly.ui.chat;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.res.ColorStateList;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.widget.ImageViewCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.Timestamp;
import com.group12.chatly.R;
import com.group12.chatly.data.model.Message;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_SENT = 1;
    private static final int VIEW_TYPE_RECEIVED = 2;
    private static final int VIEW_TYPE_TYPING = 3;
    private static final int VIEW_TYPE_DATE_HEADER = 4;
    private static final int VIEW_TYPE_SENT_IMAGE = 5;
    private static final int VIEW_TYPE_RECEIVED_IMAGE = 6;
    private static final int VIEW_TYPE_SENT_VIDEO = 7;
    private static final int VIEW_TYPE_RECEIVED_VIDEO = 8;

    private final String currentUserId;
    private final List<DisplayItem> displayList = new ArrayList<>();
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd/MM", Locale.getDefault());

    private long otherReadAtMillis = 0;
    private boolean showTyping = false;

    public interface OnMessageClickListener {
        void onMessageLongClick(Message message);
    }

    private OnMessageClickListener listener;

    public MessageAdapter(String currentUserId) {
        this.currentUserId = currentUserId;
    }

    public void setOnMessageClickListener(OnMessageClickListener listener) {
        this.listener = listener;
    }

    public void setMessages(List<Message> newMessages) {
        this.displayList.clear();
        if (newMessages != null && !newMessages.isEmpty()) {
            Calendar prevCal = null;

            for (Message msg : newMessages) {
                if (msg.getCreatedAt() != null) {
                    Date msgDate = msg.getCreatedAt().toDate();
                    Calendar msgCal = Calendar.getInstance();
                    msgCal.setTime(msgDate);

                    if (prevCal == null || !isSameDay(prevCal, msgCal)) {
                        String headerText = formatDateHeader(msgDate);
                        displayList.add(new DisplayItem(VIEW_TYPE_DATE_HEADER, null, headerText));
                        prevCal = msgCal;
                    }
                }

                boolean isSent = msg.getSenderId() != null && msg.getSenderId().equals(currentUserId);
                String msgType = msg.getType() != null ? msg.getType() : Message.TYPE_TEXT;
                int type;
                if (Message.TYPE_IMAGE.equals(msgType)) {
                    type = isSent ? VIEW_TYPE_SENT_IMAGE : VIEW_TYPE_RECEIVED_IMAGE;
                } else if (Message.TYPE_VIDEO.equals(msgType)) {
                    type = isSent ? VIEW_TYPE_SENT_VIDEO : VIEW_TYPE_RECEIVED_VIDEO;
                } else {
                    type = isSent ? VIEW_TYPE_SENT : VIEW_TYPE_RECEIVED;
                }
                displayList.add(new DisplayItem(type, msg, null));
            }
        }
        notifyDataSetChanged();
    }

    public void setOtherReadAtMillis(long millis) {
        if (this.otherReadAtMillis == millis) return;
        this.otherReadAtMillis = millis;
        notifyDataSetChanged();
    }

    public void setTyping(boolean typing) {
        if (this.showTyping == typing) return;
        this.showTyping = typing;
        if (typing) {
            notifyItemInserted(displayList.size());
        } else {
            notifyItemRemoved(displayList.size());
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position == displayList.size()) {
            return VIEW_TYPE_TYPING;
        }
        return displayList.get(position).type;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == VIEW_TYPE_DATE_HEADER) {
            return new DateHeaderViewHolder(inflater.inflate(R.layout.item_date_header, parent, false));
        } else if (viewType == VIEW_TYPE_SENT_IMAGE) {
            return new SentImageViewHolder(inflater.inflate(R.layout.item_message_sent_image, parent, false));
        } else if (viewType == VIEW_TYPE_RECEIVED_IMAGE) {
            return new ReceivedImageViewHolder(inflater.inflate(R.layout.item_message_received_image, parent, false));
        } else if (viewType == VIEW_TYPE_SENT_VIDEO) {
            return new SentVideoViewHolder(inflater.inflate(R.layout.item_message_sent_video, parent, false));
        } else if (viewType == VIEW_TYPE_RECEIVED_VIDEO) {
            return new ReceivedVideoViewHolder(inflater.inflate(R.layout.item_message_received_video, parent, false));
        } else if (viewType == VIEW_TYPE_SENT) {
            return new SentViewHolder(inflater.inflate(R.layout.item_message_sent, parent, false));
        } else if (viewType == VIEW_TYPE_TYPING) {
            return new TypingViewHolder(inflater.inflate(R.layout.item_typing_indicator, parent, false));
        } else {
            return new ReceivedViewHolder(inflater.inflate(R.layout.item_message_received, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof TypingViewHolder) return;

        DisplayItem item = displayList.get(position);

        if (holder instanceof DateHeaderViewHolder) {
            ((DateHeaderViewHolder) holder).tvDateHeader.setText(item.dateText);
        } else if (holder instanceof SentImageViewHolder) {
            Message msg = item.message;
            SentImageViewHolder sent = (SentImageViewHolder) holder;
            bindMediaContent(sent.tvContent, msg);
            sent.tvTime.setText(formatTimestamp(msg.getCreatedAt()));
            bindStatus(sent.imgStatus, msg);
            bindReactions(sent.tvReaction, msg);
            loadMediaImage(sent.ivMessageImage, msg);
            setupLongClick(sent.llBubble, msg);
        } else if (holder instanceof ReceivedImageViewHolder) {
            Message msg = item.message;
            ReceivedImageViewHolder recv = (ReceivedImageViewHolder) holder;
            bindMediaContent(recv.tvContent, msg);
            recv.tvTime.setText(formatTimestamp(msg.getCreatedAt()));
            bindReactions(recv.tvReaction, msg);
            loadMediaImage(recv.ivMessageImage, msg);
            setupLongClick(recv.llBubble, msg);
        } else if (holder instanceof SentVideoViewHolder) {
            Message msg = item.message;
            SentVideoViewHolder sent = (SentVideoViewHolder) holder;
            bindMediaContent(sent.tvContent, msg);
            sent.tvTime.setText(formatTimestamp(msg.getCreatedAt()));
            bindStatus(sent.imgStatus, msg);
            bindReactions(sent.tvReaction, msg);
            loadMediaImage(sent.ivVideoThumbnail, msg);
            setupLongClick(sent.llBubble, msg);
        } else if (holder instanceof ReceivedVideoViewHolder) {
            Message msg = item.message;
            ReceivedVideoViewHolder recv = (ReceivedVideoViewHolder) holder;
            bindMediaContent(recv.tvContent, msg);
            recv.tvTime.setText(formatTimestamp(msg.getCreatedAt()));
            bindReactions(recv.tvReaction, msg);
            loadMediaImage(recv.ivVideoThumbnail, msg);
            setupLongClick(recv.llBubble, msg);
        } else if (holder instanceof SentViewHolder) {
            Message msg = item.message;
            SentViewHolder sent = (SentViewHolder) holder;
            sent.tvContent.setText(msg.isDeleted() ? "This message was deleted" : msg.getContent());
            sent.tvContent.setAlpha(msg.isDeleted() ? 0.6f : 1.0f);
            sent.tvTime.setText(formatTimestamp(msg.getCreatedAt()));
            bindStatus(sent.imgStatus, msg);
            bindReactions(sent.tvReaction, msg);
            setupLongClick(sent.llBubble != null ? sent.llBubble : sent.itemView, msg);
        } else if (holder instanceof ReceivedViewHolder) {
            Message msg = item.message;
            ReceivedViewHolder recv = (ReceivedViewHolder) holder;
            recv.tvContent.setText(msg.isDeleted() ? "This message was deleted" : msg.getContent());
            recv.tvContent.setAlpha(msg.isDeleted() ? 0.6f : 1.0f);
            recv.tvTime.setText(formatTimestamp(msg.getCreatedAt()));
            bindReactions(recv.tvReaction, msg);
            setupLongClick(recv.llBubble != null ? recv.llBubble : recv.itemView, msg);
        }
    }

    private void bindMediaContent(TextView tvContent, Message msg) {
        if (tvContent == null) return;
        if (msg.getContent() != null && !msg.getContent().trim().isEmpty()) {
            tvContent.setText(msg.getContent());
            tvContent.setVisibility(View.VISIBLE);
        } else {
            tvContent.setVisibility(View.GONE);
        }
    }

    private void loadMediaImage(ImageView imageView, Message msg) {
        if (imageView == null || msg == null) return;
        if (msg.getSenderAvatar() != null && !msg.getSenderAvatar().trim().isEmpty()) {
            Glide.with(imageView.getContext())
                    .load(msg.getSenderAvatar())
                    .into(imageView);
        }
    }

    private void setupLongClick(View targetView, Message msg) {
        if (targetView == null) return;
        targetView.setOnLongClickListener(v -> {
            if (listener != null && !msg.isDeleted()) {
                listener.onMessageLongClick(msg);
                return true;
            }
            return false;
        });
    }

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

    private void bindStatus(ImageView imgStatus, Message msg) {
        long createdMillis = msg.getCreatedAt() == null ? 0 : msg.getCreatedAt().toDate().getTime();
        boolean seen = createdMillis > 0 && createdMillis <= otherReadAtMillis;

        imgStatus.setImageResource(seen ? R.drawable.ic_double_check : R.drawable.ic_single_check);
        int colorRes = seen ? R.color.accent : R.color.msg_tick_sent;
        ImageViewCompat.setImageTintList(imgStatus,
                ColorStateList.valueOf(ContextCompat.getColor(imgStatus.getContext(), colorRes)));
    }

    private void bindReactions(TextView tvReaction, Message msg) {
        if (tvReaction == null) return;
        if (msg == null || msg.isDeleted()) {
            tvReaction.setVisibility(View.GONE);
            return;
        }
        java.util.Map<String, String> reactions = msg.getReactions();
        if (reactions != null && !reactions.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            java.util.Set<String> unique = new java.util.LinkedHashSet<>(reactions.values());
            for (String emoji : unique) {
                if (emoji != null) sb.append(emoji);
            }
            if (sb.length() > 0) {
                tvReaction.setText(sb.toString());
                tvReaction.setVisibility(View.VISIBLE);
                return;
            }
        }
        tvReaction.setVisibility(View.GONE);
    }

    @Override
    public int getItemCount() {
        return displayList.size() + (showTyping ? 1 : 0);
    }

    private String formatTimestamp(Timestamp timestamp) {
        if (timestamp == null) return "";
        return timeFormat.format(timestamp.toDate());
    }

    private String formatDateHeader(Date date) {
        if (DateUtils.isToday(date.getTime())) {
            return "TODAY - " + timeFormat.format(date);
        } else if (isYesterday(date)) {
            return "YESTERDAY";
        } else {
            return dateFormat.format(date).toUpperCase(Locale.getDefault());
        }
    }

    private boolean isSameDay(Calendar cal1, Calendar cal2) {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }

    private boolean isYesterday(Date date) {
        Calendar yesterday = Calendar.getInstance();
        yesterday.add(Calendar.DAY_OF_YEAR, -1);

        Calendar msgCal = Calendar.getInstance();
        msgCal.setTime(date);

        return isSameDay(yesterday, msgCal);
    }

    public static class DisplayItem {
        public final int type;
        public final Message message;
        public final String dateText;

        public DisplayItem(int type, Message message, String dateText) {
            this.type = type;
            this.message = message;
            this.dateText = dateText;
        }
    }

    static class DateHeaderViewHolder extends RecyclerView.ViewHolder {
        TextView tvDateHeader;

        DateHeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDateHeader = itemView.findViewById(R.id.tvDateHeader);
        }
    }

    static class SentViewHolder extends RecyclerView.ViewHolder {
        View llBubble;
        TextView tvContent, tvTime, tvReaction;
        ImageView imgStatus;

        SentViewHolder(@NonNull View itemView) {
            super(itemView);
            llBubble = itemView.findViewById(R.id.ll_bubble);
            tvContent = itemView.findViewById(R.id.tvMessageContent);
            tvTime = itemView.findViewById(R.id.tvMessageTime);
            tvReaction = itemView.findViewById(R.id.tvReaction);
            imgStatus = itemView.findViewById(R.id.imgStatus);
        }
    }

    static class ReceivedViewHolder extends RecyclerView.ViewHolder {
        View llBubble;
        TextView tvContent, tvTime, tvReaction;

        ReceivedViewHolder(@NonNull View itemView) {
            super(itemView);
            llBubble = itemView.findViewById(R.id.ll_bubble);
            tvContent = itemView.findViewById(R.id.tvMessageContent);
            tvTime = itemView.findViewById(R.id.tvMessageTime);
            tvReaction = itemView.findViewById(R.id.tvReaction);
        }
    }

    static class SentImageViewHolder extends RecyclerView.ViewHolder {
        View llBubble;
        ImageView ivMessageImage, imgStatus;
        TextView tvContent, tvTime, tvReaction;

        SentImageViewHolder(@NonNull View itemView) {
            super(itemView);
            llBubble = itemView.findViewById(R.id.ll_bubble);
            ivMessageImage = itemView.findViewById(R.id.ivMessageImage);
            tvContent = itemView.findViewById(R.id.tvMessageContent);
            tvTime = itemView.findViewById(R.id.tvMessageTime);
            tvReaction = itemView.findViewById(R.id.tvReaction);
            imgStatus = itemView.findViewById(R.id.imgStatus);
        }
    }

    static class ReceivedImageViewHolder extends RecyclerView.ViewHolder {
        View llBubble;
        ImageView ivMessageImage;
        TextView tvContent, tvTime, tvReaction;

        ReceivedImageViewHolder(@NonNull View itemView) {
            super(itemView);
            llBubble = itemView.findViewById(R.id.ll_bubble);
            ivMessageImage = itemView.findViewById(R.id.ivMessageImage);
            tvContent = itemView.findViewById(R.id.tvMessageContent);
            tvTime = itemView.findViewById(R.id.tvMessageTime);
            tvReaction = itemView.findViewById(R.id.tvReaction);
        }
    }

    static class SentVideoViewHolder extends RecyclerView.ViewHolder {
        View llBubble;
        ImageView ivVideoThumbnail, ivPlayButton, imgStatus;
        TextView tvContent, tvTime, tvReaction;

        SentVideoViewHolder(@NonNull View itemView) {
            super(itemView);
            llBubble = itemView.findViewById(R.id.ll_bubble);
            ivVideoThumbnail = itemView.findViewById(R.id.ivVideoThumbnail);
            ivPlayButton = itemView.findViewById(R.id.ivPlayButton);
            tvContent = itemView.findViewById(R.id.tvMessageContent);
            tvTime = itemView.findViewById(R.id.tvMessageTime);
            tvReaction = itemView.findViewById(R.id.tvReaction);
            imgStatus = itemView.findViewById(R.id.imgStatus);
        }
    }

    static class ReceivedVideoViewHolder extends RecyclerView.ViewHolder {
        View llBubble;
        ImageView ivVideoThumbnail, ivPlayButton;
        TextView tvContent, tvTime, tvReaction;

        ReceivedVideoViewHolder(@NonNull View itemView) {
            super(itemView);
            llBubble = itemView.findViewById(R.id.ll_bubble);
            ivVideoThumbnail = itemView.findViewById(R.id.ivVideoThumbnail);
            ivPlayButton = itemView.findViewById(R.id.ivPlayButton);
            tvContent = itemView.findViewById(R.id.tvMessageContent);
            tvTime = itemView.findViewById(R.id.tvMessageTime);
            tvReaction = itemView.findViewById(R.id.tvReaction);
        }
    }

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
