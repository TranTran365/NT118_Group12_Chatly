package com.group12.chatly.ui.welcome;

import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.group12.chatly.R;
import com.group12.chatly.ui.auth.RegisterActivity;

public class WelcomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        // Chữ "chat." màu vàng
        TextView tvHeadline = findViewById(R.id.tvHeadline);
        highlight(tvHeadline, ContextCompat.getColor(this, R.color.accent));

        findViewById(R.id.btnCreateAccount).setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));

        findViewById(R.id.btnHaveAccount).setOnClickListener(v -> {
            // TODO: chuyển sang LoginActivity khi có màn đăng nhập
        });
    }

    /** Đoạn nằm trong thẻ <b> của string được đổi thành màu, không in đậm thêm. */
    private void highlight(TextView tv, int color) {
        SpannableString s = new SpannableString(tv.getText());
        for (StyleSpan span : s.getSpans(0, s.length(), StyleSpan.class)) {
            int start = s.getSpanStart(span);
            int end = s.getSpanEnd(span);
            s.removeSpan(span);
            s.setSpan(new ForegroundColorSpan(color), start, end,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        tv.setText(s);
    }
}