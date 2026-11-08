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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import com.group12.chatly.R;
import com.group12.chatly.data.model.Contact;
import com.group12.chatly.data.model.User;
import com.group12.chatly.data.repository.AuthCallback;
import com.group12.chatly.data.repository.AuthRepository;
import com.group12.chatly.data.repository.ChatRepository;
import com.group12.chatly.multimedia.call.ZegoCallHelper;
import com.group12.chatly.ui.chat.ChatActivity;
import com.zegocloud.uikit.prebuilt.call.invite.widget.ZegoSendCallInvitationButton;

import java.text.Collator;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class ContactsFragment extends Fragment {

    public static final int FILTER_ALL = 0;
    public static final int FILTER_ONLINE = 1;
    public static final int FILTER_REQUESTS = 2;

    private TextView tvAlphabetIndex;
    private View[] filterTabs;
    private int selectedFilter = FILTER_ALL;

    private RecyclerView rcvContacts;
    private RecyclerView rcvContactsOnline;
    private EditText etContactsSearch;

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

        loadContactsData();
        selectFilter(selectedFilter);
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

    private void loadContactsData() {
        String myUid = AuthRepository.getInstance().getCurrentUserId();
        ChatRepository.getInstance().getAllUsers(new AuthCallback<List<User>>() {
            @Override
            public void onSuccess(List<User> users) {
                allContacts.clear();
                onlineContactsList.clear();

                List<Contact> rawContacts = new ArrayList<>();
                for (User user : users) {
                    String uid = user.getUserId();
                    if (TextUtils.isEmpty(uid) || uid.equals(myUid)) continue;

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

                updateOnlineHeader();

                if (onlineAdapter != null) {
                    onlineAdapter.setOnlineContacts(onlineContactsList);
                }

                filterData(etContactsSearch != null ? etContactsSearch.getText().toString() : "");
            }

            @Override
            public void onFailure(String errorMessage) {
                allContacts.clear();
                onlineContactsList.clear();
                updateOnlineHeader();
                if (contactsAdapter != null) {
                    contactsAdapter.setContacts(new ArrayList<>());
                }
            }
        });
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
    }
}
