package com.group12.chatly;

import com.group12.chatly.multimedia.call.ZegoConfig;
import com.group12.chatly.multimedia.location.LocationHelper;
import com.group12.chatly.multimedia.voice.VoiceRecorderManager;
import com.group12.chatly.utils.FileUtils;
import com.group12.chatly.utils.ImageCompressor;
import com.group12.chatly.utils.PermissionHelper;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests covering Member 3 Multimedia logic.
 */
public class MultimediaUnitTest {

    @Test
    public void testFormatFileSize() {
        assertEquals("0 B", FileUtils.formatFileSize(0));
        assertEquals("500.0 B", FileUtils.formatFileSize(500));
        assertEquals("1.0 KB", FileUtils.formatFileSize(1024));
        assertEquals("1.5 MB", FileUtils.formatFileSize((long) (1.5 * 1024 * 1024)));
    }

    @Test
    public void testInSampleSizeCalculation() {
        // If image is within bounds, sample size should be 1
        int sample1 = ImageCompressor.calculateInSampleSize(800, 600, 1000, 1000);
        assertEquals(1, sample1);

        // If image is twice as large, sample size should be >= 2
        int sample2 = ImageCompressor.calculateInSampleSize(2000, 2000, 1000, 1000);
        assertEquals(2, sample2);

        // If image is 4x as large, sample size should be 4
        int sample3 = ImageCompressor.calculateInSampleSize(4000, 4000, 1000, 1000);
        assertEquals(4, sample3);
    }

    @Test
    public void testStaticMapUrlBuilder() {
        String url = LocationHelper.buildStaticMapUrl(10.762622, 106.660172, 400, 250, "test_api_key");
        assertNotNull(url);
        assertTrue(url.contains("maps.googleapis.com"));
        assertTrue(url.contains("10.762622"));
        assertTrue(url.contains("106.660172"));
        assertTrue(url.contains("test_api_key"));
    }

    @Test
    public void testOsmStaticMapUrlBuilder() {
        String url = LocationHelper.buildOsmStaticMapUrl(10.762622, 106.660172, 400, 250);
        assertNotNull(url);
        assertTrue(url.contains("10.762622"));
        assertTrue(url.contains("106.660172"));
    }

    @Test
    public void testVoiceRecorderConstants() {
        assertEquals(120_000, VoiceRecorderManager.MAX_RECORD_DURATION_MS);
        assertEquals(1_000, VoiceRecorderManager.MIN_RECORD_DURATION_MS);
    }

    @Test
    public void testPermissionsConstants() {
        assertTrue(PermissionHelper.LOCATION_PERMISSIONS.length >= 2);
        assertTrue(PermissionHelper.CALL_PERMISSIONS.length >= 2);
        assertTrue(PermissionHelper.AUDIO_PERMISSIONS.length >= 1);
    }
}

