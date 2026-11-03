package com.group12.chatly.ui.chat;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import com.group12.chatly.R;
import com.group12.chatly.data.model.Message;
import com.group12.chatly.data.repository.AuthCallback;
import com.group12.chatly.data.repository.ChatRepository;
import com.group12.chatly.multimedia.EmojiHelper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MessageActionsDialogFragment extends DialogFragment {

    public static final String TAG = "MessageActionsDialogFragment";

    private static final String ARG_CHAT_ID = "arg_chat_id";
    private static final String ARG_CURRENT_USER_ID = "arg_current_user_id";
    private static final String ARG_MESSAGE_ID = "arg_message_id";
    private static final String ARG_SENDER_ID = "arg_sender_id";
    private static final String ARG_TYPE = "arg_type";
    private static final String ARG_CONTENT = "arg_content";
    private static final String ARG_DELETED = "arg_deleted";
    private static final String ARG_TIME = "arg_time";
    private static final String ARG_MY_REACTION = "arg_my_reaction";

    private String chatId;
    private String currentUserId;
    private String messageId;
    private String senderId;
    private String type;
    private String content;
    private boolean deleted;
    private long timeMillis;
    private String myReaction;

    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

    /** Chỉ truyền giá trị đơn giản qua Bundle (Timestamp của Firebase không Serializable). */
    public static MessageActionsDialogFragment newInstance(String chatId, Message message, String currentUserId) {
        MessageActionsDialogFragment fragment = new MessageActionsDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_CHAT_ID, chatId);
        args.putString(ARG_CURRENT_USER_ID, currentUserId);
        args.putString(ARG_MESSAGE_ID, message.getMessageId());
        args.putString(ARG_SENDER_ID, message.getSenderId());

        String t = message.getType();
        boolean isMedia = Message.TYPE_IMAGE.equals(t) || Message.TYPE_VIDEO.equals(t);
        args.putString(ARG_TYPE, t);
        // Tin ảnh/video: dùng caption (URL không nên hiện ra cho người dùng)
        args.putString(ARG_CONTENT, isMedia ? message.getCaption() : message.getContent());

        args.putBoolean(ARG_DELETED, message.isDeleted());
        args.putLong(ARG_TIME, message.getCreatedAt() == null
                ? 0 : message.getCreatedAt().toDate().getTime());
        if (message.getReactions() != null) {
            args.putString(ARG_MY_REACTION, message.getReactions().get(currentUserId));
        }
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, android.R.style.Theme_Translucent_NoTitleBar);
        Bundle args = getArguments();
        if (args != null) {
            chatId = args.getString(ARG_CHAT_ID);
            currentUserId = args.getString(ARG_CURRENT_USER_ID);
            messageId = args.getString(ARG_MESSAGE_ID);
            senderId = args.getString(ARG_SENDER_ID);
            type = args.getString(ARG_TYPE);
            content = args.getString(ARG_CONTENT);
            deleted = args.getBoolean(ARG_DELETED, false);
            timeMillis = args.getLong(ARG_TIME, 0);
            myReaction = args.getString(ARG_MY_REACTION);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.message_actions, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Tin đã thu hồi thì không có menu
        if (messageId == null || deleted) {
            dismiss();
            return;
        }

        boolean isMine = currentUserId != null && currentUserId.equals(senderId);

        view.findViewById(R.id.root_message_actions).setOnClickListener(v -> dismiss());

        // Bản sao tin nhắn: tin của mình -> bong bóng xanh, căn phải
        LinearLayout llPreview = view.findViewById(R.id.ll_message_preview);
        TextView tvMessage = view.findViewById(R.id.tv_action_message);
        TextView tvTime = view.findViewById(R.id.tv_action_time);
        tvMessage.setText(previewText());
        tvTime.setText(timeMillis > 0 ? timeFormat.format(new Date(timeMillis)) : "");
        if (isMine) {
            llPreview.setBackgroundResource(R.drawable.bg_msg_sent);
            tvMessage.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));
            tvTime.setTextColor(ContextCompat.getColor(requireContext(), R.color.msg_tick_sent));
            LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) llPreview.getLayoutParams();
            lp.gravity = Gravity.END;
            llPreview.setLayoutParams(lp);
        }

        buildReactionBar(view);

        View actionCopy = view.findViewById(R.id.action_copy);
        actionCopy.setVisibility(TextUtils.isEmpty(content) ? View.GONE : View.VISIBLE);
        actionCopy.setOnClickListener(v -> copyText());

        // Chỉ thu hồi được tin của chính mình
        View actionDelete = view.findViewById(R.id.action_delete);
        actionDelete.setVisibility(isMine ? View.VISIBLE : View.GONE);
        actionDelete.setOnClickListener(v -> deleteMessage());

        view.findViewById(R.id.action_reply).setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Reply (coming soon)", Toast.LENGTH_SHORT).show();
            dismiss();
        });
    }

    /** Tin chữ/caption: hiện nội dung; ảnh/video không caption: hiện "Photo"/"Video". */
    private String previewText() {
        if (!TextUtils.isEmpty(content)) return content;
        if (Message.TYPE_IMAGE.equals(type)) return getString(R.string.chats_preview_photo);
        if (Message.TYPE_VIDEO.equals(type)) return getString(R.string.chats_preview_video);
        return "";
    }

    /** Đổ danh sách QUICK_REACTIONS của TV3 vào thanh reaction; emoji đang chọn được tô nền. */
    private void buildReactionBar(View view) {
        LinearLayout llReactionBar = view.findViewById(R.id.ll_reaction_bar);
        if (llReactionBar == null || getContext() == null) return;
        llReactionBar.removeAllViews();

        TypedValue outValue = new TypedValue();
        requireContext().getTheme().resolveAttribute(
                android.R.attr.selectableItemBackgroundBorderless, outValue, true);
        int rippleBg = outValue.resourceId;
        int heightPx = Math.round(48 * getResources().getDisplayMetrics().density);

        for (String emoji : EmojiHelper.QUICK_REACTIONS) {
            TextView tv = new TextView(requireContext());
            tv.setLayoutParams(new LinearLayout.LayoutParams(0, heightPx, 1f));
            tv.setGravity(Gravity.CENTER);
            tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);
            tv.setText(emoji);
            tv.setBackgroundResource(emoji.equals(myReaction) ? R.drawable.bg_reaction_selected : rippleBg);
            tv.setOnClickListener(v -> sendReaction(emoji));
            llReactionBar.addView(tv);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT);
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
    }

    /** Bấm emoji đang chọn thì gỡ reaction (truyền null), bấm emoji khác thì đổi. */
    private void sendReaction(String emoji) {
        if (chatId == null || messageId == null) {
            dismiss();
            return;
        }
        boolean removing = emoji.equals(myReaction);
        String newValue = removing ? null : emoji;

        ChatRepository.getInstance().setReaction(chatId, messageId, currentUserId, newValue,
                new AuthCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        if (!removing && getContext() != null) {
                            Toast.makeText(getContext(), "Reacted " + emoji, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(String errorMessage) {
                        if (getContext() != null) {
                            Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
        dismiss();
    }

    private void copyText() {
        if (getContext() != null && content != null) {
            ClipboardManager clipboard =
                    (ClipboardManager) requireContext().getSystemService(Context.CLIPBOARD_SERVICE);
            if (clipboard != null) {
                clipboard.setPrimaryClip(ClipData.newPlainText("Copied Message", content));
                Toast.makeText(getContext(), "Copied to clipboard!", Toast.LENGTH_SHORT).show();
            }
        }
        dismiss();
    }

    private void deleteMessage() {
        if (chatId == null || messageId == null) {
            dismiss();
            return;
        }
        ChatRepository.getInstance().deleteMessage(chatId, messageId, new AuthCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Message deleted", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(String errorMessage) {
                if (getContext() != null) {
                    Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();
                }
            }
        });
        dismiss();
    }
}
