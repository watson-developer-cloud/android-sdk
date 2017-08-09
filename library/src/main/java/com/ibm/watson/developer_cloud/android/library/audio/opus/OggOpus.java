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

package com.ibm.watson.developer_cloud.android.library.audio.opus;

/**
 * The Class OggOpus.
 */
public class OggOpus {

  static {
    System.loadLibrary("oggopus");
  }

  /**
   * Inits the audio.
   */
  public static native void initAudio();

  /**
   * Start recorder.
   *
   * @param sample_rate the sample rate
   */
  public static native void startRecorder(int sample_rate);

  /**
   * Stop recorder.
   */
  public static native void stopRecorder();

  /**
   * Encode.
   *
   * @param s           the s
   * @param sample_rate the sample rate
   * @return the int
   */
  public static native int encode(String s, int sample_rate);

  /**
   * Decode.
   *
   * @param s           the s
   * @param o           the o
   * @param sample_rate the sample rate
   * @return the int
   */
  public static native int decode(String s, String o, int sample_rate);

  /**
   * Volume.
   *
   * @return the float
   */
  public static native float volume();
}
