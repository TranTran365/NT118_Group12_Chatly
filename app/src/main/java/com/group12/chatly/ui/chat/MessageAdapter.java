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
    private static final int VIEW_TYPE_SENT_VOICE = 9;
    private static final int VIEW_TYPE_RECEIVED_VOICE = 10;
    private static final int VIEW_TYPE_SENT_FILE = 11;
    private static final int VIEW_TYPE_RECEIVED_FILE = 12;
    private static final int VIEW_TYPE_SENT_LOCATION = 13;
    private static final int VIEW_TYPE_RECEIVED_LOCATION = 14;

    private final String currentUserId;
    private final List<DisplayItem> displayList = new ArrayList<>();
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd/MM", Locale.getDefault());

    private long otherReadAtMillis = 0;
    private boolean showTyping = false;

    public interface OnMessageClickListener {
        void onMessageLongClick(Message message);
    }

    public interface OnMediaClickListener {
        void onMediaClick(Message message);
    }

    private OnMessageClickListener listener;
    private OnMediaClickListener mediaClickListener;

    public MessageAdapter(String currentUserId) {
        this.currentUserId = currentUserId;
    }

    public void setOnMessageClickListener(OnMessageClickListener listener) {
        this.listener = listener;
    }

    public void setOnMediaClickListener(OnMediaClickListener mediaClickListener) {
        this.mediaClickListener = mediaClickListener;
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
                if (msg.isDeleted()) {
                    type = isSent ? VIEW_TYPE_SENT : VIEW_TYPE_RECEIVED;
                } else if (Message.TYPE_IMAGE.equals(msgType)) {
                    type = isSent ? VIEW_TYPE_SENT_IMAGE : VIEW_TYPE_RECEIVED_IMAGE;
                } else if (Message.TYPE_VIDEO.equals(msgType)) {
                    type = isSent ? VIEW_TYPE_SENT_VIDEO : VIEW_TYPE_RECEIVED_VIDEO;
                } else if (Message.TYPE_VOICE.equals(msgType)) {
                    type = isSent ? VIEW_TYPE_SENT_VOICE : VIEW_TYPE_RECEIVED_VOICE;
                } else if (Message.TYPE_FILE.equals(msgType)) {
                    type = isSent ? VIEW_TYPE_SENT_FILE : VIEW_TYPE_RECEIVED_FILE;
                } else if (Message.TYPE_LOCATION.equals(msgType)) {
                    type = isSent ? VIEW_TYPE_SENT_LOCATION : VIEW_TYPE_RECEIVED_LOCATION;
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
        } else if (viewType == VIEW_TYPE_SENT_VOICE) {
            return new SentVoiceViewHolder(inflater.inflate(R.layout.item_message_sent_voice, parent, false));
        } else if (viewType == VIEW_TYPE_RECEIVED_VOICE) {
            return new ReceivedVoiceViewHolder(inflater.inflate(R.layout.item_message_received_voice, parent, false));
        } else if (viewType == VIEW_TYPE_SENT_FILE) {
            return new SentFileViewHolder(inflater.inflate(R.layout.item_message_sent_file, parent, false));
        } else if (viewType == VIEW_TYPE_RECEIVED_FILE) {
            return new ReceivedFileViewHolder(inflater.inflate(R.layout.item_message_received_file, parent, false));
        } else if (viewType == VIEW_TYPE_SENT_LOCATION) {
            return new SentLocationViewHolder(inflater.inflate(R.layout.item_message_sent_location, parent, false));
        } else if (viewType == VIEW_TYPE_RECEIVED_LOCATION) {
            return new ReceivedLocationViewHolder(inflater.inflate(R.layout.item_message_received_location, parent, false));
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
            if (sent.ivPlayButton != null) {
                sent.ivPlayButton.setVisibility(msg.isDeleted() ? View.GONE : View.VISIBLE);
            }
            setupLongClick(sent.llBubble, msg);
        } else if (holder instanceof ReceivedVideoViewHolder) {
            Message msg = item.message;
            ReceivedVideoViewHolder recv = (ReceivedVideoViewHolder) holder;
            bindMediaContent(recv.tvContent, msg);
            recv.tvTime.setText(formatTimestamp(msg.getCreatedAt()));
            bindReactions(recv.tvReaction, msg);
            loadMediaImage(recv.ivVideoThumbnail, msg);
            if (recv.ivPlayButton != null) {
                recv.ivPlayButton.setVisibility(msg.isDeleted() ? View.GONE : View.VISIBLE);
            }
            setupLongClick(recv.llBubble, msg);
        } else if (holder instanceof SentVoiceViewHolder) {
            Message msg = item.message;
            SentVoiceViewHolder sent = (SentVoiceViewHolder) holder;
            bindVoiceInfo(sent.tvDuration, msg);
            sent.tvTime.setText(formatTimestamp(msg.getCreatedAt()));
            bindStatus(sent.imgStatus, msg);
            bindReactions(sent.tvReaction, msg);
            setupLongClick(sent.llBubble, msg);
        } else if (holder instanceof ReceivedVoiceViewHolder) {
            Message msg = item.message;
            ReceivedVoiceViewHolder recv = (ReceivedVoiceViewHolder) holder;
            bindVoiceInfo(recv.tvDuration, msg);
            recv.tvTime.setText(formatTimestamp(msg.getCreatedAt()));
            bindReactions(recv.tvReaction, msg);
            setupLongClick(recv.llBubble, msg);
        } else if (holder instanceof SentFileViewHolder) {
            Message msg = item.message;
            SentFileViewHolder sent = (SentFileViewHolder) holder;
            bindFileInfo(sent.tvFileName, sent.tvSubDetails, msg);
            sent.tvTime.setText(formatTimestamp(msg.getCreatedAt()));
            bindStatus(sent.imgStatus, msg);
            bindReactions(sent.tvReaction, msg);
            setupLongClick(sent.llBubble, msg);
        } else if (holder instanceof ReceivedFileViewHolder) {
            Message msg = item.message;
            ReceivedFileViewHolder recv = (ReceivedFileViewHolder) holder;
            bindFileInfo(recv.tvFileName, recv.tvSubDetails, msg);
            recv.tvTime.setText(formatTimestamp(msg.getCreatedAt()));
            bindReactions(recv.tvReaction, msg);
            setupLongClick(recv.llBubble, msg);
        } else if (holder instanceof SentLocationViewHolder) {
            Message msg = item.message;
            SentLocationViewHolder sent = (SentLocationViewHolder) holder;
            bindLocationInfo(sent.tvLocationTitle, sent.tvLocationAddress, msg);
            sent.tvTime.setText(formatTimestamp(msg.getCreatedAt()));
            bindStatus(sent.imgStatus, msg);
            bindReactions(sent.tvReaction, msg);
            loadMediaImage(sent.ivMapThumbnail, msg);
            setupLongClick(sent.llBubble, msg);
            if (sent.llBubble != null) {
                sent.llBubble.setOnClickListener(v -> {
                    if (mediaClickListener != null && !msg.isDeleted()) {
                        mediaClickListener.onMediaClick(msg);
                    }
                });
            }
        } else if (holder instanceof ReceivedLocationViewHolder) {
            Message msg = item.message;
            ReceivedLocationViewHolder recv = (ReceivedLocationViewHolder) holder;
            bindLocationInfo(recv.tvLocationTitle, recv.tvLocationAddress, msg);
            recv.tvTime.setText(formatTimestamp(msg.getCreatedAt()));
            bindReactions(recv.tvReaction, msg);
            loadMediaImage(recv.ivMapThumbnail, msg);
            setupLongClick(recv.llBubble, msg);
            if (recv.llBubble != null) {
                recv.llBubble.setOnClickListener(v -> {
                    if (mediaClickListener != null && !msg.isDeleted()) {
                        mediaClickListener.onMediaClick(msg);
                    }
                });
            }
        } else if (holder instanceof SentViewHolder) {
            Message msg = item.message;
            SentViewHolder sent = (SentViewHolder) holder;
            if (msg.isDeleted()) {
                sent.tvContent.setText(R.string.chat_message_deleted);
                sent.tvContent.setAlpha(0.6f);
            } else {
                sent.tvContent.setText(msg.getContent());
                sent.tvContent.setAlpha(1.0f);
            }
            sent.tvTime.setText(formatTimestamp(msg.getCreatedAt()));
            bindStatus(sent.imgStatus, msg);
            bindReactions(sent.tvReaction, msg);
            setupLongClick(sent.llBubble != null ? sent.llBubble : sent.itemView, msg);
        } else if (holder instanceof ReceivedViewHolder) {
            Message msg = item.message;
            ReceivedViewHolder recv = (ReceivedViewHolder) holder;
            if (msg.isDeleted()) {
                recv.tvContent.setText(R.string.chat_message_deleted);
                recv.tvContent.setAlpha(0.6f);
            } else {
                recv.tvContent.setText(msg.getContent());
                recv.tvContent.setAlpha(1.0f);
            }
            recv.tvTime.setText(formatTimestamp(msg.getCreatedAt()));
            bindReactions(recv.tvReaction, msg);
            setupLongClick(recv.llBubble != null ? recv.llBubble : recv.itemView, msg);
        }
    }

    private static String formatDuration(Object seconds) {
        if (!(seconds instanceof Number)) return "0:00";
        long total = ((Number) seconds).longValue();
        return String.format(Locale.getDefault(), "%d:%02d", total / 60, total % 60);
    }

    private static String formatFileSize(Object bytes) {
        if (!(bytes instanceof Number)) return "";
        double b = ((Number) bytes).doubleValue();
        if (b < 1024) return ((long) b) + " B";
        if (b < 1024 * 1024) return String.format(Locale.getDefault(), "%.0f KB", b / 1024);
        return String.format(Locale.getDefault(), "%.1f MB", b / (1024 * 1024));
    }

    private void bindVoiceInfo(TextView tvDuration, Message msg) {
        java.util.Map<String, Object> meta = msg.getFileMetaData();
        tvDuration.setText(formatDuration(meta != null ? meta.get("duration") : null));
    }

    private void bindFileInfo(TextView tvFileName, TextView tvSubDetails, Message msg) {
        java.util.Map<String, Object> meta = msg.getFileMetaData();
        String name = (meta != null && meta.get("fileName") != null)
                ? String.valueOf(meta.get("fileName"))
                : tvFileName.getContext().getString(R.string.chat_file_default_name);
        tvFileName.setText(name);

        int dot = name.lastIndexOf('.');
        String ext = (dot >= 0 && dot < name.length() - 1)
                ? name.substring(dot + 1).toUpperCase(Locale.ROOT) : "";
        String size = formatFileSize(meta != null ? meta.get("fileSize") : null);

        String sub;
        if (ext.isEmpty()) sub = size;
        else if (size.isEmpty()) sub = ext;
        else sub = ext + " · " + size;
        tvSubDetails.setText(sub);
    }

    private void bindLocationInfo(TextView tvTitle, TextView tvAddress, Message msg) {
        if (tvTitle == null || tvAddress == null) return;

        String caption = msg.getCaption();
        if (caption != null && !caption.trim().isEmpty()) {
            tvTitle.setText(caption);
        } else {
            tvTitle.setText(tvTitle.getContext().getString(R.string.chat_location_default_title));
        }

        java.util.Map<String, Object> meta = msg.getFileMetaData();
        Object address = meta != null ? meta.get("address") : null;
        String addressText = address != null ? String.valueOf(address) : "";

        tvAddress.setText(addressText);
        tvAddress.setVisibility(addressText.trim().isEmpty() ? View.GONE : View.VISIBLE);
    }

    private void bindMediaContent(TextView tvContent, Message msg) {
        if (tvContent == null || msg == null) return;
        if (msg.isDeleted()) {
            tvContent.setText(R.string.chat_message_deleted);
            tvContent.setAlpha(0.6f);
            tvContent.setVisibility(View.VISIBLE);
            return;
        }

        tvContent.setAlpha(1.0f);
        String captionText = msg.getCaption();
        if (captionText != null && !captionText.trim().isEmpty()) {
            tvContent.setText(captionText);
            tvContent.setVisibility(View.VISIBLE);
        } else {
            tvContent.setVisibility(View.GONE);
        }
    }

    private void loadMediaImage(ImageView imageView, Message msg) {
        if (imageView == null || msg == null) return;
        if (msg.isDeleted()) {
            imageView.setVisibility(View.GONE);
            return;
        }

        imageView.setVisibility(View.VISIBLE);
        String mediaUrl = msg.getContent();

        if (mediaUrl != null && !mediaUrl.trim().isEmpty()) {
            Glide.with(imageView.getContext())
                    .load(mediaUrl)
                    .placeholder(R.drawable.bg_media_placeholder)
                    .error(R.drawable.bg_media_placeholder)
                    .into(imageView);
        } else {
            Glide.with(imageView.getContext()).clear(imageView);
            imageView.setImageResource(R.drawable.bg_media_placeholder);
        }

        imageView.setOnClickListener(v -> {
            if (mediaClickListener != null && !msg.isDeleted()) {
                mediaClickListener.onMediaClick(msg);
            }
        });
        setupLongClick(imageView, msg);
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

    static class SentVoiceViewHolder extends RecyclerView.ViewHolder {
        View llBubble;
        ImageView ivPlayButton, ivWaveform, imgStatus;
        TextView tvDuration, tvTime, tvReaction;

        SentVoiceViewHolder(@NonNull View itemView) {
            super(itemView);
            llBubble = itemView.findViewById(R.id.ll_bubble);
            ivPlayButton = itemView.findViewById(R.id.ivPlayButton);
            ivWaveform = itemView.findViewById(R.id.ivWaveform);
            tvDuration = itemView.findViewById(R.id.tvDuration);
            tvTime = itemView.findViewById(R.id.tvMessageTime);
            tvReaction = itemView.findViewById(R.id.tvReaction);
            imgStatus = itemView.findViewById(R.id.imgStatus);
        }
    }

    static class ReceivedVoiceViewHolder extends RecyclerView.ViewHolder {
        View llBubble;
        ImageView ivPlayButton, ivWaveform;
        TextView tvDuration, tvTime, tvReaction;

        ReceivedVoiceViewHolder(@NonNull View itemView) {
            super(itemView);
            llBubble = itemView.findViewById(R.id.ll_bubble);
            ivPlayButton = itemView.findViewById(R.id.ivPlayButton);
            ivWaveform = itemView.findViewById(R.id.ivWaveform);
            tvDuration = itemView.findViewById(R.id.tvDuration);
            tvTime = itemView.findViewById(R.id.tvMessageTime);
            tvReaction = itemView.findViewById(R.id.tvReaction);
        }
    }

    static class SentFileViewHolder extends RecyclerView.ViewHolder {
        View llBubble;
        ImageView ivFileIcon, ivDownloadButton, imgStatus;
        TextView tvFileName, tvSubDetails, tvTime, tvReaction;

        SentFileViewHolder(@NonNull View itemView) {
            super(itemView);
            llBubble = itemView.findViewById(R.id.ll_bubble);
            ivFileIcon = itemView.findViewById(R.id.ivFileIcon);
            ivDownloadButton = itemView.findViewById(R.id.ivDownloadButton);
            tvFileName = itemView.findViewById(R.id.tvFileName);
            tvSubDetails = itemView.findViewById(R.id.tvSubDetails);
            tvTime = itemView.findViewById(R.id.tvMessageTime);
            tvReaction = itemView.findViewById(R.id.tvReaction);
            imgStatus = itemView.findViewById(R.id.imgStatus);
        }
    }

    static class ReceivedFileViewHolder extends RecyclerView.ViewHolder {
        View llBubble;
        ImageView ivFileIcon, ivDownloadButton;
        TextView tvFileName, tvSubDetails, tvTime, tvReaction;

        ReceivedFileViewHolder(@NonNull View itemView) {
            super(itemView);
            llBubble = itemView.findViewById(R.id.ll_bubble);
            ivFileIcon = itemView.findViewById(R.id.ivFileIcon);
            ivDownloadButton = itemView.findViewById(R.id.ivDownloadButton);
            tvFileName = itemView.findViewById(R.id.tvFileName);
            tvSubDetails = itemView.findViewById(R.id.tvSubDetails);
            tvTime = itemView.findViewById(R.id.tvMessageTime);
            tvReaction = itemView.findViewById(R.id.tvReaction);
        }
    }

    static class SentLocationViewHolder extends RecyclerView.ViewHolder {
        View llBubble;
        ImageView ivMapThumbnail, imgStatus;
        TextView tvLocationTitle, tvLocationAddress, tvTime, tvReaction;

        SentLocationViewHolder(@NonNull View itemView) {
            super(itemView);
            llBubble = itemView.findViewById(R.id.ll_bubble);
            ivMapThumbnail = itemView.findViewById(R.id.ivMapThumbnail);
            tvLocationTitle = itemView.findViewById(R.id.tvLocationTitle);
            tvLocationAddress = itemView.findViewById(R.id.tvLocationAddress);
            tvTime = itemView.findViewById(R.id.tvMessageTime);
            tvReaction = itemView.findViewById(R.id.tvReaction);
            imgStatus = itemView.findViewById(R.id.imgStatus);
        }
    }

    static class ReceivedLocationViewHolder extends RecyclerView.ViewHolder {
        View llBubble;
        ImageView ivMapThumbnail;
        TextView tvLocationTitle, tvLocationAddress, tvTime, tvReaction;

        ReceivedLocationViewHolder(@NonNull View itemView) {
            super(itemView);
            llBubble = itemView.findViewById(R.id.ll_bubble);
            ivMapThumbnail = itemView.findViewById(R.id.ivMapThumbnail);
            tvLocationTitle = itemView.findViewById(R.id.tvLocationTitle);
            tvLocationAddress = itemView.findViewById(R.id.tvLocationAddress);
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
