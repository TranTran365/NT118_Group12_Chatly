package com.group12.chatly.ui.contacts;

import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.group12.chatly.R;

public class ContactsFragment extends Fragment {

    private TextView tvAlphabetIndex;

    public ContactsFragment() {
        super(R.layout.fragment_contacts);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvAlphabetIndex = view.findViewById(R.id.tvAlphabetIndex);


        // highlightAlphabet('L');
    }

    /**
     * Tô xanh chữ cái đang chọn trong cột A-Z, các chữ còn lại giữ màu mặc định.
     * Truyền ký tự không có trong cột (ví dụ '\0') để bỏ tô.
     */
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
    }
}