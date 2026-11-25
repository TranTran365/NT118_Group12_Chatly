package com.group12.chatly.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.group12.chatly.R;
import com.group12.chatly.utils.PinManager;

public class PrivacyActivity extends AppCompatActivity {

    private PinManager pinManager;
    private SwitchCompat swAppLock, swFingerprint;
    private View rowAppLock, rowFingerprint, rowChangePin, rowLockedChats;

    private final ActivityResultLauncher<Intent> lockPinLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    if (pinManager.hasPin()) {
                        pinManager.setLockEnabled(true);
                    }
                }
                updateUiState();
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy);

        pinManager = PinManager.getInstance(this);

        bindViews();
        setupInsets();
        setupListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateUiState();
    }

    private void bindViews() {
        swAppLock = findViewById(R.id.sw_app_lock);
        swFingerprint = findViewById(R.id.sw_fingerprint);
        rowAppLock = findViewById(R.id.row_app_lock);
        rowFingerprint = findViewById(R.id.row_fingerprint);
        rowChangePin = findViewById(R.id.row_change_pin);
        rowLockedChats = findViewById(R.id.row_locked_chats);
    }

    private void setupInsets() {
        View header = findViewById(R.id.header_privacy);
        if (header != null) {
            final int basePaddingTop = header.getPaddingTop();
            ViewCompat.setOnApplyWindowInsetsListener(header, (v, insets) -> {
                Insets bars = insets.getInsets(
                        WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout());
                v.setPadding(v.getPaddingLeft(), basePaddingTop + bars.top,
                        v.getPaddingRight(), v.getPaddingBottom());
                return insets;
            });
        }
    }

    private void setupListeners() {
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        rowAppLock.setOnClickListener(v -> swAppLock.toggle());
        rowFingerprint.setOnClickListener(v -> swFingerprint.toggle());

        swAppLock.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                if (!pinManager.hasPin()) {
                    Intent intent = new Intent(this, LockPinActivity.class);
                    intent.putExtra(LockPinActivity.EXTRA_MODE, LockPinActivity.MODE_CREATE);
                    lockPinLauncher.launch(intent);
                } else {
                    pinManager.setLockEnabled(true);
                }
            } else {
                pinManager.setLockEnabled(false);
            }
            updateUiState();
        });

        swFingerprint.setOnCheckedChangeListener((buttonView, isChecked) ->
                pinManager.setBiometricEnabled(isChecked));

        rowChangePin.setOnClickListener(v -> {
            Intent intent = new Intent(this, LockPinActivity.class);
            intent.putExtra(LockPinActivity.EXTRA_MODE, LockPinActivity.MODE_CREATE);
            lockPinLauncher.launch(intent);
        });

        rowLockedChats.setOnClickListener(v ->
                Toast.makeText(this, "Locked chats feature coming soon", Toast.LENGTH_SHORT).show());
    }

    private void updateUiState() {
        boolean lockEnabled = pinManager.isLockEnabled();

        swAppLock.setOnCheckedChangeListener(null);
        swAppLock.setChecked(lockEnabled);
        setupSwAppLockListener();

        swFingerprint.setEnabled(lockEnabled);
        rowFingerprint.setAlpha(lockEnabled ? 1.0f : 0.5f);
        swFingerprint.setOnCheckedChangeListener(null);
        swFingerprint.setChecked(pinManager.isBiometricEnabled() && lockEnabled);
        setupSwFingerprintListener();

        rowChangePin.setEnabled(lockEnabled);
        rowChangePin.setAlpha(lockEnabled ? 1.0f : 0.5f);
    }

    private void setupSwAppLockListener() {
        swAppLock.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                if (!pinManager.hasPin()) {
                    Intent intent = new Intent(this, LockPinActivity.class);
                    intent.putExtra(LockPinActivity.EXTRA_MODE, LockPinActivity.MODE_CREATE);
                    lockPinLauncher.launch(intent);
                } else {
                    pinManager.setLockEnabled(true);
                }
            } else {
                pinManager.setLockEnabled(false);
            }
            updateUiState();
        });
    }

    private void setupSwFingerprintListener() {
        swFingerprint.setOnCheckedChangeListener((buttonView, isChecked) ->
                pinManager.setBiometricEnabled(isChecked));
    }
}
