package com.group12.chatly.ui.profile;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.view.animation.CycleInterpolator;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import com.group12.chatly.R;
import com.group12.chatly.utils.PinManager;

public class LockPinActivity extends AppCompatActivity {

    public static final String EXTRA_MODE = "extra_mode";
    public static final int MODE_UNLOCK = 0;
    public static final int MODE_CREATE = 1;

    private static final int[] KEY_IDS = {
            R.id.key_0, R.id.key_1, R.id.key_2, R.id.key_3, R.id.key_4,
            R.id.key_5, R.id.key_6, R.id.key_7, R.id.key_8, R.id.key_9
    };

    private PinManager pinManager;
    private int mode;

    private final StringBuilder input = new StringBuilder();
    private String firstPin = null; // MODE_CREATE: PIN ở lần nhập đầu

    private View[] dots;
    private View layoutDots;
    private TextView tvTitle, tvSubtitle;
    private View keyBiometric;

    private int subtitleDefaultColor;
    private boolean inputEnabled = true;
    private CountDownTimer lockoutTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lock_pin);

        pinManager = PinManager.getInstance(this);
        mode = getIntent().getIntExtra(EXTRA_MODE, MODE_UNLOCK);

        bindViews();
        setupKeypad();
        setupModeUi();
        setupBackPress();

        if (mode == MODE_UNLOCK) {
            startLockoutIfNeeded();
            if (pinManager.isBiometricEnabled() && canUseBiometric() && inputEnabled) {
                showBiometricPrompt();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (lockoutTimer != null) lockoutTimer.cancel();
    }

    // ---------------------------------------------------------------- setup

    private void bindViews() {
        tvTitle = findViewById(R.id.tv_title);
        tvSubtitle = findViewById(R.id.tv_subtitle);
        layoutDots = findViewById(R.id.layout_pin_dots);
        keyBiometric = findViewById(R.id.key_biometric);
        subtitleDefaultColor = tvSubtitle.getCurrentTextColor();

        dots = new View[]{
                findViewById(R.id.pin_dot_1), findViewById(R.id.pin_dot_2),
                findViewById(R.id.pin_dot_3), findViewById(R.id.pin_dot_4),
                findViewById(R.id.pin_dot_5), findViewById(R.id.pin_dot_6)
        };
    }

    private void setupKeypad() {
        for (int i = 0; i < KEY_IDS.length; i++) {
            final String digit = String.valueOf(i);
            findViewById(KEY_IDS[i]).setOnClickListener(v -> onDigit(digit));
        }
        findViewById(R.id.key_delete).setOnClickListener(v -> onDelete());
        keyBiometric.setOnClickListener(v -> {
            if (inputEnabled) showBiometricPrompt();
        });
    }

    private void setupModeUi() {
        View forgot = findViewById(R.id.tv_forgot_pin);

        if (mode == MODE_CREATE) {
            tvTitle.setText(R.string.lock_pin_create_title);
            tvSubtitle.setText(R.string.lock_pin_create_subtitle);
            keyBiometric.setVisibility(View.INVISIBLE);
            forgot.setVisibility(View.GONE);
        } else {
            boolean showBio = pinManager.isBiometricEnabled() && canUseBiometric();
            keyBiometric.setVisibility(showBio ? View.VISIBLE : View.INVISIBLE);
            forgot.setOnClickListener(v -> {
                // TODO (bước 5): xác nhận -> pinManager.clear() -> đăng xuất -> WelcomeActivity
            });
        }
    }

    private void setupBackPress() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (mode == MODE_UNLOCK) {
                    // Không cho Back để lọt vào app: thoát hẳn app
                    finishAffinity();
                } else {
                    setResult(RESULT_CANCELED);
                    finish();
                }
            }
        });
    }

    // ---------------------------------------------------------------- input

    private void onDigit(String digit) {
        if (!inputEnabled || input.length() >= PinManager.PIN_LENGTH) return;
        input.append(digit);
        refreshDots();
        if (input.length() == PinManager.PIN_LENGTH) {
            // Cho chấm cuối kịp hiện ra rồi mới xử lý
            layoutDots.postDelayed(this::onPinComplete, 120);
        }
    }

    private void onDelete() {
        if (!inputEnabled || input.length() == 0) return;
        input.deleteCharAt(input.length() - 1);
        refreshDots();
    }

    private void refreshDots() {
        for (int i = 0; i < dots.length; i++) {
            dots[i].setSelected(i < input.length());
        }
    }

    private void clearInput() {
        input.setLength(0);
        refreshDots();
    }

    private void onPinComplete() {
        String pin = input.toString();
        if (mode == MODE_CREATE) {
            handleCreate(pin);
        } else {
            handleUnlock(pin);
        }
    }

    // ---------------------------------------------------------------- unlock

    private void handleUnlock(String pin) {
        if (pinManager.verifyPin(pin)) {
            onUnlocked();
            return;
        }
        shakeDots();
        clearInput();
        if (pinManager.getRemainingLockoutMs() > 0) {
            startLockoutIfNeeded();
        } else {
            showError(getString(R.string.lock_pin_error_wrong, pinManager.getRemainingAttempts()));
        }
    }

    private void onUnlocked() {
        setResult(RESULT_OK);
        finish();
    }

    private void startLockoutIfNeeded() {
        long remain = pinManager.getRemainingLockoutMs();
        if (remain <= 0) return;

        inputEnabled = false;
        clearInput();
        if (lockoutTimer != null) lockoutTimer.cancel();
        lockoutTimer = new CountDownTimer(remain, 1000) {
            @Override
            public void onTick(long ms) {
                showError(getString(R.string.lock_pin_error_locked, (int) Math.ceil(ms / 1000.0)));
            }

            @Override
            public void onFinish() {
                inputEnabled = true;
                resetSubtitle();
            }
        }.start();
    }

    // ---------------------------------------------------------------- create

    private void handleCreate(String pin) {
        if (firstPin == null) {
            // Lần nhập đầu -> chuyển sang bước xác nhận
            firstPin = pin;
            clearInput();
            tvTitle.setText(R.string.lock_pin_confirm_title);
            resetSubtitle(R.string.lock_pin_confirm_subtitle);
        } else if (firstPin.equals(pin)) {
            pinManager.setPin(pin);
            setResult(RESULT_OK);
            finish();
        } else {
            // Không khớp -> làm lại từ đầu
            firstPin = null;
            shakeDots();
            clearInput();
            tvTitle.setText(R.string.lock_pin_create_title);
            showError(getString(R.string.lock_pin_error_mismatch));
        }
    }

    // ---------------------------------------------------------------- biometric

    private boolean canUseBiometric() {
        return BiometricManager.from(this)
                .canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)
                == BiometricManager.BIOMETRIC_SUCCESS;
    }

    private void showBiometricPrompt() {
        if (!canUseBiometric()) return;

        BiometricPrompt prompt = new BiometricPrompt(this,
                ContextCompat.getMainExecutor(this),
                new BiometricPrompt.AuthenticationCallback() {
                    @Override
                    public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) {
                        onUnlocked();
                    }
                    // Lỗi / hủy: không làm gì, người dùng vẫn nhập PIN bình thường
                });

        BiometricPrompt.PromptInfo info = new BiometricPrompt.PromptInfo.Builder()
                .setTitle(getString(R.string.lock_pin_biometric_title))
                .setSubtitle(getString(R.string.lock_pin_biometric_subtitle))
                .setNegativeButtonText(getString(R.string.lock_pin_biometric_cancel))
                .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
                .build();
        prompt.authenticate(info);
    }

    // ---------------------------------------------------------------- UI helpers

    private void showError(String message) {
        tvSubtitle.setText(message);
        tvSubtitle.setTextColor(ContextCompat.getColor(this, R.color.register_error));
    }

    private void resetSubtitle() {
        resetSubtitle(mode == MODE_CREATE
                ? R.string.lock_pin_create_subtitle
                : R.string.lock_pin_subtitle);
    }

    private void resetSubtitle(int resId) {
        tvSubtitle.setText(resId);
        tvSubtitle.setTextColor(subtitleDefaultColor);
    }

    private void shakeDots() {
        ObjectAnimator shake = ObjectAnimator.ofFloat(layoutDots, "translationX", 0f, 16f);
        shake.setInterpolator(new CycleInterpolator(3));
        shake.setDuration(350);
        shake.start();
    }
}