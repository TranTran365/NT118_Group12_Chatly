package com.group12.chatly.utils;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;
import com.group12.chatly.ui.auth.LoginActivity;
import com.group12.chatly.ui.auth.RegisterActivity;
import com.group12.chatly.ui.auth.VerifyEmailActivity;
import com.group12.chatly.ui.profile.LockPinActivity;
import com.group12.chatly.ui.splash.SplashActivity;
import com.group12.chatly.ui.welcome.WelcomeActivity;

/**
 * Chốt chặn App lock: tự mở LockPinActivity khi
 *  - mở app lần đầu (cold start), hoặc
 *  - quay lại từ nền sau hơn GRACE_MS.
 * Chỉ hoạt động khi người dùng đã đăng nhập và đã bật App lock.
 */
public class AppLockManager implements Application.ActivityLifecycleCallbacks {


    private static final long GRACE_MS = 30_000L;

    private static final AppLockManager INSTANCE = new AppLockManager();

    private int startedCount = 0;      // số activity đang "started" (>0 = app ở foreground)
    private long backgroundAt = 0L;    // thời điểm app vào nền
    private boolean unlocked = false;  // đã mở khóa trong phiên này
    private boolean lockVisible = false; // đang hiện / sắp hiện màn khóa

    private AppLockManager() {}

    public static void init(Application app) {
        app.registerActivityLifecycleCallbacks(INSTANCE);
    }

    /** Gọi khi vừa nhập đúng PIN, hoặc vừa bật App lock (đang ở trong app nên không khóa ngay). */
    public static void markUnlocked() {
        INSTANCE.unlocked = true;
        INSTANCE.lockVisible = false;
    }

    /** Gọi khi đăng xuất. */
    public static void reset() {
        INSTANCE.unlocked = false;
        INSTANCE.backgroundAt = 0L;
    }


    @Override
    public void onActivityStarted(@NonNull Activity activity) {
        // Quay lại từ nền: nếu ở nền quá lâu thì bắt nhập lại PIN
        if (startedCount == 0 && backgroundAt != 0L
                && SystemClock.elapsedRealtime() - backgroundAt > GRACE_MS) {
            unlocked = false;
        }
        startedCount++;

        if (shouldLock(activity)) {
            lockVisible = true;
            activity.startActivity(new Intent(activity, LockPinActivity.class)
                    .putExtra(LockPinActivity.EXTRA_MODE, LockPinActivity.MODE_UNLOCK));
        }
    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {
        startedCount = Math.max(0, startedCount - 1);
        if (startedCount == 0) {
            backgroundAt = SystemClock.elapsedRealtime();
        }
    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {
        if (activity instanceof LockPinActivity) {
            lockVisible = false;
        }
    }

    private boolean shouldLock(Activity activity) {
        if (unlocked || lockVisible) return false;

        if (!activity.getClass().getName().startsWith("com.group12.chatly")) return false;

        if (activity instanceof LockPinActivity
                || activity instanceof SplashActivity
                || activity instanceof WelcomeActivity
                || activity instanceof LoginActivity
                || activity instanceof RegisterActivity
                || activity instanceof VerifyEmailActivity) {
            return false;
        }

        if (FirebaseAuth.getInstance().getCurrentUser() == null) return false;

        return PinManager.getInstance(activity).isLockEnabled();
    }


    @Override public void onActivityCreated(@NonNull Activity a, @Nullable Bundle b) {}
    @Override public void onActivityResumed(@NonNull Activity a) {}
    @Override public void onActivityPaused(@NonNull Activity a) {}
    @Override public void onActivitySaveInstanceState(@NonNull Activity a, @NonNull Bundle b) {}
}