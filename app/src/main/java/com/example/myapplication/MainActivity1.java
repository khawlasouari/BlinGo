package com.example.myapplication;
import com.example.myapplication.MainActivity3;
import com.example.myapplication.medica.Accueil;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.Toast;



import java.util.ArrayList;
import java.util.Locale;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity1 extends AppCompatActivity {
    private TextToSpeech textToSpeech;
    private static final int REQUEST_CODE_SPEECH_INPUT = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main1);

        // Initialiser Text-to-Speech
        textToSpeech = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                int result = textToSpeech.setLanguage(Locale.FRENCH);
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Toast.makeText(this, "Langue non supportée pour TTS", Toast.LENGTH_SHORT).show();
                } else {
                    speak("Bienvenue.");
                    // Lancer la reconnaissance vocale après le message
                    startVoiceInput();
                }
            }
        });
    }

    // Méthode pour la synthèse vocale
    private void speak(String text) {
        if (textToSpeech != null) {
            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, "cmd_start");
        }
    }

    // Méthode pour lancer la reconnaissance vocale
    private void startVoiceInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Parlez maintenant...");
        try {
            startActivityForResult(intent, REQUEST_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "Reconnaissance vocale non supportée", Toast.LENGTH_SHORT).show();
        }
    }

    // Récupérer le résultat vocal
    // Dans ton activité, par exemple MainActivity.java

    private static final int REQUEST_CODE_VOICE = 100;

    private void startVoiceRecognition() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Parlez maintenant...");
        try {
            startActivityForResult(intent, REQUEST_CODE_VOICE);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(), "Reconnaissance vocale non supportée", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_SPEECH_INPUT && resultCode == RESULT_OK && data != null) {
            ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (result != null && result.size() > 0) {
                String spokenText = result.get(0).toLowerCase();

                if (spokenText.contains("reconnaissance")) {
                    startActivity(new Intent(MainActivity1.this, MainActivity.class));

                } else if (spokenText.contains("detection")) {
                    startActivity(new Intent(MainActivity1.this, DetectorActivity.class));

                } else if (spokenText.contains("texte")) {
                    Intent intent = new Intent(MainActivity1.this, MainActivity3.class);
                    Log.d("DEBUG", "Starting MainActivity3...");
                    startActivity(intent);

                }
                else if (spokenText.contains("médicament") || spokenText.contains("liste de médicaments") || spokenText.contains("interface de médicament")) {
                    speak("Ouverture de l’interface des médicaments");
                    Intent intent = new Intent(MainActivity1.this, Accueil.class);
                    startActivity(intent);
                }

                else {
                    speak("Commande non reconnue. Veuillez réessayer.");
                }
            }
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (textToSpeech != null) {
            textToSpeech.speak("Que voulez-vous faire ? Dites profil, enfants ou employés.", TextToSpeech.QUEUE_FLUSH, null, null);

            // Attendre que la voix ait fini avant de lancer la reconnaissance vocale
            textToSpeech.setOnUtteranceProgressListener(new android.speech.tts.UtteranceProgressListener() {
                @Override
                public void onStart(String utteranceId) { }

                @Override
                public void onDone(String utteranceId) {
                    runOnUiThread(() -> startVoiceInput());
                }

                @Override
                public void onError(String utteranceId) { }
            });

            // Spécifier un ID pour détecter la fin de la voix
            textToSpeech.speak(" ", TextToSpeech.QUEUE_FLUSH, null, "cmd_start");
        }
    }


    @Override
    protected void onDestroy() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        super.onDestroy();
    }
}
