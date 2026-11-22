package com.group12.chatly.ui.call;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.group12.chatly.R;
import com.group12.chatly.data.model.User;

public class OutgoingCallActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_outgoing_call);

        TextView tvName = findViewById(R.id.tvName);
        TextView tvInitials = findViewById(R.id.tvInitials);

        String calleeId = getIntent().getStringExtra("callee_id");
        String calleeNameHint = getIntent().getStringExtra("callee_name");

        if (calleeNameHint != null) {
            tvName.setText(calleeNameHint);
            tvInitials.setText(getInitials(calleeNameHint));
        }

        if (calleeId != null) {
            FirebaseFirestore.getInstance()
                    .collection("users").document(calleeId).get()
                    .addOnSuccessListener(doc -> {
                        User u = doc.toObject(User.class);
                        if (u != null && u.getDisplayName() != null) {
                            tvName.setText(u.getDisplayName());
                            tvInitials.setText(getInitials(u.getDisplayName()));
                        }
                    });
        }

        findViewById(R.id.btnMinimize).setOnClickListener(v -> finish());

        // mute / speaker: bấm để bật-tắt trạng thái giao diện
        findViewById(R.id.btnMute).setOnClickListener(v -> {
            v.setSelected(!v.isSelected());
            // TODO: gọi Zego/AudioManager để mute thật
        });
        findViewById(R.id.btnSpeaker).setOnClickListener(v -> {
            v.setSelected(!v.isSelected());
            // TODO: bật/tắt loa ngoài thật
        });
        findViewById(R.id.btnKeypad).setOnClickListener(v -> {
            // TODO: mở bàn phím số (nếu làm)
        });

        findViewById(R.id.btnCancel).setOnClickListener(v -> {
            // TODO: cập nhật call_rooms sang trạng thái ENDED/REJECTED
            finish();
        });
    }

    private String getInitials(String name) {
        if (name == null || name.trim().isEmpty()) return "";
        String[] parts = name.trim().split("\\s+");
        if (parts.length == 1) return parts[0].substring(0, 1).toUpperCase();
        return (parts[0].substring(0, 1) + parts[parts.length - 1].substring(0, 1)).toUpperCase();
    }
}