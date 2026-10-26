package com.group12.chatly.ui.splash;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.group12.chatly.MainActivity;
import com.group12.chatly.R;
import com.group12.chatly.data.repository.AuthRepository;
import com.group12.chatly.ui.welcome.WelcomeActivity;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {
    private static final long SPLASH_DELAY_MS = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Class<?> targetActivity = AuthRepository.getInstance().isUserLoggedIn()
                    ? MainActivity.class
                    : WelcomeActivity.class;
            startActivity(new Intent(this, targetActivity));
            finish();
        }, SPLASH_DELAY_MS);
    }
}
