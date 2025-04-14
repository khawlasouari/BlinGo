package com.example.myapplication.medica;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.speech.tts.TextToSpeech;
import com.example.myapplication.R; // ✅ Remplace par le bon nom de package


import androidx.core.app.NotificationCompat;

import java.util.Locale;

public class ReminderReceiver extends BroadcastReceiver {


    @Override
    public void onReceive(Context context, Intent intent) {
        String nomMedicament = intent.getStringExtra("nomMedicament");
        if (nomMedicament == null) nomMedicament = "votre médicament";

        // Intent pour l'activité à ouvrir quand on clique sur la notification
        Intent notificationIntent = new Intent(context, Accueil.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Créer la notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "medicationReminder")
                .setSmallIcon(R.drawable.ic_medicine)
                .setContentTitle("Rappel de médicament")
                .setContentText("Il est temps de prendre " + nomMedicament)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        // Gérer le canal (Android 8+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "medicationReminder",
                    "Rappel Médicament",
                    NotificationManager.IMPORTANCE_HIGH
            );
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }

        // Afficher la notification
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify(1, builder.build());
        }

        // 🟢 Lecture vocale avec TextToSpeech — ICI exactement
        final String finalNomMedicament = nomMedicament;

        final TextToSpeech[] tts = new TextToSpeech[1];
        tts[0] = new TextToSpeech(context.getApplicationContext(), status -> {
            if (status == TextToSpeech.SUCCESS) {
                tts[0].setLanguage(Locale.FRENCH);
                String message = "Il est temps de prendre " + finalNomMedicament;
                tts[0].speak(message, TextToSpeech.QUEUE_FLUSH, null, "medReminderId");

                tts[0].setOnUtteranceProgressListener(new android.speech.tts.UtteranceProgressListener() {
                    @Override
                    public void onStart(String utteranceId) {
                    }

                    @Override
                    public void onDone(String utteranceId) {
                        tts[0].shutdown(); // Libérer les ressources
                    }

                    @Override
                    public void onError(String utteranceId) {
                    }
                });
            }
        });
    }
}