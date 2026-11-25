package com.group12.chatly.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

import androidx.annotation.NonNull;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import java.security.MessageDigest;
import java.security.SecureRandom;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Quản lý PIN khóa app (chỉ lưu trên máy).
 * - PIN không lưu thô: hash PBKDF2 + salt ngẫu nhiên.
 * - Dữ liệu nằm trong EncryptedSharedPreferences (khóa nằm trong Android Keystore).
 */
public class PinManager {

    public static final int PIN_LENGTH = 6;
    public static final int MAX_ATTEMPTS = 5;
    public static final long LOCKOUT_MS = 30_000L;

    private static final String PREFS_NAME = "chatly_app_lock";
    private static final String KEY_HASH = "pin_hash";
    private static final String KEY_SALT = "pin_salt";
    private static final String KEY_LOCK_ENABLED = "lock_enabled";
    private static final String KEY_BIOMETRIC = "biometric_enabled";
    private static final String KEY_FAILS = "failed_attempts";
    private static final String KEY_LOCKED_UNTIL = "locked_until";

    private static final int PBKDF2_ITERATIONS = 100_000;
    private static final int HASH_BITS = 256;

    private static PinManager instance;
    private final SharedPreferences prefs;

    public static synchronized PinManager getInstance(@NonNull Context context) {
        if (instance == null) {
            instance = new PinManager(context.getApplicationContext());
        }
        return instance;
    }

    private PinManager(Context appContext) {
        try {
            MasterKey masterKey = new MasterKey.Builder(appContext)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();
            prefs = EncryptedSharedPreferences.create(
                    appContext,
                    PREFS_NAME,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM);
        } catch (Exception e) {
            throw new IllegalStateException("Cannot init PinManager", e);
        }
    }

    // ---------- Trạng thái ----------

    public boolean hasPin() {
        return prefs.contains(KEY_HASH) && prefs.contains(KEY_SALT);
    }

    /** Khóa app chỉ có hiệu lực khi đã bật VÀ đã có PIN. */
    public boolean isLockEnabled() {
        return prefs.getBoolean(KEY_LOCK_ENABLED, false) && hasPin();
    }

    public void setLockEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_LOCK_ENABLED, enabled).apply();
    }

    public boolean isBiometricEnabled() {
        return prefs.getBoolean(KEY_BIOMETRIC, false);
    }

    public void setBiometricEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_BIOMETRIC, enabled).apply();
    }

    // ---------- Đặt / kiểm tra PIN ----------

    public void setPin(@NonNull String pin) {
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        byte[] hash = hash(pin, salt);
        prefs.edit()
                .putString(KEY_SALT, Base64.encodeToString(salt, Base64.NO_WRAP))
                .putString(KEY_HASH, Base64.encodeToString(hash, Base64.NO_WRAP))
                .putInt(KEY_FAILS, 0)
                .putLong(KEY_LOCKED_UNTIL, 0L)
                .apply();
    }

    /** @return true nếu đúng PIN. Sai thì tăng bộ đếm; sai đủ MAX_ATTEMPTS thì khóa tạm. */
    public boolean verifyPin(@NonNull String pin) {
        if (getRemainingLockoutMs() > 0 || !hasPin()) return false;

        byte[] salt = Base64.decode(prefs.getString(KEY_SALT, ""), Base64.NO_WRAP);
        byte[] expected = Base64.decode(prefs.getString(KEY_HASH, ""), Base64.NO_WRAP);
        boolean ok = MessageDigest.isEqual(expected, hash(pin, salt));

        if (ok) {
            prefs.edit().putInt(KEY_FAILS, 0).putLong(KEY_LOCKED_UNTIL, 0L).apply();
        } else {
            int fails = prefs.getInt(KEY_FAILS, 0) + 1;
            SharedPreferences.Editor editor = prefs.edit();
            if (fails >= MAX_ATTEMPTS) {
                editor.putInt(KEY_FAILS, 0)
                        .putLong(KEY_LOCKED_UNTIL, System.currentTimeMillis() + LOCKOUT_MS);
            } else {
                editor.putInt(KEY_FAILS, fails);
            }
            editor.apply();
        }
        return ok;
    }

    public int getRemainingAttempts() {
        return MAX_ATTEMPTS - prefs.getInt(KEY_FAILS, 0);
    }

    /** Số ms còn bị khóa tạm (0 nếu không bị khóa). */
    public long getRemainingLockoutMs() {
        long left = prefs.getLong(KEY_LOCKED_UNTIL, 0L) - System.currentTimeMillis();
        return Math.max(left, 0L);
    }

    /** Xóa toàn bộ (gọi khi đăng xuất hoặc tắt App lock). */
    public void clear() {
        prefs.edit().clear().apply();
    }

    // ---------- Nội bộ ----------

    private static byte[] hash(String pin, byte[] salt) {
        try {
            PBEKeySpec spec = new PBEKeySpec(pin.toCharArray(), salt, PBKDF2_ITERATIONS, HASH_BITS);
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                    .generateSecret(spec).getEncoded();
        } catch (Exception e) {
            throw new IllegalStateException("Hash PIN failed", e);
        }
    }
}