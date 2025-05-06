package com.example.myapplication.medica;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.example.myapplication.R;

import java.util.ArrayList;
import java.util.Locale;

public class AddMed extends Activity {
    private EditText medicineNameEditText, doseEditText;
    private TimePicker timePicker;
    private CheckBox everyDayCheckBox;
    private Button voiceMedicineButton, voiceDoseButton, voiceTimeButton, saveButton;
    private TextToSpeech tts;

    private DatabaseHelper dbHelper;
    private boolean isEditMode = false;
    private int medicamentId;


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.addmed);

        // Initialisation des vues
        medicineNameEditText = findViewById(R.id.et_medicine_name);
        doseEditText = findViewById(R.id.et_dose);
        timePicker = findViewById(R.id.time_picker);
        everyDayCheckBox = findViewById(R.id.cb_every_day);

        voiceMedicineButton = findViewById(R.id.btn_voice_medicine);
        voiceDoseButton = findViewById(R.id.btn_voice_dose);
        voiceTimeButton = findViewById(R.id.btn_voice_time);
        saveButton = findViewById(R.id.btn_save);

        dbHelper = new DatabaseHelper(this);
        if (getIntent().hasExtra("nom")) {
            medicineNameEditText.setText(getIntent().getStringExtra("nom"));
            doseEditText.setText(getIntent().getStringExtra("dose"));

            // tu peux aussi bloquer le champ nom pour éviter de changer l'identité du médicament
        }

        // Text-to-Speech
        tts = new TextToSpeech(this, status -> {
            if (status != TextToSpeech.ERROR) {
                tts.setLanguage(Locale.FRENCH);
            }
        });

        // Mode modification ?
        Intent intent = getIntent();
        isEditMode = intent.getBooleanExtra("isEditMode", false);

        if (isEditMode) {
            medicamentId = intent.getIntExtra("id", -1);
            medicineNameEditText.setText(intent.getStringExtra("nom"));
            doseEditText.setText(intent.getStringExtra("dose"));
            String heure = intent.getStringExtra("heure");
            if (heure != null && heure.contains(":")) {
                String[] parts = heure.split(":");
                timePicker.setHour(Integer.parseInt(parts[0]));
                timePicker.setMinute(Integer.parseInt(parts[1]));
            }
            saveButton.setText("Modifier");
        }

        // Écouteurs
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

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (result != null && !result.isEmpty()) {
                processVoiceInput(requestCode, result.get(0));
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
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

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void convertAndSetTime(String spokenTime) {
        try {
            Log.d("VOICE_TIME", "Texte reçu pour l'heure : " + spokenTime);

            spokenTime = spokenTime.toLowerCase()
                    .replace(" heures et demie", ":30")
                    .replace(" heure et demie", ":30")
                    .replace(" heures et quart", ":15")
                    .replace(" heure et quart", ":15")
                    .replace(" heures moins le quart", ":45")
                    .replace(" heure moins le quart", ":45")
                    .replace(" heures", ":")
                    .replace(" heure", ":")
                    .replace("h", ":")
                    .replace(" ", "");

            Log.d("VOICE_TIME", "Après remplacement : " + spokenTime);

            String[] timeParts = spokenTime.split(":");

            if (timeParts.length >= 2) {
                int hour = parseFrenchNumber(timeParts[0].trim());
                int minute = parseFrenchNumber(timeParts[1].trim());

                Log.d("VOICE_TIME", "Heure : " + hour + " Minute : " + minute);

                if (hour >= 0 && hour <= 23 && minute >= 0 && minute <= 59) {
                    timePicker.setHour(hour);
                    timePicker.setMinute(minute);
                    speak("Heure enregistrée : " + hour + " heures " + minute + " minutes.");
                } else {
                    speak("L'heure détectée est invalide.");
                }
            } else {
                speak("Je n'ai pas bien compris l'heure, essayez encore.");
            }

        } catch (Exception e) {
            Log.e("VOICE_TIME", "Erreur lors du traitement de l'heure : " + e.getMessage());
            speak("Je n'ai pas compris l'heure, essayez encore.");
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

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void saveMedicine() {
        String medicineName = medicineNameEditText.getText().toString().trim();
        String dose = doseEditText.getText().toString().trim();
        int hour = timePicker.getHour();
        int minute = timePicker.getMinute();
        String heure = String.format(Locale.getDefault(), "%02d:%02d", hour, minute);

        if (medicineName.isEmpty() || dose.isEmpty()) {
            speak("Veuillez remplir toutes les informations.");
            return;
        }

        if (isEditMode) {
            Medicament m = new Medicament(medicamentId, medicineName, dose, heure);
            dbHelper.modifierMedicament(m);
            Toast.makeText(this, "Médicament modifié", Toast.LENGTH_SHORT).show();
        } else {
            dbHelper.ajouterMedicament(new Medicament(medicineName, dose, heure));
            Toast.makeText(this, "Médicament ajouté", Toast.LENGTH_SHORT).show();
        }

        // Préparer le retour à Accueil
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