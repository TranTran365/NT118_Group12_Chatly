package com.group12.chatly.ui.contacts;

import android.content.Context;
import android.view.LayoutInflater;
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

public class ContactsOnlineAdapter extends RecyclerView.Adapter<ContactsOnlineAdapter.OnlineViewHolder> {

    public interface OnOnlineContactClickListener {
        void onOnlineContactClick(Contact contact);
    }

    private final List<Contact> onlineContacts = new ArrayList<>();
    private OnOnlineContactClickListener listener;

    public ContactsOnlineAdapter() {
    }

    public void setOnOnlineContactClickListener(OnOnlineContactClickListener listener) {
        this.listener = listener;
    }

    public void setOnlineContacts(List<Contact> newContacts) {
        this.onlineContacts.clear();
        if (newContacts != null) {
            this.onlineContacts.addAll(newContacts);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public OnlineViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_contact_online, parent, false);
        return new OnlineViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OnlineViewHolder holder, int position) {
        Contact contact = onlineContacts.get(position);
        holder.bind(contact);
    }

    @Override
    public int getItemCount() {
        return onlineContacts.size();
    }

    class OnlineViewHolder extends RecyclerView.ViewHolder {

        private final TextView tvInitials;
        private final ImageView ivAvatar;
        private final TextView tvName;

        OnlineViewHolder(@NonNull View itemView) {
            super(itemView);
            tvInitials = itemView.findViewById(R.id.tvContactOnlineInitials);
            ivAvatar = itemView.findViewById(R.id.ivContactOnlineAvatar);
            tvName = itemView.findViewById(R.id.tvContactOnlineName);
        }

        void bind(Contact contact) {
            Context context = itemView.getContext();
            tvName.setText(contact.getDisplayName());

            if (contact.getAvatarUrl() != null && !contact.getAvatarUrl().isEmpty()) {
                ivAvatar.setVisibility(View.VISIBLE);
                tvInitials.setVisibility(View.GONE);
                Glide.with(context)
                        .load(contact.getAvatarUrl())
                        .placeholder(R.drawable.bg_profile_avatar)
                        .circleCrop()
                        .into(ivAvatar);
            } else {
                ivAvatar.setVisibility(View.GONE);
                tvInitials.setVisibility(View.VISIBLE);
                tvInitials.setText(contact.getInitials());
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onOnlineContactClick(contact);
            });
        }
    }
}
