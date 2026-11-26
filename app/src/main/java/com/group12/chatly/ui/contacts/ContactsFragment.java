package com.group12.chatly.ui.contacts;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import android.widget.Toast;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.group12.chatly.data.repository.ContactRepository;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import com.group12.chatly.R;
import com.group12.chatly.data.model.Contact;
import com.group12.chatly.data.model.User;
import com.group12.chatly.data.repository.AuthCallback;
import com.group12.chatly.multimedia.call.ZegoCallHelper;
import com.group12.chatly.ui.chat.ChatActivity;
import com.group12.chatly.viewmodel.AuthState;
import com.group12.chatly.viewmodel.ContactViewModel;
import com.zegocloud.uikit.prebuilt.call.invite.widget.ZegoSendCallInvitationButton;

import java.text.Collator;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class ContactsFragment extends Fragment implements ContactActionsDialogFragment.Listener {

    public static final int FILTER_ALL = 0;
    public static final int FILTER_ONLINE = 1;
    public static final int FILTER_REQUESTS = 2;

    private TextView tvAlphabetIndex;
    private View[] filterTabs;
    private int selectedFilter = FILTER_ALL;

    private RecyclerView rcvContacts;
    private RecyclerView rcvContactsOnline;
    private EditText etContactsSearch;

    private View layoutContactsEmpty, layoutContactsFilters, frameContactsList, btnAddFriend;
    private TextView tvContactsTagline;

    private ContactViewModel contactViewModel;

    private ContactsAdapter contactsAdapter;
    private ContactsOnlineAdapter onlineAdapter;

    private final List<Contact> allContacts = new ArrayList<>();
    private final List<Contact> onlineContactsList = new ArrayList<>();

    public ContactsFragment() {
        super(R.layout.fragment_contacts);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvAlphabetIndex = view.findViewById(R.id.tvAlphabetIndex);
        rcvContacts = view.findViewById(R.id.rcvContacts);
        rcvContactsOnline = view.findViewById(R.id.rcvContactsOnline);
        etContactsSearch = view.findViewById(R.id.etContactsSearch);

        layoutContactsEmpty = view.findViewById(R.id.layoutContactsEmpty);
        layoutContactsFilters = view.findViewById(R.id.layoutContactsFilters);
        frameContactsList = view.findViewById(R.id.frameContactsList);
        btnAddFriend = view.findViewById(R.id.btnAddFriend);
        tvContactsTagline = view.findViewById(R.id.tvContactsTagline);

        View.OnClickListener openAddFriend = v ->
                startActivity(new Intent(requireContext(), AddFriendActivity.class));
        view.findViewById(R.id.btnEmptyAddFriend).setOnClickListener(openAddFriend);
        btnAddFriend.setOnClickListener(openAddFriend);
        view.findViewById(R.id.btnEmptyInvite).setOnClickListener(v -> inviteFriends());

        // Ẩn các banner chưa có nguồn dữ liệu thật (Requests)
        View layoutRequestsBanner = view.findViewById(R.id.layoutRequestsBanner);
        if (layoutRequestsBanner != null) layoutRequestsBanner.setVisibility(View.GONE);

        View tvRequestsBadge = view.findViewById(R.id.tvContactsRequestsBadge);
        if (tvRequestsBadge != null) tvRequestsBadge.setVisibility(View.GONE);

        View layoutFilterRequests = view.findViewById(R.id.layoutContactsFilterRequests);
        if (layoutFilterRequests != null) layoutFilterRequests.setVisibility(View.GONE);

        setupAdapters();
        setupFilterTabs(view);
        setupSearch();

        contactViewModel = new ViewModelProvider(this).get(ContactViewModel.class);
        observeFriends();
        selectFilter(selectedFilter);
    }

    @Override
    public void onStart() {
        super.onStart();
        // Realtime listener của TV1: chỉ lấy bạn bè đã accepted
        contactViewModel.startListeningToFriends();
    }

    @Override
    public void onStop() {
        super.onStop();
        contactViewModel.stopListening();
    }

    private void observeFriends() {
        contactViewModel.getFriendsListState().observe(getViewLifecycleOwner(), state -> {
            if (state == null) return;
            if (state.getStatus() == AuthState.Status.SUCCESS) {
                bindFriends(state.getData() != null ? state.getData() : new ArrayList<>());
            } else if (state.getStatus() == AuthState.Status.ERROR) {
                bindFriends(new ArrayList<>());
                if (isAdded() && state.getErrorMessage() != null) {
                    Toast.makeText(requireContext(), state.getErrorMessage(), Toast.LENGTH_SHORT).show();
                }
            }
            // IDLE / LOADING: giữ nguyên list hiện tại, tránh nháy empty state
        });
    }

    private void setupAdapters() {
        // Contacts list adapter
        contactsAdapter = new ContactsAdapter();
        contactsAdapter.setOnContactActionListener(new ContactsAdapter.OnContactActionListener() {
            @Override
            public void onContactMessageClick(Contact contact) {
                openChat(contact);
            }

            @Override
            public void onContactVoiceClick(Contact contact) {
                startZegoCall(contact, false);
            }

            @Override
            public void onContactVideoClick(Contact contact) {
                startZegoCall(contact, true);
            }

            @Override
            public void onContactLongClick(Contact contact) {
                ContactActionsDialogFragment.newInstance(contact)
                        .show(getChildFragmentManager(), ContactActionsDialogFragment.TAG);
            }
        });

        if (rcvContacts != null) {
            rcvContacts.setLayoutManager(new LinearLayoutManager(requireContext()));
            rcvContacts.setAdapter(contactsAdapter);

            if (rcvContacts.getItemAnimator() instanceof SimpleItemAnimator) {
                ((SimpleItemAnimator) rcvContacts.getItemAnimator()).setSupportsChangeAnimations(false);
            }
        }

        // Online contacts adapter
        onlineAdapter = new ContactsOnlineAdapter();
        onlineAdapter.setOnOnlineContactClickListener(this::openChat);

        if (rcvContactsOnline != null) {
            rcvContactsOnline.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
            rcvContactsOnline.setAdapter(onlineAdapter);
        }
    }

    @Override
    public void onContactAction(Contact contact, ContactActionsDialogFragment.Action action) {
        if (contact == null) return;
        switch (action) {
            case MESSAGE:
                openChat(contact);
                break;
            case CALL:
                startZegoCall(contact, false);
                break;
            case PROFILE:
                ContactProfileBottomSheet.newInstance(contact)
                        .show(getChildFragmentManager(), ContactProfileBottomSheet.TAG);
                break;
            case BLOCK:
                confirm("Block " + contact.getDisplayName() + "?",
                        "They won't be able to message or call you.",
                        () -> {
                            // TODO: chờ Backend thêm blockUser()
                            Toast.makeText(requireContext(), "Coming soon", Toast.LENGTH_SHORT).show();
                        });
                break;
            case REMOVE:
                confirm("Remove " + contact.getDisplayName() + "?",
                        "You will no longer be friends.",
                        () -> removeContact(contact));
                break;
        }
    }

    private void confirm(String title, String message, Runnable onConfirm) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(title)
                .setMessage(message)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("OK", (d, w) -> onConfirm.run())
                .show();
    }

    private void removeContact(Contact contact) {
        FirebaseUser me = FirebaseAuth.getInstance().getCurrentUser();
        if (me == null || TextUtils.isEmpty(contact.getUserId())) return;

        String friendshipId = ContactRepository.getCompositeFriendshipId(me.getUid(), contact.getUserId());
        ContactRepository.getInstance().removeFriendship(friendshipId, new AuthCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                // Không cần reload: listener realtime trong ContactViewModel tự cập nhật list
            }

            @Override
            public void onFailure(String errorMessage) {
                if (!isAdded()) return;
                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void startZegoCall(Contact contact, boolean isVideo) {
        if (contact == null || TextUtils.isEmpty(contact.getUserId()) || getContext() == null) return;
        ZegoSendCallInvitationButton button = new ZegoSendCallInvitationButton(requireContext());
        if (isVideo) {
            ZegoCallHelper.setupVideoCallButton(button, contact.getUserId(), contact.getDisplayName());
        } else {
            ZegoCallHelper.setupVoiceCallButton(button, contact.getUserId(), contact.getDisplayName());
        }
        button.performClick();
    }

    private void openChat(Contact contact) {
        if (contact == null || TextUtils.isEmpty(contact.getUserId()) || getContext() == null) return;
        Intent intent = new Intent(requireContext(), ChatActivity.class);
        intent.putExtra(ChatActivity.EXTRA_TARGET_USER_ID, contact.getUserId());
        intent.putExtra(ChatActivity.EXTRA_TARGET_USER_NAME, contact.getDisplayName());
        startActivity(intent);
    }

    private void setupFilterTabs(View view) {
        filterTabs = new View[]{
                view.findViewById(R.id.tvContactsFilterAll),
                view.findViewById(R.id.tvContactsFilterOnline),
                view.findViewById(R.id.layoutContactsFilterRequests)
        };
        for (int i = 0; i < filterTabs.length; i++) {
            if (filterTabs[i] == null) continue;
            final int tabIndex = i;
            filterTabs[i].setOnClickListener(v -> selectFilter(tabIndex));
        }
    }

    private void setupSearch() {
        if (etContactsSearch == null) return;
        etContactsSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterData(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void bindFriends(List<User> users) {
        allContacts.clear();
        onlineContactsList.clear();

        List<Contact> rawContacts = new ArrayList<>();
        for (User user : users) {
            String uid = user.getUserId();
            if (TextUtils.isEmpty(uid)) continue;

            String name = TextUtils.isEmpty(user.getDisplayName())
                    ? getString(R.string.chats_unknown_user) : user.getDisplayName();
            String statusText = user.isOnline()
                    ? "online"
                    : (!TextUtils.isEmpty(user.getBio()) ? user.getBio() : "offline");

            Contact contact = new Contact(
                    uid, name, statusText, user.isOnline(),
                    user.getAvatarUrl(), getGroupLetter(name), false
            );
            rawContacts.add(contact);
            if (user.isOnline()) {
                onlineContactsList.add(contact);
            }
        }

        // Sắp xếp theo tên (dùng Collator tiếng Việt)
        Collator collator = Collator.getInstance(new Locale("vi", "VN"));
        Collections.sort(rawContacts, (c1, c2) -> collator.compare(c1.getDisplayName(), c2.getDisplayName()));

        allContacts.addAll(rawContacts);
        setEmptyState(allContacts.isEmpty());

        updateOnlineHeader();

        if (onlineAdapter != null) {
            onlineAdapter.setOnlineContacts(onlineContactsList);
        }

        filterData(etContactsSearch != null ? etContactsSearch.getText().toString() : "");
    }

    private void setEmptyState(boolean empty) {
        int listVisibility = empty ? View.GONE : View.VISIBLE;
        if (layoutContactsEmpty != null) layoutContactsEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
        if (layoutContactsFilters != null) layoutContactsFilters.setVisibility(listVisibility);
        if (frameContactsList != null) frameContactsList.setVisibility(listVisibility);
        if (btnAddFriend != null) btnAddFriend.setVisibility(listVisibility);
        if (tvContactsTagline != null) {
            tvContactsTagline.setText(empty ? R.string.contacts_empty_tagline : R.string.contacts_tagline);
        }
    }

    private void inviteFriends() {
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("text/plain");
        share.putExtra(Intent.EXTRA_TEXT, getString(R.string.contacts_invite_text));
        startActivity(Intent.createChooser(share, getString(R.string.contacts_empty_btn_invite)));
    }

    private void updateOnlineHeader() {
        View view = getView();
        if (view == null) return;

        TextView tvOnlineCount = view.findViewById(R.id.tvContactsOnlineCount);
        if (tvOnlineCount != null) {
            View onlineRow = (View) tvOnlineCount.getParent().getParent();
            if (onlineContactsList.isEmpty()) {
                if (onlineRow != null) onlineRow.setVisibility(View.GONE);
            } else {
                if (onlineRow != null) onlineRow.setVisibility(View.VISIBLE);
                tvOnlineCount.setText(getString(R.string.contacts_online_around, onlineContactsList.size()));
            }
        }
    }

    private void filterData(String query) {
        List<Contact> filtered = new ArrayList<>();
        String q = stripAccents(query.trim());

        String currentLetter = "";
        for (Contact c : allContacts) {
            boolean matchesFilter = (selectedFilter == FILTER_ALL)
                    || (selectedFilter == FILTER_ONLINE && c.isOnline());

            boolean matchesSearch = q.isEmpty() || stripAccents(c.getDisplayName()).contains(q);

            if (matchesFilter && matchesSearch) {
                boolean isNewHeader = !c.getGroupLetter().equals(currentLetter);
                Contact item = new Contact(
                        c.getUserId(), c.getDisplayName(), c.getStatusText(),
                        c.isOnline(), c.getAvatarUrl(), c.getGroupLetter(), isNewHeader
                );
                currentLetter = c.getGroupLetter();
                filtered.add(item);
            }
        }

        if (contactsAdapter != null) {
            contactsAdapter.setContacts(filtered);
        }
    }

    private static String stripAccents(String s) {
        if (s == null) return "";
        String normalized = Normalizer.normalize(s, Normalizer.Form.NFD);
        String plain = normalized.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        return plain.replace('đ', 'd').replace('Đ', 'D').toLowerCase(Locale.getDefault());
    }

    private static String getGroupLetter(String name) {
        if (name == null || name.trim().isEmpty()) return "#";
        String plain = stripAccents(name.trim());
        if (plain.isEmpty()) return "#";
        char first = Character.toUpperCase(plain.charAt(0));
        if (first >= 'A' && first <= 'Z') {
            return String.valueOf(first);
        }
        return "#";
    }

    /** Đổi tab lọc đang chọn. */
    public void selectFilter(int filter) {
        selectedFilter = filter;
        if (filterTabs == null) return;

        for (int i = 0; i < filterTabs.length; i++) {
            if (filterTabs[i] == null) continue;
            final int tabIndex = i;
            setBackgroundKeepPadding(filterTabs[i], tabIndex == filter ? R.drawable.bg_filter_active : 0);
        }

        filterData(etContactsSearch != null ? etContactsSearch.getText().toString() : "");
    }

    public int getSelectedFilter() {
        return selectedFilter;
    }

    private static void setBackgroundKeepPadding(View view, int resId) {
        int left = view.getPaddingLeft();
        int top = view.getPaddingTop();
        int right = view.getPaddingRight();
        int bottom = view.getPaddingBottom();
        view.setBackgroundResource(resId);
        view.setPadding(left, top, right, bottom);
    }

    public void highlightAlphabet(char active) {
        if (tvAlphabetIndex == null) return;

        String text = tvAlphabetIndex.getText().toString();
        int pos = text.indexOf(Character.toUpperCase(active));

        SpannableString spannable = new SpannableString(text);
        if (pos >= 0) {
            spannable.setSpan(
                    new ForegroundColorSpan(ContextCompat.getColor(
                            tvAlphabetIndex.getContext(), R.color.chatly_blue)),
                    pos, pos + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        tvAlphabetIndex.setText(spannable);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        tvAlphabetIndex = null;
        filterTabs = null;
        rcvContacts = null;
        rcvContactsOnline = null;
        etContactsSearch = null;
        layoutContactsEmpty = null;
        layoutContactsFilters = null;
        frameContactsList = null;
        btnAddFriend = null;
        tvContactsTagline = null;
    }
}