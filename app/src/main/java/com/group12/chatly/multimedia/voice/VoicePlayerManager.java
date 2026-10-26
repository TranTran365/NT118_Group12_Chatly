package com.group12.chatly.multimedia.voice;

import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.io.IOException;

/**
 * Manager class responsible for audio playback of voice messages.
 * Features:
 * - Singleton pattern to guarantee only one voice is played at any time
 * - Progress tracking callbacks for seekbar and waveform synchronization
 * - Support for local files and remote streaming URLs
 * - Lifecycle-aware cleanup
 */
public class VoicePlayerManager {

    private static final String TAG = "VoicePlayerManager";
    private static VoicePlayerManager instance;

    public interface PlaybackListener {
        void onPlayStart(int totalDurationMs);
        void onPlayProgress(int currentPositionMs, int totalDurationMs);
        void onPlayPause();
        void onPlayResume();
        void onPlayCompleted();
        void onPlayError(String errorMessage);
    }

    private MediaPlayer mediaPlayer;
    private String currentAudioSource;
    private boolean isPlaying = false;
    private PlaybackListener currentListener;

    private final Handler progressHandler = new Handler(Looper.getMainLooper());
    private final Runnable progressRunnable = new Runnable() {
        @Override
        public void run() {
            if (mediaPlayer != null && isPlaying) {
                try {
                    int current = mediaPlayer.getCurrentPosition();
                    int total = mediaPlayer.getDuration();
                    if (currentListener != null) {
                        currentListener.onPlayProgress(current, total);
                    }
                    progressHandler.postDelayed(this, 100);
                } catch (IllegalStateException ignored) {
                }
            }
        }
    };

    public static synchronized VoicePlayerManager getInstance() {
        if (instance == null) {
            instance = new VoicePlayerManager();
        }
        return instance;
    }

    private VoicePlayerManager() {
    }

    /**
     * Plays audio from local file path or remote URL.
     * Stops any currently playing audio before starting.
     */
    public synchronized void playAudio(String audioSource, PlaybackListener listener) {
        if (audioSource == null) return;

        // If clicking on same audio that is currently paused, resume it
        if (audioSource.equals(currentAudioSource) && mediaPlayer != null) {
            if (!isPlaying) {
                resumeAudio();
                return;
            } else {
                pauseAudio();
                return;
            }
        }

        stopAudio();
        this.currentAudioSource = audioSource;
        this.currentListener = listener;

        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioAttributes(
                    new AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .build()
            );

            mediaPlayer.setDataSource(audioSource);
            mediaPlayer.setOnPreparedListener(mp -> {
                try {
                    mp.start();
                    isPlaying = true;
                    if (currentListener != null) {
                        currentListener.onPlayStart(mp.getDuration());
                    }
                    progressHandler.post(progressRunnable);
                } catch (Exception e) {
                    handlePlaybackError("Failed to start playback: " + e.getMessage());
                }
            });

            mediaPlayer.setOnCompletionListener(mp -> {
                isPlaying = false;
                progressHandler.removeCallbacks(progressRunnable);
                if (currentListener != null) {
                    currentListener.onPlayCompleted();
                }
                stopAudio();
            });

            mediaPlayer.setOnErrorListener((mp, what, extra) -> {
                handlePlaybackError("MediaPlayer error (code: " + what + ", extra: " + extra + ")");
                return true;
            });

            mediaPlayer.prepareAsync();
        } catch (IOException | IllegalStateException e) {
            handlePlaybackError("Could not play audio source: " + e.getMessage());
        }
    }

    public synchronized void pauseAudio() {
        if (mediaPlayer != null && isPlaying) {
            try {
                mediaPlayer.pause();
                isPlaying = false;
                progressHandler.removeCallbacks(progressRunnable);
                if (currentListener != null) {
                    currentListener.onPlayPause();
                }
            } catch (Exception ignored) {
            }
        }
    }

    public synchronized void resumeAudio() {
        if (mediaPlayer != null && !isPlaying) {
            try {
                mediaPlayer.start();
                isPlaying = true;
                progressHandler.post(progressRunnable);
                if (currentListener != null) {
                    currentListener.onPlayResume();
                }
            } catch (Exception ignored) {
            }
        }
    }

    public synchronized void seekTo(int positionMs) {
        if (mediaPlayer != null) {
            try {
                mediaPlayer.seekTo(positionMs);
            } catch (Exception ignored) {
            }
        }
    }

    public synchronized void stopAudio() {
        progressHandler.removeCallbacks(progressRunnable);
        isPlaying = false;
        currentAudioSource = null;
        if (mediaPlayer != null) {
            try {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                }
                mediaPlayer.reset();
                mediaPlayer.release();
            } catch (Exception ignored) {
            }
            mediaPlayer = null;
        }
    }

    private void handlePlaybackError(String errorMsg) {
        Log.e(TAG, errorMsg);
        stopAudio();
        if (currentListener != null) {
            currentListener.onPlayError(errorMsg);
        }
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public String getCurrentAudioSource() {
        return currentAudioSource;
    }
}

