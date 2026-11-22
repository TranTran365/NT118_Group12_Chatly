package com.group12.chatly.ui.call;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.group12.chatly.R;
import com.group12.chatly.data.model.User;

import java.util.Locale;

public class ActiveVideoCallActivity extends AppCompatActivity {

    private final Handler handler = new Handler(Looper.getMainLooper());
    private TextView tvTimerChip;
    private long startTime;

    private final Runnable ticker = new Runnable() {
        @Override
        public void run() {
            long sec = (SystemClock.elapsedRealtime() - startTime) / 1000;
            long h = sec / 3600, m = (sec % 3600) / 60, s = sec % 60;
            String time = h > 0
                    ? String.format(Locale.US, "%d:%02d:%02d", h, m, s)
                    : String.format(Locale.US, "%02d:%02d", m, s);
            tvTimerChip.setText(time + " · connected");
            handler.postDelayed(this, 1000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_outgoing_video_call);

        tvTimerChip = findViewById(R.id.tvCallingChip);
        TextView tvPeerChip = findViewById(R.id.tvBottomChip);


        findViewById(R.id.pipContainer).setVisibility(View.VISIBLE);
        ((TextView) findViewById(R.id.tvCancelLabel)).setText("end call");
        tvPeerChip.setText("");

        // người đối diện
        String peerId = getIntent().getStringExtra("peer_id");
        String peerNameHint = getIntent().getStringExtra("peer_name");
        if (peerNameHint != null) {
            tvPeerChip.setText(peerNameHint);
        }
        if (peerId != null) {
            FirebaseFirestore.getInstance()
                    .collection("users").document(peerId).get()
                    .addOnSuccessListener(doc -> {
                        User u = doc.toObject(User.class);
                        if (u != null && u.getDisplayName() != null) {
                            tvPeerChip.setText(u.getDisplayName());
                        }
                    });
        }

        long elapsedBefore = getIntent().getLongExtra("elapsed_ms", 0);
        startTime = SystemClock.elapsedRealtime() - elapsedBefore;

        findViewById(R.id.btnMinimize).setOnClickListener(v -> finish());
        findViewById(R.id.btnMute).setOnClickListener(v -> {
            v.setSelected(!v.isSelected());
            // TODO: mute mic thật
        });
        findViewById(R.id.btnFlip).setOnClickListener(v -> {
            v.setSelected(!v.isSelected());
            // TODO: đổi camera trước/sau thật
        });
        findViewById(R.id.btnCameraOff).setOnClickListener(v -> {
            v.setSelected(!v.isSelected());
            // TODO: tắt/bật camera thật
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
}