package com.group12.chatly.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.MimeTypeMap;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Locale;

/**
 * Utility class for reading file metadata, copying URIs to temporary cache,
 * and managing application cache cleanup for multimedia files.
 */
public final class FileUtils {

    private static final String TAG = "FileUtils";
    private static final long MAX_CACHE_AGE_MS = 24 * 60 * 60 * 1000L; // 24 hours

    private FileUtils() {
        // Private constructor
    }

    /**
     * File Metadata holder class.
     */
    public static class FileMetaData {
        private final String fileName;
        private final long fileSize;
        private final String mimeType;
        private final long durationMs;

        public FileMetaData(String fileName, long fileSize, String mimeType, long durationMs) {
            this.fileName = fileName;
            this.fileSize = fileSize;
            this.mimeType = mimeType;
            this.durationMs = durationMs;
        }

        public String getFileName() {
            return fileName;
        }

        public long getFileSize() {
            return fileSize;
        }

        public String getMimeType() {
            return mimeType;
        }

        public long getDurationMs() {
            return durationMs;
        }

        public String getFormattedSize() {
            return formatFileSize(fileSize);
        }
    }

    /**
     * Extracts metadata (name, size, mimeType, duration) for a given Uri.
     */
    public static FileMetaData getFileMetaData(Context context, Uri uri) {
        if (context == null || uri == null) {
            return new FileMetaData("unknown", 0L, "application/octet-stream", 0L);
        }

        String fileName = queryFileName(context, uri);
        long fileSize = queryFileSize(context, uri);
        String mimeType = getMimeType(context, uri);
        long durationMs = 0L;

        if (mimeType != null && (mimeType.startsWith("audio/") || mimeType.startsWith("video/"))) {
            durationMs = extractMediaDuration(context, uri);
        }

        return new FileMetaData(fileName, fileSize, mimeType, durationMs);
    }

    /**
     * Resolves display name of the Uri.
     */
    public static String queryFileName(Context context, Uri uri) {
        if (uri == null) return "unknown";
        String result = null;
        if (ContentResolver.SCHEME_CONTENT.equals(uri.getScheme())) {
            try (Cursor cursor = context.getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (nameIndex != -1) {
                        result = cursor.getString(nameIndex);
                    }
                }
            } catch (Exception e) {
                Log.w(TAG, "Error querying file name: " + e.getMessage());
            }
        }
        if (result == null) {
            result = uri.getLastPathSegment();
        }
        return result != null ? result : "file_" + System.currentTimeMillis();
    }

    /**
     * Resolves file size in bytes from Uri.
     */
    public static long queryFileSize(Context context, Uri uri) {
        if (uri == null) return 0L;
        long size = 0L;
        if (ContentResolver.SCHEME_CONTENT.equals(uri.getScheme())) {
            try (Cursor cursor = context.getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
                    if (sizeIndex != -1 && !cursor.isNull(sizeIndex)) {
                        size = cursor.getLong(sizeIndex);
                    }
                }
            } catch (Exception e) {
                Log.w(TAG, "Error querying file size: " + e.getMessage());
            }
        } else if (ContentResolver.SCHEME_FILE.equals(uri.getScheme())) {
            String path = uri.getPath();
            if (path != null) {
                File file = new File(path);
                if (file.exists()) {
                    size = file.length();
                }
            }
        }
        return size;
    }

    /**
     * Retrieves MIME type of Uri.
     */
    public static String getMimeType(Context context, Uri uri) {
        if (uri == null) return "application/octet-stream";
        String mimeType = null;
        if (ContentResolver.SCHEME_CONTENT.equals(uri.getScheme())) {
            mimeType = context.getContentResolver().getType(uri);
        } else {
            String fileExtension = MimeTypeMap.getFileExtensionFromUrl(uri.toString());
            if (!TextUtils.isEmpty(fileExtension)) {
                mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension.toLowerCase(Locale.ROOT));
            }
        }
        return mimeType != null ? mimeType : "application/octet-stream";
    }

    /**
     * Extracts duration in milliseconds for audio/video media.
     */
    public static long extractMediaDuration(Context context, Uri uri) {
        try (MediaMetadataRetriever retriever = new MediaMetadataRetriever()) {
            retriever.setDataSource(context, uri);
            String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            if (time != null) {
                return Long.parseLong(time);
            }
        } catch (Exception e) {
            Log.w(TAG, "Error extracting media duration: " + e.getMessage());
        }
        return 0L;
    }

    /**
     * Copies Uri stream content to a temporary cache file.
     */
    public static File copyUriToCacheFile(Context context, Uri uri, String prefix, String extension) {
        if (context == null || uri == null) return null;
        try {
            String fileName = (prefix != null ? prefix : "temp_") + System.currentTimeMillis() + (extension != null ? extension : ".tmp");
            File tempFile = new File(context.getCacheDir(), fileName);
            try (InputStream in = context.getContentResolver().openInputStream(uri);
                 OutputStream out = new FileOutputStream(tempFile)) {
                if (in == null) return null;
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
                out.flush();
            }
            return tempFile;
        } catch (Exception e) {
            Log.e(TAG, "Failed to copy Uri to cache file: " + e.getMessage(), e);
            return null;
        }
    }

    /**
     * Formats bytes into human-readable representation (B, KB, MB, GB).
     */
    public static String formatFileSize(long bytes) {
        if (bytes <= 0) return "0 B";
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(bytes) / Math.log10(1024));
        if (digitGroups >= units.length) digitGroups = units.length - 1;
        return String.format(Locale.US, "%.1f %s", bytes / Math.pow(1024, digitGroups), units[digitGroups]);
    }

    /**
     * Cleans up temporary multimedia files older than 24 hours from the cache directory.
     */
    public static void cleanupOldTempFiles(Context context) {
        if (context == null) return;
        File cacheDir = context.getCacheDir();
        if (cacheDir != null && cacheDir.isDirectory()) {
            File[] files = cacheDir.listFiles();
            if (files != null) {
                long now = System.currentTimeMillis();
                for (File file : files) {
                    if (now - file.lastModified() > MAX_CACHE_AGE_MS) {
                        try {
                            //noinspection ResultOfMethodCallIgnored
                            file.delete();
                        } catch (Exception ignored) {
                        }
                    }
                }
            }
        }
    }

    /**
     * Deletes a specific temporary file after uploading to remote storage.
     */
    public static boolean deleteFile(String filePath) {
        if (TextUtils.isEmpty(filePath)) return false;
        try {
            File file = new File(filePath);
            return file.exists() && file.delete();
        } catch (Exception e) {
            Log.w(TAG, "Failed to delete file " + filePath + ": " + e.getMessage());
            return false;
        }
    }
}

