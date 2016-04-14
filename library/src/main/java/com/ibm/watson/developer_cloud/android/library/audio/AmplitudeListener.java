package com.ibm.watson.developer_cloud.android.library.audio;

/**
 * Receives amplitude and volume data per sample from {@link MicrophoneInputStream}.
 */
public interface AmplitudeListener {
  /**
   * Amplitude and volume data from the audio sample.
   *
   * @param amplitude Amplitude from sample.
   * @param volume Volume from sample.
   */
  void onSample(double amplitude, double volume);
}
