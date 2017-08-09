/*
 * Copyright 2017 IBM Corp. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package com.ibm.watson.developer_cloud.android.library.audio;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

/**
 * The Class MicrophoneHelper.
 */
public class MicrophoneHelper {

  /**
   * The Constant REQUEST_PERMISSION.
   */
  public static final int REQUEST_PERMISSION = 5000;

  private Activity activity;
  private MicrophoneInputStream inputStream;

  /**
   * Small helper class to sit in between the client and the more in-depth microphone classes. Meant to provide a
   * similar experience to the other Helper classes as well as handle permissions.
   *
   * @param activity The current activity
   */
  public MicrophoneHelper(Activity activity) {
    this.activity = activity;
    requestRecordingPermission();
  }

  /**
   * Gets the input stream.
   *
   * @param opusEncoded the opus encoded
   * @return the input stream
   */
  public MicrophoneInputStream getInputStream(boolean opusEncoded) {
    this.inputStream = new MicrophoneInputStream(opusEncoded);
    return this.inputStream;
  }

  /**
   * Close input stream.
   */
  public void closeInputStream() {
    try {
      this.inputStream.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void requestRecordingPermission() {
    if (ContextCompat.checkSelfPermission(activity,
            Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
      ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.RECORD_AUDIO},
              REQUEST_PERMISSION);
    }
  }

}
