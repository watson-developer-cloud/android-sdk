package com.ibm.watson.developer_cloud.android.myapplication;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import com.ibm.watson.developer_cloud.language_translation.v2.LanguageTranslation;
import com.ibm.watson.developer_cloud.language_translation.v2.model.TranslationResult;
import com.ibm.watson.developer_cloud.service.ServiceCallback;

public class MainActivity extends AppCompatActivity {
  private RadioGroup targetLanguage;
  private EditText input;
  private Button translate;
  private TextView translatedText;

  private LanguageTranslation translationService;
  private String selectedTargetLanguage = "es";

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    targetLanguage = (RadioGroup) findViewById(R.id.target_language);
    input = (EditText) findViewById(R.id.input);
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

    translationService = new LanguageTranslation();
    String username = getString(R.string.language_translation_username);
    String password = getString(R.string.language_translation_password);
    translationService.setUsernameAndPassword(username, password);

    translate.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        translationService.translate(input.getText().toString(), "en", selectedTargetLanguage)
            .enqueue(new ServiceCallback<TranslationResult>() {
              @Override public void onResponse(TranslationResult result) {
                showTranslation(result.getFirstTranslation());
              }

              @Override public void onFailure(final Exception e) {
                e.printStackTrace();
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
      }
    });
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
