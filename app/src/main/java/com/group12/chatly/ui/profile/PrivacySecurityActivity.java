package com.group12.chatly.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.group12.chatly.utils.AppLockManager;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.biometric.BiometricManager;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.group12.chatly.R;
import com.group12.chatly.utils.PinManager;

public class PrivacySecurityActivity extends AppCompatActivity {

    private PinManager pinManager;
    private SwitchCompat swAppLock, swFingerprint;
    private View rowFingerprint, rowChangePin;
    private boolean updatingUi = false; // chặn listener chạy khi mình tự set trạng thái công tắc

    // Bật App lock: đặt PIN mới (2 lần) -> thành công thì mới bật khóa
    private final ActivityResultLauncher<Intent> enableLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK) {
                    pinManager.setLockEnabled(true);
                    AppLockManager.markUnlocked();
                }
                refreshUi();
            });

    // Tắt App lock: phải nhập đúng PIN hiện tại
    private final ActivityResultLauncher<Intent> disableLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK) {
                    pinManager.clear();
                    Toast.makeText(this, R.string.privacy_lock_off, Toast.LENGTH_SHORT).show();
                }
                refreshUi();
            });

    // Đổi PIN bước 2: đặt PIN mới (LockPinActivity tự lưu khi 2 lần khớp)
    private final ActivityResultLauncher<Intent> changeCreateLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK) {
                    Toast.makeText(this, R.string.privacy_pin_updated, Toast.LENGTH_SHORT).show();
                }
            });

    // Đổi PIN bước 1: nhập PIN hiện tại
    private final ActivityResultLauncher<Intent> changeVerifyLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK) {
                    changeCreateLauncher.launch(buildIntent(LockPinActivity.MODE_CREATE));
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy_security);

        pinManager = PinManager.getInstance(this);

        swAppLock = findViewById(R.id.sw_app_lock);
        swFingerprint = findViewById(R.id.sw_fingerprint);
        rowFingerprint = findViewById(R.id.row_fingerprint);
        rowChangePin = findViewById(R.id.row_change_pin);

        setupInsets();
        setupClicks();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshUi();
    }

    //  setup

    private void setupInsets() {
        View header = findViewById(R.id.header_privacy);
        final int basePaddingTop = header.getPaddingTop();
        ViewCompat.setOnApplyWindowInsetsListener(header, (v, insets) -> {
            Insets bars = insets.getInsets(
                    WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout());
            v.setPadding(v.getPaddingLeft(), basePaddingTop + bars.top,
                    v.getPaddingRight(), v.getPaddingBottom());
            return insets;
        });
        ViewCompat.requestApplyInsets(header);
    }

    private void setupClicks() {
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        // Bấm cả hàng cũng bật/tắt được
        findViewById(R.id.row_app_lock).setOnClickListener(v -> swAppLock.toggle());

        swAppLock.setOnCheckedChangeListener((button, checked) -> {
            if (updatingUi) return;
            if (checked) {
                enableLauncher.launch(buildIntent(LockPinActivity.MODE_CREATE));
            } else {
                disableLauncher.launch(buildIntent(LockPinActivity.MODE_VERIFY));
            }
        });

        rowFingerprint.setOnClickListener(v -> {
            if (!pinManager.isLockEnabled()) {
                toast(R.string.privacy_need_app_lock);
            } else if (!canUseBiometric()) {
                toast(R.string.privacy_no_biometric);
            } else {
                swFingerprint.toggle();
            }
        });

        swFingerprint.setOnCheckedChangeListener((button, checked) -> {
            if (updatingUi) return;
            pinManager.setBiometricEnabled(checked);
        });

        rowChangePin.setOnClickListener(v -> {
            if (!pinManager.isLockEnabled()) {
                toast(R.string.privacy_need_app_lock);
            } else {
                changeVerifyLauncher.launch(buildIntent(LockPinActivity.MODE_VERIFY));
            }
        });

        findViewById(R.id.row_locked_chats).setOnClickListener(v -> {
            // TODO: Locked chats (tính năng riêng, chưa làm)
        });
    }

    // UI state

    private void refreshUi() {
        boolean lockOn = pinManager.isLockEnabled();
        boolean bioOk = canUseBiometric();

        updatingUi = true;
        swAppLock.setChecked(lockOn);
        swFingerprint.setChecked(lockOn && bioOk && pinManager.isBiometricEnabled());
        updatingUi = false;

        swFingerprint.setEnabled(lockOn && bioOk);
        rowFingerprint.setAlpha(lockOn && bioOk ? 1f : 0.45f);
        rowChangePin.setAlpha(lockOn ? 1f : 0.45f);
    }

    private boolean canUseBiometric() {
        return BiometricManager.from(this)
                .canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)
                == BiometricManager.BIOMETRIC_SUCCESS;
    }

    private Intent buildIntent(int mode) {
        return new Intent(this, LockPinActivity.class)
                .putExtra(LockPinActivity.EXTRA_MODE, mode);
    }

    private void toast(int resId) {
        Toast.makeText(this, resId, Toast.LENGTH_SHORT).show();
    }
}