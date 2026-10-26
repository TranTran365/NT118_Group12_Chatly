package com.group12.chatly.ui.media;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.group12.chatly.R;
import com.group12.chatly.multimedia.location.LocationHelper;

import java.util.Locale;

/**
 * Activity for interactive Google Maps inspection and external navigation.
 */
public class LocationViewerActivity extends AppCompatActivity implements OnMapReadyCallback {

    public static final String EXTRA_LATITUDE = "extra_latitude";
    public static final String EXTRA_LONGITUDE = "extra_longitude";
    public static final String EXTRA_TITLE = "extra_title";

    private double latitude = 0.0;
    private double longitude = 0.0;
    private String locationTitle;

    private TextView tvLocationTitle;
    private TextView tvCoordinates;
    private MaterialButton btnNavigate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_viewer);

        latitude = getIntent().getDoubleExtra(EXTRA_LATITUDE, 0.0);
        longitude = getIntent().getDoubleExtra(EXTRA_LONGITUDE, 0.0);
        locationTitle = getIntent().getStringExtra(EXTRA_TITLE);

        MaterialToolbar toolbar = findViewById(R.id.toolbar_location);
        tvLocationTitle = findViewById(R.id.tv_location_title);
        tvCoordinates = findViewById(R.id.tv_coordinates);
        btnNavigate = findViewById(R.id.btn_navigate);

        toolbar.setNavigationOnClickListener(v -> finish());

        if (!TextUtils.isEmpty(locationTitle)) {
            tvLocationTitle.setText(locationTitle);
            toolbar.setTitle(locationTitle);
        } else {
            locationTitle = getString(R.string.current_location_marker);
        }

        tvCoordinates.setText(String.format(Locale.US, "Lat: %.6f, Lng: %.6f", latitude, longitude));

        btnNavigate.setOnClickListener(v -> {
            LocationHelper.openNavigationIntent(this, latitude, longitude, locationTitle);
        });

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_fragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        if (googleMap == null) return;

        LatLng targetLocation = new LatLng(latitude, longitude);

        googleMap.addMarker(new MarkerOptions()
                .position(targetLocation)
                .title(locationTitle));

        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(targetLocation, 16.0f));
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.getUiSettings().setCompassEnabled(true);
    }
}

