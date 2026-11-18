package com.group12.chatly.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.group12.chatly.R;
import com.group12.chatly.ui.profile.EditProfileActivity;

public class VerifyEmailActivity extends AppCompatActivity {

    public static final String EXTRA_EMAIL = "extra_email";

    private TextView tvEmail;
    private TextView tvChangeEmail;
    private Button btnVerify;
    private Button btnOpenMail;
    private TextView tvResend;
    private ImageButton btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_email);

        tvEmail = findViewById(R.id.tvEmail);
        tvChangeEmail = findViewById(R.id.tvChangeEmail);
        btnVerify = findViewById(R.id.btnVerify);
        btnOpenMail = findViewById(R.id.btnOpenMail);
        tvResend = findViewById(R.id.tvResend);
        btnBack = findViewById(R.id.btnBack);

        String email = getIntent().getStringExtra(EXTRA_EMAIL);
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (email == null && currentUser != null) {
            email = currentUser.getEmail();
        }

        if (tvEmail != null && email != null) {
            tvEmail.setText(email);
        }

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        if (tvChangeEmail != null) {
            tvChangeEmail.setOnClickListener(v -> finish());
        }

        if (btnVerify != null) {
            btnVerify.setOnClickListener(v -> checkEmailVerification());
        }

        if (btnOpenMail != null) {
            btnOpenMail.setOnClickListener(v -> openMailApp());
        }

        if (tvResend != null) {
            tvResend.setOnClickListener(v -> resendVerificationEmail());
        }
    }

    private void checkEmailVerification() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Session expired. Please sign in again.", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        user.reload().addOnCompleteListener(task -> {
            if (user.isEmailVerified()) {
                Toast.makeText(this, "Email verified successfully!", Toast.LENGTH_SHORT).show();
                navigateToProfileSetup();
            } else {
                Toast.makeText(this, "Email not verified yet. Please check your inbox and tap the link.", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void openMailApp() {
        try {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_APP_EMAIL);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(Intent.createChooser(intent, "Open mail app"));
        } catch (Exception e) {
            Toast.makeText(this, "No mail app found", Toast.LENGTH_SHORT).show();
        }
    }

    private void resendVerificationEmail() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            user.sendEmailVerification().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(this, "Verification email sent!", Toast.LENGTH_SHORT).show();
                } else {
                    String msg = task.getException() != null ? task.getException().getMessage() : "Failed to send email.";
                    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(this, "No user signed in.", Toast.LENGTH_SHORT).show();
        }
    }

    private void navigateToProfileSetup() {
        Intent intent = new Intent(this, EditProfileActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
