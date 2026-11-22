package com.group12.chatly.ui.call;

import android.os.Build;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import com.group12.chatly.R;
import com.group12.chatly.data.model.User;

public class IncomingCallActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // hiện trên màn hình khóa khi có cuộc gọi
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true);
            setTurnScreenOn(true);
        } else {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                    | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        }
        setContentView(R.layout.activity_incoming_call);

        String callerId = getIntent().getStringExtra("caller_id");
        String callerNameHint = getIntent().getStringExtra("caller_name"); // hiện tạm trong lúc load

        TextView tvName = findViewById(R.id.tvName);
        TextView tvInitials = findViewById(R.id.tvInitials);

        // hiện tạm tên truyền kèm (nếu có), không thì để trống
        if (callerNameHint != null) {
            tvName.setText(callerNameHint);
            tvInitials.setText(getInitials(callerNameHint));
        }

        // load tên thật từ Firestore
        if (callerId != null) {
            FirebaseFirestore.getInstance()
                .collection("users").document(callerId).get()
                .addOnSuccessListener(doc -> {
                    User u = doc.toObject(User.class);
                    if (u != null && u.getDisplayName() != null) {
                        tvName.setText(u.getDisplayName());
                        tvInitials.setText(getInitials(u.getDisplayName()));
                    }
                });
        }

        findViewById(R.id.btnDecline).setOnClickListener(v -> finish());
        findViewById(R.id.btnAccept).setOnClickListener(v -> {
            // TODO: mở màn hình đang gọi (CallActivity)
        });
        findViewById(R.id.btnMinimize).setOnClickListener(v -> finish());
    }

    private String getInitials(String name) {
        if (name == null || name.trim().isEmpty()) return "";
        String[] parts = name.trim().split("\\s+");
        if (parts.length == 1) return parts[0].substring(0, 1).toUpperCase();
        return (parts[0].substring(0, 1) + parts[parts.length - 1].substring(0, 1)).toUpperCase();
    }
}