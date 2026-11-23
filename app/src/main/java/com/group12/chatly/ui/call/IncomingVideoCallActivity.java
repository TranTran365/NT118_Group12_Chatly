package com.group12.chatly.ui.call;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.group12.chatly.R;
import com.group12.chatly.data.model.User;

public class IncomingVideoCallActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // hiện trên màn hình khóa khi có cuộc gọi
        setShowWhenLocked(true);
        setTurnScreenOn(true);
        setContentView(R.layout.activity_incoming_video_call);

        TextView chip = findViewById(R.id.tvCallingChip);

        String callerId = getIntent().getStringExtra("caller_id");
        String callerNameHint = getIntent().getStringExtra("caller_name");

        if (callerNameHint != null) {
            chip.setText(callerNameHint + " is calling...");
        }
        if (callerId != null) {
            FirebaseFirestore.getInstance()
                    .collection("users").document(callerId).get()
                    .addOnSuccessListener(doc -> {
                        User u = doc.toObject(User.class);
                        if (u != null && u.getDisplayName() != null) {
                            chip.setText(u.getDisplayName() + " is calling...");
                        }
                    });
        }

        findViewById(R.id.btnMinimize).setOnClickListener(v -> finish());
        findViewById(R.id.btnDecline).setOnClickListener(v -> {
            // TODO: cập nhật call_rooms sang REJECTED
            finish();
        });
        findViewById(R.id.btnAccept).setOnClickListener(v -> {
            // TODO: chuyển sang màn video call đang diễn ra
        });
        findViewById(R.id.btnMessage).setOnClickListener(v -> {
            // TODO: từ chối + mở chat
        });
        findViewById(R.id.btnRemind).setOnClickListener(v -> {
            // TODO: nhắc lại sau
        });
    }
}