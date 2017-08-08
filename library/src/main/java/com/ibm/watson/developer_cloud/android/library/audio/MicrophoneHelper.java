package com.ibm.watson.developer_cloud.android.library.audio;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

/**
 * Created by lpatino on 8/7/17.
 */

public class MicrophoneHelper {

    public static final int REQUEST_PERMISSION = 5000;

    private Activity activity;
    private MicrophoneInputStream inputStream;

    /**
     * Small helper class to sit in between the client and the more in-depth microphone classes.
     * Meant to provide a similar experience to the other Helper classes as well as handle
     * permissions.
     * @param activity The current activity
     */
    public MicrophoneHelper(Activity activity) {
        this.activity = activity;
        requestRecordingPermission();
    }

    public MicrophoneInputStream getInputStream(boolean opusEncoded) {
        this.inputStream = new MicrophoneInputStream(opusEncoded);
        return this.inputStream;
    }

    public void closeInputStream() {
        try {
            this.inputStream.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void requestRecordingPermission() {
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_PERMISSION);
        }
    }

}
