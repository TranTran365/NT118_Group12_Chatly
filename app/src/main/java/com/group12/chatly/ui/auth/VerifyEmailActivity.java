package com.group12.chatly.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.group12.chatly.R;
import com.group12.chatly.data.repository.AuthRepository;
import com.group12.chatly.ui.profile.EditProfileActivity;

public class VerifyEmailActivity extends AppCompatActivity {

    public static final String EXTRA_EMAIL = "extra_email";
    private static final long COOLDOWN_MS = 30000; // 30 seconds cooldown

    private TextView tvEmail;
    private TextView tvChangeEmail;
    private Button btnVerify;
    private Button btnOpenMail;
    private TextView tvResend;
    private ImageButton btnBack;

    private CountDownTimer resendTimer;

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
            btnBack.setOnClickListener(v -> performSignOutAndBack());
        }

        if (tvChangeEmail != null) {
            tvChangeEmail.setOnClickListener(v -> performSignOutAndBack());
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
            Toast.makeText(this, R.string.verify_session_expired, Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        user.reload().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Toast.makeText(this, R.string.verify_error_network, Toast.LENGTH_SHORT).show();
                return;
            }

            if (user.isEmailVerified()) {
                Toast.makeText(this, R.string.verify_success, Toast.LENGTH_SHORT).show();
                navigateToProfileSetup();
            } else {
                Toast.makeText(this, R.string.verify_not_yet, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void openMailApp() {
        try {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_APP_EMAIL);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(Intent.createChooser(intent, getString(R.string.verify_btn_open_mail)));
        } catch (Exception e) {
            Toast.makeText(this, R.string.verify_no_mail_app, Toast.LENGTH_SHORT).show();
        }
    }

    private void resendVerificationEmail() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            user.sendEmailVerification().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(this, R.string.verify_email_sent, Toast.LENGTH_SHORT).show();
                    startResendCooldown();
                } else {
                    String msg = task.getException() != null && task.getException().getMessage() != null
                            ? task.getException().getMessage()
                            : getString(R.string.verify_not_yet);
                    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(this, R.string.verify_session_expired, Toast.LENGTH_SHORT).show();
        }
    }

    private void startResendCooldown() {
        if (tvResend == null) return;
        tvResend.setEnabled(false);
        tvResend.setAlpha(0.6f);

        if (resendTimer != null) {
            resendTimer.cancel();
        }

        resendTimer = new CountDownTimer(COOLDOWN_MS, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long seconds = millisUntilFinished / 1000;
                long mins = seconds / 60;
                long secs = seconds % 60;
                tvResend.setText(getString(R.string.verify_resend_cooldown, mins, secs));
            }

            @Override
            public void onFinish() {
                tvResend.setEnabled(true);
                tvResend.setAlpha(1.0f);
                tvResend.setText(R.string.verify_resend);
            }
        }.start();
    }

    private void performSignOutAndBack() {
        AuthRepository.getInstance().logout();
        Intent intent = new Intent(this, RegisterActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void navigateToProfileSetup() {
        Intent intent = new Intent(this, EditProfileActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (resendTimer != null) {
            resendTimer.cancel();
        }
    }
}
