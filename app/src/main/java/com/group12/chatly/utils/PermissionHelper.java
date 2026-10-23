package com.group12.chatly.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.group12.chatly.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for runtime permissions handling.
 * Compatible with Android 7.0 (API 24) to Android 14+ (API 34+),
 * including Android 13 Granular Media Permissions.
 */
public final class PermissionHelper {

    private PermissionHelper() {
        // Private constructor to prevent instantiation
    }

    public static final String[] LOCATION_PERMISSIONS = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };

    public static final String[] AUDIO_PERMISSIONS = {
            Manifest.permission.RECORD_AUDIO
    };

    public static final String[] CAMERA_PERMISSIONS = {
            Manifest.permission.CAMERA
    };

    /**
     * Permissions required for Audio / Video Call
     */
    public static final String[] CALL_PERMISSIONS = {
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
    };

    /**
     * Returns the appropriate media read permissions based on Android OS version.
     */
    public static String[] getMediaPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return new String[]{
                    Manifest.permission.READ_MEDIA_IMAGES,
                    Manifest.permission.READ_MEDIA_VIDEO,
                    Manifest.permission.READ_MEDIA_AUDIO
            };
        } else {
            return new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE
            };
        }
    }

    /**
     * Checks if all specified permissions are granted.
     */
    public static boolean hasPermissions(Context context, String... permissions) {
        if (context == null || permissions == null) {
            return false;
        }
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(context, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    public static boolean hasLocationPermissions(Context context) {
        return hasPermissions(context, LOCATION_PERMISSIONS);
    }

    public static boolean hasAudioPermission(Context context) {
        return hasPermissions(context, AUDIO_PERMISSIONS);
    }

    public static boolean hasCameraPermission(Context context) {
        return hasPermissions(context, CAMERA_PERMISSIONS);
    }

    public static boolean hasCallPermissions(Context context) {
        return hasPermissions(context, CALL_PERMISSIONS);
    }

    public static boolean hasMediaPermissions(Context context) {
        return hasPermissions(context, getMediaPermissions());
    }

    /**
     * Returns a list of permissions from the given array that are still denied.
     */
    public static List<String> getDeniedPermissions(Context context, String... permissions) {
        List<String> denied = new ArrayList<>();
        if (context != null && permissions != null) {
            for (String perm : permissions) {
                if (ContextCompat.checkSelfPermission(context, perm)
                        != PackageManager.PERMISSION_GRANTED) {
                    denied.add(perm);
                }
            }
        }
        return denied;
    }

    /**
     * Checks whether any of the denied permissions should show rationale.
     */
    public static boolean shouldShowRationale(Activity activity, String... permissions) {
        if (activity == null || permissions == null) return false;
        for (String perm : permissions) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, perm)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Displays an explanation dialog before requesting permission.
     */
    public static void showPermissionExplanationDialog(Activity activity,
                                                      String title,
                                                      String message,
                                                      Runnable onPositive) {
        if (activity == null || activity.isFinishing() || activity.isDestroyed()) return;
        new AlertDialog.Builder(activity)
                .setTitle(title != null ? title : activity.getString(R.string.permission_required))
                .setMessage(message)
                .setPositiveButton(R.string.btn_sign_up, (dialog, which) -> {
                    dialog.dismiss();
                    if (onPositive != null) onPositive.run();
                })
                .setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss())
                .show();
    }

    /**
     * Displays a dialog advising the user to grant permanently denied permissions in Settings.
     */
    public static void showSettingsRedirectDialog(Activity activity, String message) {
        if (activity == null || activity.isFinishing() || activity.isDestroyed()) return;
        new AlertDialog.Builder(activity)
                .setTitle(R.string.permission_required)
                .setMessage(message != null ? message : activity.getString(R.string.permission_denied_settings_prompt))
                .setPositiveButton(R.string.settings, (dialog, which) -> {
                    dialog.dismiss();
                    openAppSettings(activity);
                })
                .setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss())
                .show();
    }

    /**
     * Opens the application's details settings page in Android system.
     */
    public static void openAppSettings(Context context) {
        if (context == null) return;
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", context.getPackageName(), null);
        intent.setData(uri);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
}

