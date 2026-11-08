package com.group12.chatly.ui.contacts;

import android.content.Context;
import android.transition.TransitionManager;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.group12.chatly.R;
import com.group12.chatly.data.model.Contact;

import java.util.ArrayList;
import java.util.List;

public class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.ContactViewHolder> {

    public interface OnContactActionListener {
        void onContactMessageClick(Contact contact);
        void onContactVoiceClick(Contact contact);
        void onContactVideoClick(Contact contact);
    }

    private final List<Contact> contacts = new ArrayList<>();
    private OnContactActionListener listener;
    private int swipedPosition = RecyclerView.NO_POSITION;

    public ContactsAdapter() {
    }

    public void setOnContactActionListener(OnContactActionListener listener) {
        this.listener = listener;
    }

    public void setContacts(List<Contact> newContacts) {
        this.contacts.clear();
        if (newContacts != null) {
            this.contacts.addAll(newContacts);
        }
        this.swipedPosition = RecyclerView.NO_POSITION;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_contact_row, parent, false);
        return new ContactViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactViewHolder holder, int position) {
        Contact contact = contacts.get(position);
        holder.bind(contact, position);
    }

    @Override
    public int getItemCount() {
        return contacts.size();
    }

    class ContactViewHolder extends RecyclerView.ViewHolder {

        private final TextView tvContactLetter;
        private final ViewGroup layoutContactRow;
        private final TextView tvContactInitials;
        private final ImageView ivContactAvatar;
        private final View vContactOnlineDot;
        private final TextView tvContactName;
        private final TextView tvContactStatus;
        private final View btnContactMessage;
        private final View layoutContactCallActions;
        private final View btnContactVoice;
        private final View btnContactVideo;
        private final View vContactDivider;

        ContactViewHolder(@NonNull View itemView) {
            super(itemView);
            tvContactLetter = itemView.findViewById(R.id.tvContactLetter);
            layoutContactRow = itemView.findViewById(R.id.layoutContactRow);
            tvContactInitials = itemView.findViewById(R.id.tvContactInitials);
            ivContactAvatar = itemView.findViewById(R.id.ivContactAvatar);
            vContactOnlineDot = itemView.findViewById(R.id.vContactOnlineDot);
            tvContactName = itemView.findViewById(R.id.tvContactName);
            tvContactStatus = itemView.findViewById(R.id.tvContactStatus);
            btnContactMessage = itemView.findViewById(R.id.btnContactMessage);
            layoutContactCallActions = itemView.findViewById(R.id.layoutContactCallActions);
            btnContactVoice = itemView.findViewById(R.id.btnContactVoice);
            btnContactVideo = itemView.findViewById(R.id.btnContactVideo);
            vContactDivider = itemView.findViewById(R.id.vContactDivider);
        }

        void bind(Contact contact, int position) {
            Context context = itemView.getContext();

            // 1. Chữ cái nhóm
            if (contact.isShowGroupHeader()) {
                tvContactLetter.setVisibility(View.VISIBLE);
                tvContactLetter.setText(contact.getGroupLetter());
            } else {
                tvContactLetter.setVisibility(View.GONE);
            }

            // 2. Thông tin contact
            tvContactName.setText(contact.getDisplayName());
            tvContactStatus.setText(contact.getStatusText());
            vContactOnlineDot.setVisibility(contact.isOnline() ? View.VISIBLE : View.GONE);

            // Avatar / Initials
            if (contact.getAvatarUrl() != null && !contact.getAvatarUrl().isEmpty()) {
                ivContactAvatar.setVisibility(View.VISIBLE);
                tvContactInitials.setVisibility(View.GONE);
                Glide.with(context)
                        .load(contact.getAvatarUrl())
                        .placeholder(R.drawable.bg_profile_avatar)
                        .circleCrop()
                        .into(ivContactAvatar);
            } else {
                ivContactAvatar.setVisibility(View.GONE);
                tvContactInitials.setVisibility(View.VISIBLE);
                tvContactInitials.setText(contact.getInitials());
            }

            // 3. Đường kẻ divider
            if (vContactDivider != null) {
                boolean isLastItem = (position == getItemCount() - 1);
                vContactDivider.setVisibility(isLastItem ? View.GONE : View.VISIBLE);
            }

            // 4. Trạng thái Swipe
            boolean isSwiped = (position == swipedPosition);
            updateSwipeState(isSwiped, false);

            // 5. Gestures
            final float density = context.getResources().getDisplayMetrics().density;
            final float swipeDistance = 60 * density;
            final float swipeVelocity = 200 * density;

            GestureDetector gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onSingleTapConfirmed(MotionEvent e) {
                    int pos = getBindingAdapterPosition();
                    if (pos == RecyclerView.NO_POSITION) return false;
                    if (pos == swipedPosition) {
                        collapseSwipe();
                    } else if (listener != null) {
                        listener.onContactMessageClick(contact);
                    }
                    return true;
                }

                @Override
                public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                    if (e1 == null || e2 == null) return false;
                    int pos = getBindingAdapterPosition();
                    if (pos == RecyclerView.NO_POSITION) return false;
                    float diffX = e2.getX() - e1.getX();
                    float diffY = e2.getY() - e1.getY();
                    if (Math.abs(diffX) > Math.abs(diffY)
                            && Math.abs(diffX) > swipeDistance
                            && Math.abs(velocityX) > swipeVelocity) {
                        if (diffX < 0) setSwipedPosition(pos); else collapseSwipe();
                        return true;
                    }
                    return false;
                }
            });

            layoutContactRow.setOnTouchListener((v, event) -> {
                gestureDetector.onTouchEvent(event);
                v.onTouchEvent(event);   // giữ ripple
                return true;
            });

            // 6. Cụm nút hành động
            btnContactMessage.setOnClickListener(v -> {
                if (listener != null) listener.onContactMessageClick(contact);
            });

            btnContactVoice.setOnClickListener(v -> {
                if (listener != null) listener.onContactVoiceClick(contact);
            });

            btnContactVideo.setOnClickListener(v -> {
                if (listener != null) listener.onContactVideoClick(contact);
            });
        }

        private void updateSwipeState(boolean isSwiped, boolean animate) {
            if (animate) {
                TransitionManager.beginDelayedTransition(layoutContactRow);
            }
            if (isSwiped) {
                btnContactMessage.setVisibility(View.GONE);
                layoutContactCallActions.setVisibility(View.VISIBLE);
            } else {
                btnContactMessage.setVisibility(View.VISIBLE);
                layoutContactCallActions.setVisibility(View.GONE);
            }
        }
    }

    private void setSwipedPosition(int position) {
        if (swipedPosition == position) return;
        int prev = swipedPosition;
        swipedPosition = position;
        if (prev != RecyclerView.NO_POSITION) {
            notifyItemChanged(prev);
        }
        notifyItemChanged(swipedPosition);
    }

    private void collapseSwipe() {
        if (swipedPosition != RecyclerView.NO_POSITION) {
            int prev = swipedPosition;
            swipedPosition = RecyclerView.NO_POSITION;
            notifyItemChanged(prev);
        }
    }
}
