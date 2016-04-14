package com.ibm.watson.developer_cloud.android.library.audio;

/**
 * Delegate for consuming audio data from {@link MicrophoneCaptureThread}.
 */
interface AudioConsumer {
  /**
   * Data that has been recorded in the most recent sample and is ready for consumption.
   *
   * @param data Buffer of audio data in raw form.
   * @param amplitude Amplitude from sample.
   * @param volume Volume from sample.
   */
  void consume(byte[] data, double amplitude, double volume);
}
