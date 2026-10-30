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

    private final String currentUserId;
    private final List<DisplayItem> displayList = new ArrayList<>();
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd/MM", Locale.getDefault());

    private long otherReadAtMillis = 0;
    private boolean showTyping = false;

    public MessageAdapter(String currentUserId) {
        this.currentUserId = currentUserId;
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
                int type = isSent ? VIEW_TYPE_SENT : VIEW_TYPE_RECEIVED;
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
        } else if (holder instanceof SentViewHolder) {
            Message msg = item.message;
            SentViewHolder sent = (SentViewHolder) holder;
            sent.tvContent.setText(msg.getContent());
            sent.tvTime.setText(formatTimestamp(msg.getCreatedAt()));
            bindStatus(sent.imgStatus, msg);
        } else if (holder instanceof ReceivedViewHolder) {
            Message msg = item.message;
            ((ReceivedViewHolder) holder).tvContent.setText(msg.getContent());
            ((ReceivedViewHolder) holder).tvTime.setText(formatTimestamp(msg.getCreatedAt()));
        }
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
