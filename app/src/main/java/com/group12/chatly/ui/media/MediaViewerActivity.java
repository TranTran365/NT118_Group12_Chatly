package com.group12.chatly.ui.media;

import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.github.chrisbanes.photoview.PhotoView;
import com.google.android.material.appbar.MaterialToolbar;
import com.group12.chatly.R;

/**
 * Activity for full-screen media inspection.
 * Supports pinch-to-zoom images via PhotoView and streaming playback via VideoView.
 */
public class MediaViewerActivity extends AppCompatActivity {

    public static final String EXTRA_MEDIA_URL = "extra_media_url";
    public static final String EXTRA_MEDIA_TYPE = "extra_media_type"; // "IMAGE" or "VIDEO"
    public static final String EXTRA_MEDIA_TITLE = "extra_media_title";

    public static final String TYPE_IMAGE = "IMAGE";
    public static final String TYPE_VIDEO = "VIDEO";

    private MaterialToolbar toolbar;
    private PhotoView photoView;
    private FrameLayout videoContainer;
    private VideoView videoView;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_viewer);

        toolbar = findViewById(R.id.toolbar_media);
        photoView = findViewById(R.id.photo_view);
        videoContainer = findViewById(R.id.layout_video_container);
        videoView = findViewById(R.id.video_view);
        progressBar = findViewById(R.id.pb_media_loading);

        String mediaUrl = getIntent().getStringExtra(EXTRA_MEDIA_URL);
        String mediaType = getIntent().getStringExtra(EXTRA_MEDIA_TYPE);
        String mediaTitle = getIntent().getStringExtra(EXTRA_MEDIA_TITLE);

        if (!TextUtils.isEmpty(mediaTitle)) {
            toolbar.setTitle(mediaTitle);
        }

        toolbar.setNavigationOnClickListener(v -> finish());

        if (TextUtils.isEmpty(mediaUrl)) {
            Toast.makeText(this, "Media URL is missing", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if (TYPE_VIDEO.equalsIgnoreCase(mediaType)) {
            displayVideo(mediaUrl);
        } else {
            displayImage(mediaUrl);
        }
    }

    private void displayImage(String url) {
        photoView.setVisibility(View.VISIBLE);
        videoContainer.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);

        Glide.with(this)
                .load(url)
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model,
                                                @NonNull Target<Drawable> target, boolean isFirstResource) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(MediaViewerActivity.this, "Failed to load image", Toast.LENGTH_SHORT).show();
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(@NonNull Drawable resource, @NonNull Object model,
                                                   Target<Drawable> target, @NonNull DataSource dataSource,
                                                   boolean isFirstResource) {
                        progressBar.setVisibility(View.GONE);
                        return false;
                    }
                })
                .into(photoView);
    }

    private void displayVideo(String url) {
        photoView.setVisibility(View.GONE);
        videoContainer.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.VISIBLE);

        MediaController mediaController = new MediaController(this);
        mediaController.setAnchorView(videoView);

        videoView.setMediaController(mediaController);
        videoView.setVideoURI(Uri.parse(url));

        videoView.setOnPreparedListener(mp -> {
            progressBar.setVisibility(View.GONE);
            videoView.start();
        });

        videoView.setOnErrorListener((mp, what, extra) -> {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(MediaViewerActivity.this, "Failed to play video", Toast.LENGTH_SHORT).show();
            return true;
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (videoView != null && videoView.isPlaying()) {
            videoView.pause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (videoView != null) {
            videoView.stopPlayback();
        }
    }
}

