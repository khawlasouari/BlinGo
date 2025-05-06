package com.example.myapplication.medica;

import androidx.recyclerview.widget.RecyclerView;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;
import android.widget.Toast;

import com.example.myapplication.MainActivity1;
import com.example.myapplication.R;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

public class Accueil extends AppCompatActivity {
    private RecyclerView recyclerView;
    private MedicamentAdapter adapter;
    private List<Medicament> medicamentList;
    private static final int VOICE_REQUEST_CODE = 100;
    private static final int ADD_MEDICAMENT_REQUEST = 1;
    private TextToSpeech textToSpeech;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.acceuil);

        recyclerView = findViewById(R.id.recyclerView);
        dbHelper = new DatabaseHelper(this);
        medicamentList = dbHelper.getTousLesMedicaments();

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MedicamentAdapter(medicamentList);
        recyclerView.setAdapter(adapter);

        textToSpeech = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech.setLanguage(Locale.FRENCH);
                textToSpeech.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                    @Override
                    public void onStart(String utteranceId) {
                    }

                    @Override
                    public void onDone(String utteranceId) {
                        runOnUiThread(() -> startVoiceRecognition());
                    }

                    @Override
                    public void onError(String utteranceId) {
                    }
                });

                Bundle params = new Bundle();
                params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "ID1");
                textToSpeech.speak("Bonjour, dites une commande comme ajouter, supprimer ou modifier un médicament.",
                        TextToSpeech.QUEUE_FLUSH, params, "ID1");
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) requestNotificationPermission();
        requestVibratePermission();

        createNotificationChannel();
        scheduleMedicationReminder();
    }

    private void startVoiceRecognition() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "fr-FR");
        try {
            startActivityForResult(intent, VOICE_REQUEST_CODE);
        } catch (Exception e) {
            Toast.makeText(this, "Erreur reconnaissance vocale : " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void speak(String text) {
        Bundle params = new Bundle();
        params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "ID2");
        textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, params, "ID2");
    }

    private void handleVoiceCommand(String command) {
        command = command.toLowerCase().trim();
        Log.d("CommandeVocale", "Texte reconnu : " + command);
        Toast.makeText(this, "Texte reconnu : " + command, Toast.LENGTH_LONG).show();

        if (command.matches(".*(ajouter|nouveau|enregistrer).*")) {
            speak("Ajout d’un médicament. Veuillez entrer les détails.");
            Intent intent = new Intent(this, AddMed.class);
            startActivityForResult(intent, ADD_MEDICAMENT_REQUEST);

        } else if (command.contains("supprimer")) {
            String nom = command.replaceAll("supprimer", "")
                    .replaceAll("[0-9]+.*", "")
                    .trim();
            supprimerMedicamentParNom(nom);

        } else if (command.contains("modifier")) {
            String nom = command.replace("modifier", "").trim();
            modifierMedicamentParNom(nom);

        } else if ( command.contains("début")) {

            speak("accueil.");
            Intent intent = new Intent(this, MainActivity1.class);
            startActivity(intent);
            finish();

        } else {
            speak("Commande non reconnue. Réessayez.");
        }
    }



    private void supprimerMedicamentParNom(String nom) {
        boolean trouve = false;
        for (int i = 0; i < medicamentList.size(); i++) {
            if (medicamentList.get(i).getNom().equalsIgnoreCase(nom)) {
                dbHelper.supprimerMedicament(medicamentList.get(i).getId());
                medicamentList.remove(i);
                adapter.notifyDataSetChanged();
                speak("Médicament supprimé : " + nom);
                trouve = true;
                break;
            }
        }

        if (!trouve) {
            speak("Médicament introuvable : " + nom);
        }
    }



    private void modifierMedicamentParNom(String nom) {
        for (Medicament m : medicamentList) {
            if (m.getNom().equalsIgnoreCase(nom)) {
                Intent intent = new Intent(this, AddMed.class);
                intent.putExtra("isEditMode", true);
                intent.putExtra("nom", m.getNom());
                intent.putExtra("dose", m.getDose());
                intent.putExtra("heure", m.getHeurePrise());
                intent.putExtra("id", m.getId()); // pour mise à jour
                startActivityForResult(intent, ADD_MEDICAMENT_REQUEST);
                speak("Modification de " + nom);
                return;
            }
        }
        speak("Médicament non trouvé : " + nom);
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == VOICE_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (result != null && !result.isEmpty()) {
                handleVoiceCommand(result.get(0));
            }
        } else if (requestCode == ADD_MEDICAMENT_REQUEST && resultCode == RESULT_OK && data != null) {
            String nom = data.getStringExtra("nom");
            String dose = data.getStringExtra("dose");
            String heure = data.getStringExtra("heure");

            Medicament medicament = new Medicament(nom, dose, heure);
            dbHelper.ajouterMedicament(medicament);
            medicamentList.add(medicament);
            adapter.notifyDataSetChanged();
            enregistrerMedicamentDansFirestore(medicament);
            speak("Médicament ajouté avec succès : " + nom);
        } else {
            startVoiceRecognition();
        }
    }

    public void enregistrerMedicamentDansFirestore(Medicament medicament) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String, Object> data = new HashMap<>();
        data.put("nom", medicament.getNom());
        data.put("dose", medicament.getDose());
        data.put("heure", medicament.getHeurePrise());

        db.collection("medicaments")
                .document(medicament.getNom().toLowerCase())
                .set(data)
                .addOnSuccessListener(aVoid -> Toast.makeText(this, "Ajouté à Firebase", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Erreur Firebase : " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "medicationReminder", "Medication Reminder", NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Canal pour les rappels de médicaments");
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void scheduleMedicationReminder() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, ReminderReceiver.class);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 14);
        calendar.set(Calendar.MINUTE, 10);
        calendar.set(Calendar.SECOND, 0);

        if (alarmManager != null) {
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                    AlarmManager.INTERVAL_DAY, pendingIntent);
        }
    }

    private void requestNotificationPermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 1);
        }
    }

    private void requestVibratePermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.VIBRATE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.VIBRATE}, 2);
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
