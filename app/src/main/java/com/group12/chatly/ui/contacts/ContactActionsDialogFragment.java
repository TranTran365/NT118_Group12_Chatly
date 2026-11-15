package com.group12.chatly.ui.contacts;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.group12.chatly.R;
import com.group12.chatly.data.model.Contact;

public class ContactActionsDialogFragment extends DialogFragment {

    public static final String TAG = "ContactActionsDialog";
    private static final String ARG_CONTACT = "arg_contact";

    public enum Action { MESSAGE, CALL, PROFILE, BLOCK, REMOVE }

    public interface Listener {
        void onContactAction(Contact contact, Action action);
    }

    private Contact contact;

    public static ContactActionsDialogFragment newInstance(Contact contact) {
        ContactActionsDialogFragment fragment = new ContactActionsDialogFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_CONTACT, contact);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, android.R.style.Theme_Translucent_NoTitleBar);
        if (getArguments() != null) {
            contact = (Contact) getArguments().getSerializable(ARG_CONTACT);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_contact_actions, container, false);

        // Bấm ra ngoài card thì đóng
        view.findViewById(R.id.rootContactActions).setOnClickListener(v -> dismiss());

        bindAction(view, R.id.actionMessage, Action.MESSAGE);
        bindAction(view, R.id.actionCall, Action.CALL);
        bindAction(view, R.id.actionProfile, Action.PROFILE);
        bindAction(view, R.id.actionBlock, Action.BLOCK);
        bindAction(view, R.id.actionRemove, Action.REMOVE);
        return view;
    }

    private void bindAction(View root, int viewId, Action action) {
        root.findViewById(viewId).setOnClickListener(v -> {
            Fragment parent = getParentFragment();
            if (parent instanceof Listener) {
                ((Listener) parent).onContactAction(contact, action);
            }
            dismiss();
        });
    }
}