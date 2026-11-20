package com.group12.chatly.ui.splash;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseUser;
import com.group12.chatly.MainActivity;
import com.group12.chatly.R;
import com.group12.chatly.data.repository.AuthRepository;
import com.group12.chatly.ui.auth.VerifyEmailActivity;
import com.group12.chatly.ui.welcome.WelcomeActivity;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {
    private static final long SPLASH_DELAY_MS = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            AuthRepository authRepository = AuthRepository.getInstance();
            if (authRepository.isUserLoggedIn()) {
                FirebaseUser currentUser = authRepository.getCurrentUser();
                if (currentUser != null && !currentUser.isEmailVerified()
                        && currentUser.getProviderData().stream().anyMatch(p -> "password".equals(p.getProviderId()))) {
                    Intent intent = new Intent(this, VerifyEmailActivity.class);
                    intent.putExtra(VerifyEmailActivity.EXTRA_EMAIL, currentUser.getEmail());
                    startActivity(intent);
                } else {
                    startActivity(new Intent(this, MainActivity.class));
                }
            } else {
                startActivity(new Intent(this, WelcomeActivity.class));
            }
            finish();
        }, SPLASH_DELAY_MS);
    }
}
