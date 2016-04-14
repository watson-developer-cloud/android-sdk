package com.ibm.watson.developer_cloud.android.myapplication;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatImageButton;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import com.ibm.watson.developer_cloud.android.library.audio.MicrophoneInputStream;
import com.ibm.watson.developer_cloud.language_translation.v2.LanguageTranslation;
import com.ibm.watson.developer_cloud.language_translation.v2.model.TranslationResult;
import com.ibm.watson.developer_cloud.service.ServiceCallback;
import com.ibm.watson.developer_cloud.speech_to_text.v1.RecognizeOptions;
import com.ibm.watson.developer_cloud.speech_to_text.v1.SpeechToText;
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.SpeechResults;
import com.ibm.watson.developer_cloud.speech_to_text.v1.websocket.RecognizeDelegate;

public class MainActivity extends AppCompatActivity {
  private RadioGroup targetLanguage;
  private EditText input;
  private AppCompatImageButton mic;
  private Button translate;
  private TextView translatedText;

  private SpeechToText speechService;
  private LanguageTranslation translationService;
  private String selectedTargetLanguage = "es";

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    speechService = initSpeechToTextService();
    translationService = initLanguageTranslationService();

    targetLanguage = (RadioGroup) findViewById(R.id.target_language);
    input = (EditText) findViewById(R.id.input);
    mic = (AppCompatImageButton) findViewById(R.id.mic);
    translate = (Button) findViewById(R.id.translate);
    translatedText = (TextView) findViewById(R.id.translated_text);

    targetLanguage.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
      @Override public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (checkedId) {
          case R.id.spanish:
            selectedTargetLanguage = "es";
            break;
          case R.id.french:
            selectedTargetLanguage = "fr";
            break;
          case R.id.italian:
            selectedTargetLanguage = "it";
            break;
        }
      }
    });

    input.addTextChangedListener(new EmptyTextWatcher());

    mic.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        mic.setEnabled(false);

        new Thread(new Runnable() {
          @Override public void run() {
            try {
              speechService.recognizeUsingWebSockets(new MicrophoneInputStream(),
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
        translationService.translate(input.getText().toString(), "en", selectedTargetLanguage)
            .enqueue(new ServiceCallback<TranslationResult>() {
              @Override public void onResponse(TranslationResult result) {
                showTranslation(result.getFirstTranslation());
              }

              @Override public void onFailure(final Exception e) {
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

  private SpeechToText initSpeechToTextService() {
    SpeechToText service = new SpeechToText();
    String username = getString(R.string.speech_text_username);
    String password = getString(R.string.speech_text_password);
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
    RecognizeOptions options = new RecognizeOptions();
    options.continuous(true);
    options.contentType(MicrophoneInputStream.CONTENT_TYPE);
    options.interimResults(true);
    options.inactivityTimeout(2000);
    return options;
  }

  private class MicrophoneRecognizeDelegate implements RecognizeDelegate {
    @Override public void onMessage(SpeechResults speechResults) {
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

  private class EmptyTextWatcher implements TextWatcher {
    @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

    @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
      if (s.length() == 0) {
        translate.setEnabled(false);
      } else if (!translate.isEnabled()) {
        translate.setEnabled(true);
      }
    }

    @Override public void afterTextChanged(Editable s) {}
  }
}
