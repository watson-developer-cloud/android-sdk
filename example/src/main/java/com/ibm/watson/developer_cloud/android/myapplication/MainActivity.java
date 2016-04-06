package com.ibm.watson.developer_cloud.android.myapplication;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import com.ibm.watson.developer_cloud.language_translation.v2.LanguageTranslation;
import com.ibm.watson.developer_cloud.language_translation.v2.model.TranslationResult;

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

    translationService = new LanguageTranslation();
    String username = getString(R.string.language_translation_username);
    String password = getString(R.string.language_translation_password);
    translationService.setUsernameAndPassword(username, password);

    translate.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        new TranslationAsyncTask().execute(input.getText().toString());
      }
    });
  }

  class TranslationAsyncTask extends AsyncTask<String, Void, TranslationResult> {
    Exception e;

    @Override protected TranslationResult doInBackground(String... params) {
      try {
        return translationService.translate(params[0], "en", selectedTargetLanguage);
      } catch (Exception e) {
        this.e = e;
        return null;
      }
    }

    @Override protected void onPostExecute(TranslationResult result) {
      super.onPostExecute(result);
      if (result != null) {
        translatedText.setText(result.getTranslations().get(0).getTranslation());
      } else {
        Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
      }
    }
  }
}
