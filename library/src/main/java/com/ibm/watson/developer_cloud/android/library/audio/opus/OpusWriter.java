/*
 * Copyright 2017 IBM Corp. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.ibm.watson.developer_cloud.android.library.audio.opus;

import com.ibm.watson.developer_cloud.android.library.audio.AudioConsumer;
import com.ibm.watson.developer_cloud.android.library.audio.AudioFileWriter;
import com.ibm.watson.developer_cloud.android.library.audio.utils.SpeechConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Random;

/**
 * The Class OpusWriter.
 */
public class OpusWriter extends AudioFileWriter {
  /**
   * Number of packets in an Ogg page (must be less than 255).
   */
  public static final int PACKETS_PER_OGG_PAGE = 50;
  /**
   * The audio consumer.
   */
  public AudioConsumer audioConsumer;
  /**
   * Defines the sampling rate of the audio input.
   */
  protected int sampleRate;

  /**
   * Ogg Stream Serial Number.
   */
  protected int streamSerialNumber;
  /**
   * Ogg Page count.
   */
  protected int pageCount;
  private String TAG = this.getClass().getSimpleName();
  /**
   * Data buffer
   */
  private byte[] dataBuffer;
  /**
   * Pointer within the Data buffer
   */
  private int dataBufferPtr;
  /**
   * Header buffer.
   */
  private byte[] headerBuffer;
  /**
   * Pointer within the Header buffer
   */
  private int headerBufferPtr;
  /**
   * Opus packet count within an Ogg Page
   */
  private int packetCount;
  /**
   * Absolute granule position
   * (the number of audio samples from beginning of file to end of Ogg Packet).
   */
  private long granulepos;
  /**
   * Frame size
   */
  private int frameSize;

  /**
   * Setting up the OggOpus Writer.
   */
  public OpusWriter() {
  }

  /**
   * Instantiates a new opus writer.
   *
   * @param ac the ac
   */
  public OpusWriter(AudioConsumer ac) {
    this.audioConsumer = ac;

    if (streamSerialNumber == 0)
      streamSerialNumber = new Random().nextInt();
    dataBuffer = new byte[65565];
    dataBufferPtr = 0;
    headerBuffer = new byte[255];
    headerBufferPtr = 0;
    pageCount = 0;
    packetCount = 0;
    granulepos = 0;
    this.sampleRate = SpeechConfiguration.SAMPLE_RATE;
    this.frameSize = SpeechConfiguration.FRAME_SIZE;
  }

  /**
   * Close.
   *
   * @throws IOException Signals that an I/O exception has occurred.
   */
  @Override
  public void close() throws IOException {
    flush(true);
  }

  /**
   * Open.
   *
   * @param file the file
   * @throws IOException Signals that an I/O exception has occurred.
   */
  @Override
  public void open(File file) throws IOException {
  }

  /**
   * Open.
   *
   * @param filename the filename
   * @throws IOException Signals that an I/O exception has occurred.
   */
  @Override
  public void open(String filename) throws IOException {
  }

  /**
   * Write header.
   *
   * @param comment the comment
   */
  @Override
  public void writeHeader(String comment) {
    byte[] header;
    byte[] data;
    int chkSum;

        /* writes the OGG header page */
    header = buildOggPageHeader(2, 0, streamSerialNumber, pageCount++, 1, new byte[]{19});
    data = buildOpusHeader(sampleRate);
    chkSum = OggCrc.checksum(0, header, 0, header.length);
    chkSum = OggCrc.checksum(chkSum, data, 0, data.length);
    writeInt(header, 22, chkSum);
    this.write(header);
    this.write(data);

        /* Writes the OGG comment page */
    header = buildOggPageHeader(0, 0, streamSerialNumber, pageCount++, 1, new byte[]{(byte) (comment.length() + 8)});
    data = buildOpusComment(comment);
    chkSum = OggCrc.checksum(0, header, 0, header.length);
    chkSum = OggCrc.checksum(chkSum, data, 0, data.length);
    writeInt(header, 22, chkSum);
    this.write(header);
    this.write(data);
  }

  /**
   * Write data packet.
   *
   * @param data   audio data
   * @param offset the offset from which to start reading the data.
   * @param len    the length of data to read.
   * @throws IOException Signals that an I/O exception has occurred.
   */
  @Override
  public void writePacket(byte[] data, int offset, int len)
          throws IOException {
    // if nothing to write
    if (len <= 0) {
      return;
    }
    if (packetCount > PACKETS_PER_OGG_PAGE) {
      flush(false);
    }
    System.arraycopy(data, offset, dataBuffer, dataBufferPtr, len);
    dataBufferPtr += len;
    headerBuffer[headerBufferPtr++] = (byte) len;
    packetCount++;
    granulepos += this.frameSize * 2;
  }

  /**
   * Flush the Ogg page out of the buffers into the file.
   *
   * @param eos - end of stream
   * @throws IOException Signals that an I/O exception has occurred.
   */
  protected void flush(final boolean eos) throws IOException {
    int chksum;
    byte[] header;
        /* Writes the OGG header page */
    header = buildOggPageHeader((eos ? 4 : 0), granulepos, streamSerialNumber, pageCount++, packetCount, headerBuffer);
    chksum = OggCrc.checksum(0, header, 0, header.length);
    chksum = OggCrc.checksum(chksum, dataBuffer, 0, dataBufferPtr);
    writeInt(header, 22, chksum);

    this.write(header);
    this.write(dataBuffer, 0, dataBufferPtr);

    dataBufferPtr = 0;
    headerBufferPtr = 0;
    packetCount = 0;
  }

  /**
   * Write data into Socket.
   *
   * @param data the data
   */
  public void write(byte[] data) {
    this.audioConsumer.consume(data);
  }

  /**
   * Write data into Socket.
   *
   * @param data   the data
   * @param offset the offset
   * @param count  the count
   */
  public void write(byte[] data, int offset, int count) {
    byte[] tmp = new byte[count];
    System.arraycopy(data, offset, tmp, 0, count);

    this.audioConsumer.consume(tmp);

  }
}
