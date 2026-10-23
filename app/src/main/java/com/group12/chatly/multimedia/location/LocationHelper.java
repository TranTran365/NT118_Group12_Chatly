package com.group12.chatly.multimedia.location;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationTokenSource;
import com.group12.chatly.utils.PermissionHelper;

import java.util.Locale;

/**
 * Helper class for retrieving GPS coordinates using FusedLocationProviderClient
 * and building map previews or navigation intents.
 */
public class LocationHelper {

    private static final String TAG = "LocationHelper";

    public interface LocationCallback {
        void onLocationSuccess(double latitude, double longitude, float accuracy);
        void onLocationError(String errorMessage);
    }

    private final Context context;
    private final FusedLocationProviderClient fusedLocationClient;

    public LocationHelper(Context context) {
        this.context = context.getApplicationContext();
        this.fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
    }

    /**
     * Checks whether GPS or Network location providers are enabled on the device.
     */
    public static boolean isGpsEnabled(Context context) {
        if (context == null) return false;
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (locationManager == null) return false;
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    /**
     * Retrieves high-accuracy current location using FusedLocationProviderClient.
     */
    @SuppressLint("MissingPermission")
    public void getCurrentLocation(@NonNull LocationCallback callback) {
        if (!PermissionHelper.hasLocationPermissions(context)) {
            callback.onLocationError("Location permission is not granted.");
            return;
        }

        if (!isGpsEnabled(context)) {
            callback.onLocationError("GPS is disabled. Please enable Location in device settings.");
            return;
        }

        CancellationTokenSource cancellationTokenSource = new CancellationTokenSource();

        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cancellationTokenSource.getToken())
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        callback.onLocationSuccess(location.getLatitude(), location.getLongitude(), location.getAccuracy());
                    } else {
                        // Fallback to last known location if immediate fix is null
                        getLastKnownLocation(callback);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Failed to get current location: " + e.getMessage());
                    getLastKnownLocation(callback);
                });
    }

    @SuppressLint("MissingPermission")
    private void getLastKnownLocation(@NonNull LocationCallback callback) {
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(lastLocation -> {
                    if (lastLocation != null) {
                        callback.onLocationSuccess(lastLocation.getLatitude(), lastLocation.getLongitude(), lastLocation.getAccuracy());
                    } else {
                        callback.onLocationError("Unable to obtain GPS fix. Try again outdoors.");
                    }
                })
                .addOnFailureListener(e -> callback.onLocationError("Location error: " + e.getMessage()));
    }

    /**
     * Constructs a Google Static Maps thumbnail URL for previewing in chat messages.
     */
    public static String buildStaticMapUrl(double latitude, double longitude, int width, int height, String apiKey) {
        return String.format(Locale.US,
                "https://maps.googleapis.com/maps/api/staticmap?center=%f,%f&zoom=16&size=%dx%d&markers=color:red%%7C%f,%f&key=%s",
                latitude, longitude, width, height, latitude, longitude, apiKey != null ? apiKey : "");
    }

    /**
     * Constructs an OpenStreetMap static thumbnail fallback (requires no API key).
     */
    public static String buildOsmStaticMapUrl(double latitude, double longitude, int width, int height) {
        return String.format(Locale.US,
                "https://static-maps.yandex.ru/1.x/?ll=%f,%f&z=16&l=map&size=%d,%d&pt=%f,%f,pm2rdm",
                longitude, latitude, width, height, longitude, latitude);
    }

    /**
     * Launches external Google Maps or browser with navigation intent to coordinates.
     */
    public static void openNavigationIntent(Context context, double latitude, double longitude, String label) {
        if (context == null) return;
        String query = String.format(Locale.US, "%f,%f", latitude, longitude);
        if (label != null && !label.isEmpty()) {
            query = String.format(Locale.US, "%f,%f(%s)", latitude, longitude, Uri.encode(label));
        }
        Uri uri = Uri.parse("geo:" + latitude + "," + longitude + "?q=" + query);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        try {
            context.startActivity(intent);
        } catch (Exception e) {
            // Fallback to web browser Google Maps URL
            Uri webUri = Uri.parse(String.format(Locale.US, "https://www.google.com/maps/search/?api=1&query=%f,%f", latitude, longitude));
            Intent webIntent = new Intent(Intent.ACTION_VIEW, webUri);
            webIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(webIntent);
        }
    }

    /**
     * Opens device location settings page.
     */
    public static void openLocationSettings(Context context) {
        if (context == null) return;
        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
}

