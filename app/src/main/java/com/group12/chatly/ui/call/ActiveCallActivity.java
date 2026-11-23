package com.group12.chatly.ui.call;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.group12.chatly.R;
import com.group12.chatly.data.model.User;

import java.util.Locale;

public class ActiveCallActivity extends AppCompatActivity {

    private final Handler handler = new Handler(Looper.getMainLooper());
    private TextView tvStatus;
    private long startTime;

    private final Runnable ticker = new Runnable() {
        @Override
        public void run() {
            long sec = (SystemClock.elapsedRealtime() - startTime) / 1000;
            long h = sec / 3600, m = (sec % 3600) / 60, s = sec % 60;
            tvStatus.setText(h > 0
                    ? String.format(Locale.US, "%d:%02d:%02d", h, m, s)
                    : String.format(Locale.US, "%02d:%02d", m, s));
            handler.postDelayed(this, 1000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_outgoing_call); // dùng lại layout

        TextView tvName = findViewById(R.id.tvName);
        TextView tvInitials = findViewById(R.id.tvInitials);
        tvStatus = findViewById(R.id.tvStatus);

        // đổi 3 chỗ khác so với màn outgoing
        ((TextView) findViewById(R.id.tvBadge)).setText("yap session in progress");
        ((TextView) findViewById(R.id.tvCancelLabel)).setText("end call");
        tvStatus.setFontFeatureSettings("tnum"); // số đều nhau, đồng hồ không bị nhảy

        // người đối diện
        String peerId = getIntent().getStringExtra("peer_id");
        String peerNameHint = getIntent().getStringExtra("peer_name");
        if (peerNameHint != null) {
            tvName.setText(peerNameHint);
            tvInitials.setText(getInitials(peerNameHint));
        }
        if (peerId != null) {
            FirebaseFirestore.getInstance()
                    .collection("users").document(peerId).get()
                    .addOnSuccessListener(doc -> {
                        User u = doc.toObject(User.class);
                        if (u != null && u.getDisplayName() != null) {
                            tvName.setText(u.getDisplayName());
                            tvInitials.setText(getInitials(u.getDisplayName()));
                        }
                    });
        }

        // mốc bắt đầu cuộc gọi (truyền từ màn trước để đồng hồ không reset khi mở lại)
        long elapsedBefore = getIntent().getLongExtra("elapsed_ms", 0);
        startTime = SystemClock.elapsedRealtime() - elapsedBefore;

        findViewById(R.id.btnMinimize).setOnClickListener(v -> finish());
        findViewById(R.id.btnMute).setOnClickListener(v -> {
            v.setSelected(!v.isSelected());
            // TODO: mute mic thật
        });
        findViewById(R.id.btnSpeaker).setOnClickListener(v -> {
            v.setSelected(!v.isSelected());
            // TODO: bật/tắt loa ngoài thật
        });
        findViewById(R.id.btnKeypad).setOnClickListener(v -> {
            // TODO: bàn phím số (nếu làm)
        });
        findViewById(R.id.btnCancel).setOnClickListener(v -> {
            // TODO: cập nhật call_rooms sang ENDED
            finish();
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        handler.post(ticker);
    }

    @Override
    protected void onStop() {
        super.onStop();
        handler.removeCallbacks(ticker);
    }

    private String getInitials(String name) {
        if (name == null || name.trim().isEmpty()) return "";
        String[] parts = name.trim().split("\\s+");
        if (parts.length == 1) return parts[0].substring(0, 1).toUpperCase();
        return (parts[0].substring(0, 1) + parts[parts.length - 1].substring(0, 1)).toUpperCase();
    }
}