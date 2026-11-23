package com.group12.chatly.ui.call;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.group12.chatly.R;
import com.group12.chatly.data.model.User;

public class OutgoingVideoCallActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_outgoing_video_call);

        TextView chip = findViewById(R.id.tvCallingChip);
        chip.setText("calling...");

        String calleeId = getIntent().getStringExtra("callee_id");
        String calleeNameHint = getIntent().getStringExtra("callee_name");

        if (calleeNameHint != null) {
            chip.setText("calling " + calleeNameHint + "...");
        }
        if (calleeId != null) {
            FirebaseFirestore.getInstance()
                    .collection("users").document(calleeId).get()
                    .addOnSuccessListener(doc -> {
                        User u = doc.toObject(User.class);
                        if (u != null && u.getDisplayName() != null) {
                            chip.setText("calling " + u.getDisplayName() + "...");
                        }
                    });
        }

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
            // TODO: cập nhật call_rooms sang ENDED/REJECTED
            finish();
        });
    }
}