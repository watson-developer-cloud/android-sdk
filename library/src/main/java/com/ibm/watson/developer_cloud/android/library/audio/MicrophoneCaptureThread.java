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

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

import com.ibm.watson.developer_cloud.android.library.audio.opus.OggOpusEnc;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Dedicated thread for capturing raw audio data from the microphone. Captured data is passed to an
 * {@link AudioConsumer}. To begin capturing data, call . Ensure  is called to stop this
 * thread from running and to clean up its resources appropriately.
 */
final class MicrophoneCaptureThread implements Runnable {
  private static final String TAG = MicrophoneCaptureThread.class.getName();
  private static final int SAMPLE_RATE = 16000;
  private final AudioConsumer consumer;
  private boolean opusEncoded;
  private OggOpusEnc encoder;
  private AudioRecord record;
  /** Used to signal when the thread is complete. */
  private ReentrantLock running = new ReentrantLock();

  private PipedOutputStream os;
  private PipedInputStream is;


  private boolean doStop = false;

  public synchronized void doStop() {
    this.doStop = true;
  }

  private synchronized boolean keepRunning() {
    return this.doStop == false;
  }

  /**
   * This only initializes data associated with the thread. To start recording microphone data, call .
   * Ensure that there is a corresponding call to  when finished recording data.
   *
   * @param consumer    Delegate for consuming audio data from the microphone.
   * @param opusEncoded the opus encoded
   */
  public MicrophoneCaptureThread(AudioConsumer consumer, boolean opusEncoded) {
    android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
    this.consumer = consumer;
    this.opusEncoded = opusEncoded;
  }

  /**
   * Run.
   */
  @Override
  public void run() {
    int bufferSize = Math.max(SAMPLE_RATE / 2,
            AudioRecord.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT));
    short[] buffer = new short[bufferSize]; // use short to hold 16-bit PCM encoding

    record = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT, bufferSize);
    record.startRecording();

    if (opusEncoded) {
      try {
        encoder = new OggOpusEnc(consumer);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

    try {
      //running.lock();
      while (keepRunning()) {
        try {
          int r = record.read(buffer, 0, buffer.length);

          // calculate amplitude and volume
          long v = 0;
          for (int i = 0; i < r; i++) {
            v += buffer[i] * buffer[i];
          }

          double amplitude = v / (double) r;
          double volume = 0;
          if (amplitude > 0) {
            volume = 10 * Math.log10(amplitude);
          }

          // convert short buffer to bytes
          ByteBuffer bufferBytes = ByteBuffer.allocate(r * 2); // 2 bytes per short
          bufferBytes.order(ByteOrder.LITTLE_ENDIAN); // save little-endian byte from short buffer
          bufferBytes.asShortBuffer().put(buffer, 0, r);
          byte[] bytes = bufferBytes.array();

          if (opusEncoded) {
            encoder.onStart(); // must be called before writing
            encoder.encodeAndWrite(bytes);

          } else {
            consumer.consume(bytes, amplitude, volume);
          }
        } catch (IOException e) {
          e.printStackTrace();
        }
      }

      if (encoder != null) {
        encoder.close();
      }
      record.stop();
      record.release();
    } finally {

    }
  }
}
