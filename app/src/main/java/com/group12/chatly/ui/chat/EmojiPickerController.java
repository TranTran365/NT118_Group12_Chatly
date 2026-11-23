package com.group12.chatly.ui.chat;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.group12.chatly.R;
import com.group12.chatly.multimedia.EmojiData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EmojiPickerController {

    private static final String PREFS = "emoji_prefs";
    private static final String KEY_RECENT = "recent";
    private static final int MAX_RECENT = 24;

    private final View root;
    private final EditText target;
    private final SharedPreferences prefs;
    private final EmojiAdapter adapter = new EmojiAdapter(this::onEmojiClicked);

    private final TextView tabEmoji, tabStickers, tvSectionTitle, tvCategoryName, tvStickersSoon;
    private final View layoutContent;
    private final View[] catButtons;
    private int currentCat = 0;

    public EmojiPickerController(View root, EditText target) {
        this.root = root;
        this.target = target;
        this.prefs = root.getContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);

        tabEmoji = root.findViewById(R.id.tabEmoji);
        tabStickers = root.findViewById(R.id.tabStickers);
        tvSectionTitle = root.findViewById(R.id.tvEmojiSectionTitle);
        tvCategoryName = root.findViewById(R.id.tvEmojiCategoryName);
        tvStickersSoon = root.findViewById(R.id.tvStickersSoon);
        layoutContent = root.findViewById(R.id.layoutEmojiContent);

        RecyclerView rcv = root.findViewById(R.id.rcvEmoji);
        rcv.setLayoutManager(new GridLayoutManager(root.getContext(), 6));
        rcv.setAdapter(adapter);

        catButtons = new View[]{
                root.findViewById(R.id.catRecent),
                root.findViewById(R.id.catSmileys),
                root.findViewById(R.id.catHearts),
                root.findViewById(R.id.catFood),
                root.findViewById(R.id.catFlags)
        };
        for (int i = 0; i < catButtons.length; i++) {
            final int idx = i;
            catButtons[i].setOnClickListener(v -> selectCategory(idx));
        }

        tabEmoji.setOnClickListener(v -> selectTab(true));
        tabStickers.setOnClickListener(v -> selectTab(false));
        root.findViewById(R.id.btnEmojiBackspace).setOnClickListener(v -> backspace());

        selectTab(true);
        selectCategory(0);
    }

    public boolean isShowing() {
        return root.getVisibility() == View.VISIBLE;
    }

    public void show() {
        selectCategory(currentCat); // refresh recent
        root.setVisibility(View.VISIBLE);
    }

    public void hide() {
        root.setVisibility(View.GONE);
    }

    private void selectTab(boolean emoji) {
        tabEmoji.setSelected(emoji);
        tabStickers.setSelected(!emoji);
        layoutContent.setVisibility(emoji ? View.VISIBLE : View.GONE);
        tvStickersSoon.setVisibility(emoji ? View.GONE : View.VISIBLE);
    }

    private void selectCategory(int idx) {
        currentCat = idx;
        for (int i = 0; i < catButtons.length; i++) {
            catButtons[i].setSelected(i == idx);
        }
        tvSectionTitle.setText(EmojiData.SECTION_TITLES[idx]);
        tvCategoryName.setText(EmojiData.RIGHT_LABELS[idx]);
        adapter.submit(idx == 0 ? loadRecent() : Arrays.asList(EmojiData.EMOJIS[idx]));
    }

    private void onEmojiClicked(String emoji) {
        int s = Math.max(target.getSelectionStart(), 0);
        int e = Math.max(target.getSelectionEnd(), 0);
        target.getText().replace(Math.min(s, e), Math.max(s, e), emoji);
        saveRecent(emoji);
    }

    private void backspace() {
        target.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL));
        target.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DEL));
    }

    private List<String> loadRecent() {
        String saved = prefs.getString(KEY_RECENT, null);
        if (TextUtils.isEmpty(saved)) {
            return new ArrayList<>(Arrays.asList(EmojiData.DEFAULT_RECENT));
        }
        return new ArrayList<>(Arrays.asList(saved.split("\\|")));
    }

    private void saveRecent(String emoji) {
        List<String> list = loadRecent();
        list.remove(emoji);
        list.add(0, emoji);
        while (list.size() > MAX_RECENT) list.remove(list.size() - 1);
        prefs.edit().putString(KEY_RECENT, TextUtils.join("|", list)).apply();
    }

    // ---- Adapter ----
    static class EmojiAdapter extends RecyclerView.Adapter<EmojiAdapter.VH> {
        interface OnClick { void onClick(String emoji); }

        private final List<String> items = new ArrayList<>();
        private final OnClick listener;

        EmojiAdapter(OnClick listener) { this.listener = listener; }

        void submit(List<String> list) {
            items.clear();
            items.addAll(list);
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_emoji, parent, false);
            return new VH((TextView) v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {
            String emoji = items.get(position);
            holder.tv.setText(emoji);
            holder.tv.setOnClickListener(v -> listener.onClick(emoji));
        }

        @Override
        public int getItemCount() { return items.size(); }

        static class VH extends RecyclerView.ViewHolder {
            final TextView tv;
            VH(TextView tv) { super(tv); this.tv = tv; }
        }
    }
}
