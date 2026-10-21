package com.group12.chatly.multimedia.voice;

import android.content.Context;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.io.File;
import java.io.IOException;

/**
 * Manager class responsible for recording voice messages in MPEG_4 / AAC format (.m4a).
 * Features include:
 * - Max duration limitation (up to 2 minutes)
 * - Realtime duration and amplitude polling for waveform display
 * - Minimum duration validation (discarding < 1 second clips)
 * - Safe release and cancellation handling
 */
public class VoiceRecorderManager {

    private static final String TAG = "VoiceRecorderManager";
    public static final int MAX_RECORD_DURATION_MS = 120_000; // 2 minutes max
    public static final int MIN_RECORD_DURATION_MS = 1_000;   // 1 second min

    public interface RecordListener {
        void onRecordStart();
        void onRecordTick(long elapsedMs, int amplitude);
        void onRecordCompleted(String filePath, long durationMs);
        void onRecordCanceled(String reason);
        void onRecordError(String errorMessage);
    }

    private MediaRecorder recorder;
    private String outputFilePath;
    private boolean isRecording = false;
    private long recordStartTime = 0L;
    private RecordListener listener;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable tickerRunnable = new Runnable() {
        @Override
        public void run() {
            if (isRecording && recorder != null) {
                long elapsed = System.currentTimeMillis() - recordStartTime;
                int amplitude = 0;
                try {
                    amplitude = recorder.getMaxAmplitude();
                } catch (Exception ignored) {
                }

                if (listener != null) {
                    listener.onRecordTick(elapsed, amplitude);
                }

                if (elapsed >= MAX_RECORD_DURATION_MS) {
                    stopRecording();
                    return;
                }

                handler.postDelayed(this, 100);
            }
        }
    };

    public void setRecordListener(RecordListener listener) {
        this.listener = listener;
    }

    /**
     * Starts voice recording with high quality AAC encoding.
     */
    public synchronized boolean startRecording(Context context) {
        if (isRecording) {
            cancelRecording();
        }

        try {
            File cacheFile = new File(context.getCacheDir(), "voice_" + System.currentTimeMillis() + ".m4a");
            outputFilePath = cacheFile.getAbsolutePath();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                recorder = new MediaRecorder(context);
            } else {
                recorder = new MediaRecorder();
            }

            recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            recorder.setAudioEncodingBitRate(128000);
            recorder.setAudioSamplingRate(44100);
            recorder.setOutputFile(outputFilePath);

            recorder.prepare();
            recorder.start();

            isRecording = true;
            recordStartTime = System.currentTimeMillis();

            handler.post(tickerRunnable);

            if (listener != null) {
                listener.onRecordStart();
            }
            return true;
        } catch (IOException | IllegalStateException e) {
            Log.e(TAG, "Failed to start recording: " + e.getMessage(), e);
            releaseRecorder();
            if (listener != null) {
                listener.onRecordError("Failed to initialize audio recorder: " + e.getMessage());
            }
            return false;
        }
    }

    /**
     * Stops the recording, checks minimum duration, and returns the file path.
     */
    public synchronized String stopRecording() {
        if (!isRecording || recorder == null) {
            return null;
        }

        long duration = System.currentTimeMillis() - recordStartTime;
        handler.removeCallbacks(tickerRunnable);

        try {
            recorder.stop();
        } catch (RuntimeException e) {
            Log.w(TAG, "Recorder stop error (recording was likely too short or interrupted): " + e.getMessage());
        }

        releaseRecorder();

        if (duration < MIN_RECORD_DURATION_MS) {
            cancelRecordingFile();
            if (listener != null) {
                listener.onRecordCanceled("Too short");
            }
            return null;
        }

        String savedPath = outputFilePath;
        if (listener != null) {
            listener.onRecordCompleted(savedPath, duration);
        }
        return savedPath;
    }

    /**
     * Cancels the current recording and removes the temp file (e.g. slide to cancel).
     */
    public synchronized void cancelRecording() {
        if (!isRecording) return;
        handler.removeCallbacks(tickerRunnable);

        try {
            if (recorder != null) {
                recorder.stop();
            }
        } catch (Exception ignored) {
        }

        releaseRecorder();
        cancelRecordingFile();

        if (listener != null) {
            listener.onRecordCanceled("Canceled by user");
        }
    }

    private void cancelRecordingFile() {
        if (outputFilePath != null) {
            File file = new File(outputFilePath);
            if (file.exists()) {
                //noinspection ResultOfMethodCallIgnored
                file.delete();
            }
            outputFilePath = null;
        }
    }

    private void releaseRecorder() {
        isRecording = false;
        if (recorder != null) {
            try {
                recorder.release();
            } catch (Exception ignored) {
            }
            recorder = null;
        }
    }

    public boolean isRecording() {
        return isRecording;
    }

    public long getElapsedDurationMs() {
        if (!isRecording) return 0L;
        return System.currentTimeMillis() - recordStartTime;
    }

    public String getOutputFilePath() {
        return outputFilePath;
    }
}

