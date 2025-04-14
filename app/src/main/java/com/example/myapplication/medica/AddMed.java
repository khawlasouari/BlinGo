package com.example.myapplication.medica;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;
import com.example.myapplication.R; // ✅ Remplace par le bon nom de package


import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Locale;

public class AddMed extends Activity {
    private EditText medicineNameEditText, doseEditText;
    private TimePicker timePicker;
    private CheckBox everyDayCheckBox;
    private Button voiceMedicineButton, voiceDoseButton, voiceTimeButton, saveButton;
    private TextToSpeech tts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.addmed);

        medicineNameEditText = findViewById(R.id.et_medicine_name);
        doseEditText = findViewById(R.id.et_dose);
        timePicker = findViewById(R.id.time_picker);
        everyDayCheckBox = findViewById(R.id.cb_every_day);

        voiceMedicineButton = findViewById(R.id.btn_voice_medicine);
        voiceDoseButton = findViewById(R.id.btn_voice_dose);
        voiceTimeButton = findViewById(R.id.btn_voice_time);
        saveButton = findViewById(R.id.btn_save);

        tts = new TextToSpeech(this, status -> {
            if (status != TextToSpeech.ERROR) {
                tts.setLanguage(Locale.FRENCH);
            }
        });

        voiceMedicineButton.setOnClickListener(v -> startVoiceRecognition(101));
        voiceDoseButton.setOnClickListener(v -> startVoiceRecognition(102));
        voiceTimeButton.setOnClickListener(v -> startVoiceRecognition(103));
        saveButton.setOnClickListener(v -> saveMedicine());

        Log.d("AddMed", "onCreate() appelé");
    }

    private void startVoiceRecognition(int requestCode) {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.FRENCH);

        switch (requestCode) {
            case 101:
                intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Dites le nom du médicament.");
                break;
            case 102:
                intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Dites la dose du médicament, par exemple : un virgule cinq.");
                break;
            case 103:
                intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Dites l'heure de prise, par exemple : quinze heures trente.");
                break;
        }

        startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (result != null && !result.isEmpty()) {
                String spokenText = result.get(0);
                processVoiceInput(requestCode, spokenText);
            }
        }
    }

    private void processVoiceInput(int requestCode, String spokenText) {
        switch (requestCode) {
            case 101:
                medicineNameEditText.setText(spokenText);
                speak("Nom du médicament enregistré : " + spokenText);
                break;
            case 102:
                convertAndSetDose(spokenText);
                break;
            case 103:
                convertAndSetTime(spokenText);
                break;
        }
    }

    private void convertAndSetDose(String spokenDose) {
        try {
            spokenDose = spokenDose.toLowerCase()
                    .replace("virgule", ".")
                    .replace(",", ".")
                    .replace("milligrammes", "mg")
                    .replace("milligramme", "mg")
                    .replace("grammes", "g")
                    .replace("gramme", "g")
                    .replace("unités", "unité")
                    .replace("comprimés", "comprimé");

            String doseValue = "";
            String unit = "";

            String[] words = spokenDose.split(" ");
            for (String word : words) {
                if (word.matches("[0-9.]+")) {
                    doseValue = word;
                } else if (word.matches("[a-zA-Z]+")) {
                    unit = word;
                }
            }

            if (doseValue.isEmpty()) {
                speak("Je n'ai pas compris la valeur de la dose.");
                return;
            }

            String formattedDose = doseValue + (unit.isEmpty() ? "" : " " + unit);
            doseEditText.setText(formattedDose);
            speak("Dose enregistrée : " + formattedDose);
        } catch (Exception e) {
            speak("Je n'ai pas compris la dose, essayez encore.");
        }
    }

    private void convertAndSetTime(String spokenTime) {
        try {
            spokenTime = spokenTime.toLowerCase()
                    .replace(" heures ", ":")
                    .replace(" heure ", ":")
                    .replace(" heures", ":")
                    .replace(" heure", ":")
                    .replace(" et demie", ":30")
                    .replace(" et quart", ":15")
                    .replace(" moins le quart", ":45");

            Log.d("VoiceTime", "Texte après remplacement : " + spokenTime);

            String[] timeParts = spokenTime.split(":");

            if (timeParts.length == 2) {
                int hour = parseFrenchNumber(timeParts[0].trim());
                int minute = parseFrenchNumber(timeParts[1].trim());

                if (hour >= 0 && hour <= 23 && minute >= 0 && minute <= 59) {
                    timePicker.setHour(hour);
                    timePicker.setMinute(minute);
                    speak("Heure enregistrée : " + hour + " heures " + minute + " minutes.");
                } else {
                    speak("Heure invalide.");
                }
            } else {
                speak("Je n'ai pas compris l'heure, essayez encore.");
            }
        } catch (Exception e) {
            speak("Je n'ai pas compris l'heure, essayez encore.");
            Log.e("VoiceTime", "Erreur de parsing : ", e);
        }
    }

    private int parseFrenchNumber(String word) {
        switch (word) {
            case "zéro": return 0;
            case "un": case "une": return 1;
            case "deux": return 2;
            case "trois": return 3;
            case "quatre": return 4;
            case "cinq": return 5;
            case "six": return 6;
            case "sept": return 7;
            case "huit": return 8;
            case "neuf": return 9;
            case "dix": return 10;
            case "onze": return 11;
            case "douze": return 12;
            case "treize": return 13;
            case "quatorze": return 14;
            case "quinze": return 15;
            case "seize": return 16;
            case "dix-sept": return 17;
            case "dix-huit": return 18;
            case "dix-neuf": return 19;
            case "vingt": return 20;
            case "vingt et un": return 21;
            case "vingt-deux": return 22;
            case "vingt-trois": return 23;
            case "trente": return 30;
            case "quarante-cinq": return 45;
            default:
                try {
                    return Integer.parseInt(word);
                } catch (NumberFormatException e) {
                    return -1;
                }
        }
    }

    private void saveMedicine() {
        String medicineName = medicineNameEditText.getText().toString();
        String dose = doseEditText.getText().toString();
        int hour = timePicker.getHour();
        int minute = timePicker.getMinute();
        boolean everyDay = everyDayCheckBox.isChecked();

        if (medicineName.isEmpty() || dose.isEmpty()) {
            speak("Veuillez remplir toutes les informations.");
            return;
        }

        String heure = String.format(Locale.getDefault(), "%02d:%02d", hour, minute);

        Intent resultIntent = new Intent();
        resultIntent.putExtra("nom", medicineName);
        resultIntent.putExtra("dose", dose);
        resultIntent.putExtra("heure", heure);
        setResult(RESULT_OK, resultIntent);
        finish();
    }

    private void speak(String text) {
        if (tts != null && !tts.isSpeaking()) {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
        }
    }

    @Override
    protected void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
        Log.d("AddMed", "onDestroy() appelé");
    }
}
