/**
 * Copyright IBM Corporation 2016
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **/

package com.ibm.watson.developer_cloud.android.myapplication;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import com.ibm.watson.developer_cloud.android.library.audio.MicrophoneInputStream;
import com.ibm.watson.developer_cloud.http.ServiceCallback;
import com.ibm.watson.developer_cloud.language_translation.v2.LanguageTranslation;
import com.ibm.watson.developer_cloud.language_translation.v2.model.Language;
import com.ibm.watson.developer_cloud.language_translation.v2.model.TranslationResult;
import com.ibm.watson.developer_cloud.speech_to_text.v1.RecognizeOptions;
import com.ibm.watson.developer_cloud.speech_to_text.v1.SpeechToText;
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.SpeechResults;
import com.ibm.watson.developer_cloud.speech_to_text.v1.websocket.RecognizeCallback;
import com.ibm.watson.developer_cloud.text_to_speech.v1.TextToSpeech;
import com.ibm.watson.developer_cloud.text_to_speech.v1.model.Voice;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {
  private final String TAG = "MainActivity";

  private RadioGroup targetLanguage;
  private EditText input;
  private ImageButton mic;
  private Button translate;
  private ImageButton play;
  private TextView translatedText;

  private SpeechToText speechService;
  private TextToSpeech textService;
  private LanguageTranslation translationService;
  private Language selectedTargetLanguage = Language.SPANISH;

  private AudioTrack audioTrack;
  private static final int RECORDER_BPP = 16;
  private static final int RECORDER_SAMPLERATE = 48000;
  private static final int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_STEREO;
  private static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
  private Thread recordingThread;
  private int bufferSize;

  private MediaPlayer mPlayer;
  private int sampleRate;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    speechService = initSpeechToTextService();
    textService = initTextToSpeechService();
    translationService = initLanguageTranslationService();

    targetLanguage = (RadioGroup) findViewById(R.id.target_language);
    input = (EditText) findViewById(R.id.input);
    mic = (ImageButton) findViewById(R.id.mic);
    translate = (Button) findViewById(R.id.translate);
    play = (ImageButton) findViewById(R.id.play);
    translatedText = (TextView) findViewById(R.id.translated_text);

    targetLanguage.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
      @Override public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (checkedId) {
          case R.id.spanish:
            selectedTargetLanguage = Language.SPANISH;
            break;
          case R.id.french:
            selectedTargetLanguage = Language.FRENCH;
            break;
          case R.id.italian:
            selectedTargetLanguage = Language.ITALIAN;
            break;
        }
      }
    });

    input.addTextChangedListener(new EmptyTextWatcher() {
      @Override public void onEmpty(boolean empty) {
        if (empty) {
          translate.setEnabled(false);
        } else {
          translate.setEnabled(true);
        }
      }
    });

    mic.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        mic.setEnabled(false);

        new Thread(new Runnable() {
          @Override public void run() {
            try {
              //FileInputStream audio = new FileInputStream(getResources().);
              speechService.recognizeUsingWebSocket(new MicrophoneInputStream(),
                  getRecognizeOptions(), new MicrophoneRecognizeDelegate());
            } catch (Exception e) {
              showError(e);
            }
          }
        }).start();
      }
    });

    translate.setOnClickListener(new View.OnClickListener() {

      @Override public void onClick(View v) {
        translationService.translate(input.getText().toString(), Language.ENGLISH, selectedTargetLanguage)
            .enqueue(new ServiceCallback<TranslationResult>() {
              @Override public void onResponse(TranslationResult response) {
                showTranslation(response.getFirstTranslation());
              }

              @Override public void onFailure(Exception e) {
                showError(e);
              }
            });
      }
    });

    translatedText.addTextChangedListener(new EmptyTextWatcher() {
      @Override public void onEmpty(boolean empty) {
        if (empty) {
          play.setEnabled(false);
        } else {
          play.setEnabled(true);
        }
      }
    });

    play.setEnabled(false);

    play.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        textService.synthesize(translatedText.getText().toString(), Voice.EN_LISA)
            .enqueue(new ServiceCallback<InputStream>() {
              @Override public void onResponse(InputStream stream) {
                playTranscription(stream);
              }

              @Override public void onFailure(Exception e) {
                showError(e);
              }
            });
      }
    });
  }

  private void showTranslation(final String translation) {
    runOnUiThread(new Runnable() {
      @Override public void run() {
        translatedText.setText(translation);
      }
    });
  }

  private void showError(final Exception e) {
    runOnUiThread(new Runnable() {
      @Override public void run() {
        Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
        e.printStackTrace();
      }
    });
  }

  private void showMicText(final String text) {
    runOnUiThread(new Runnable() {
      @Override public void run() {
        input.setText(text);
      }
    });
  }

  private void enableMicButton() {
    runOnUiThread(new Runnable() {
      @Override public void run() {
        mic.setEnabled(true);
      }
    });
  }

  private void playTranscription(InputStream stream) {
        try {
          byte[] data = convertStreamToByteArray(stream);
          if (data.length > 28) {
            sampleRate = readInt(data, 24);
          }
          int headSize = 44, metaDataSize = 48;
          int destPos = headSize + metaDataSize;
          int rawLength = data.length - destPos;
          byte[] d = new byte[rawLength];
          System.arraycopy(data, destPos, d, 0, rawLength);
          initPlayer();
          audioTrack.write(d, 0, d.length);
          stream.close();
          if (audioTrack != null && audioTrack.getState() != AudioTrack.STATE_UNINITIALIZED) {
            audioTrack.release();
          }
        } catch (IOException e2) {
          Log.e(TAG, e2.getMessage());
        }
  }

  private SpeechToText initSpeechToTextService() {
    SpeechToText service = new SpeechToText();
    String username = getString(R.string.speech_text_username);
    String password = getString(R.string.speech_text_password);
    service.setUsernameAndPassword(username, password);
    return service;
  }

  private TextToSpeech initTextToSpeechService() {
    TextToSpeech service = new TextToSpeech();
    String username = getString(R.string.text_speech_username);
    String password = getString(R.string.text_speech_password);
    service.setUsernameAndPassword(username, password);
    return service;
  }

  private LanguageTranslation initLanguageTranslationService() {
    LanguageTranslation service = new LanguageTranslation();
    String username = getString(R.string.language_translation_username);
    String password = getString(R.string.language_translation_password);
    service.setUsernameAndPassword(username, password);
    return service;
  }

  private RecognizeOptions getRecognizeOptions() {
    RecognizeOptions.Builder options = new RecognizeOptions.Builder();
    options.continuous(true);
    options.contentType(MicrophoneInputStream.CONTENT_TYPE);
    options.interimResults(true);
    options.inactivityTimeout(2000);
    return options.build();
  }

  private abstract class EmptyTextWatcher implements TextWatcher {
    private boolean isEmpty = true; // assumes text is initially empty

    @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

    @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
      if (s.length() == 0) {
        isEmpty = true;
        onEmpty(true);
      } else if (isEmpty) {
        isEmpty = false;
        onEmpty(false);
      }
    }

    @Override public void afterTextChanged(Editable s) {}

    public abstract void onEmpty(boolean empty);
  }

  private class MicrophoneRecognizeDelegate implements RecognizeCallback {

    @Override public void onTranscription(SpeechResults speechResults) {
      String text = speechResults.getResults().get(0).getAlternatives().get(0).getTranscript();
      showMicText(text);
    }

    @Override public void onConnected() {

    }

    @Override public void onError(Exception e) {
      showError(e);
      enableMicButton();
    }

    @Override public void onDisconnected() {
      enableMicButton();
    }
  }

  /**
   * Initialize AudioTrack by getting buffersize
   */
  public void initPlayer() {
    synchronized (this) {
      int bs = AudioTrack.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);
      audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
          sampleRate,
          AudioFormat.CHANNEL_OUT_MONO,
          AudioFormat.ENCODING_PCM_16BIT,
          bs,
          AudioTrack.MODE_STREAM);
      if (audioTrack != null)
        audioTrack.play();
    }
  }

  private static byte[] convertStreamToByteArray(InputStream is) throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    byte[] buff = new byte[10240];
    int i;
    while ((i = is.read(buff, 0, buff.length)) > 0) {
      baos.write(buff, 0, i);
    }

    return baos.toByteArray();
  }

  protected static int readInt(final byte[] data, final int offset)  {
    return (data[offset] & 0xff) |
        ((data[offset+1] & 0xff) <<  8) |
        ((data[offset+2] & 0xff) << 16) |
        (data[offset+3] << 24); // no 0xff on the last one to keep the sign
  }

}
