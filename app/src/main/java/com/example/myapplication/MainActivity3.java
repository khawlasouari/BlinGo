package com.example.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.MediaStore;
import android.speech.tts.TextToSpeech;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.io.IOException;
import java.util.Locale;

public class MainActivity3 extends AppCompatActivity {

    private Button selectImageButton, captureImageButton;
    private ImageView imageView;
    private TextView textView;
    private Bitmap bitmap;
    private TextToSpeech textToSpeech;

    // Launcher pour sélectionner une image depuis la galerie
    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    try {
                        bitmap = BitmapFactory.decodeStream(this.getContentResolver()
                                .openInputStream(result.getData().getData()));
                        imageView.setImageBitmap(bitmap);
                        recognizeTextFromImage(bitmap);
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Échec du chargement de l'image", Toast.LENGTH_SHORT).show();
                    }
                }
            });

    // Launcher pour capturer une image avec la caméra
    private final ActivityResultLauncher<Intent> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Bundle extras = result.getData().getExtras();
                    if (extras != null) {
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                            bitmap = extras.getParcelable("data", Bitmap.class); // API 33+
                        } else {
                            bitmap = extras.getParcelable("data"); // Compatibilité API < 33
                        }

                        if (bitmap != null) {
                            imageView.setImageBitmap(bitmap);
                            recognizeTextFromImage(bitmap);
                        } else {
                            Toast.makeText(this, "Erreur : Image non disponible", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3); // Assure-toi que ce layout existe

        // Initialisation des vues
        selectImageButton = findViewById(R.id.selectImageButton);
        captureImageButton = findViewById(R.id.captureImageButton);
        imageView = findViewById(R.id.imageView);
        textView = findViewById(R.id.textView);

        // Initialiser la synthèse vocale
        textToSpeech = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech.setLanguage(Locale.FRENCH);
                speak("Bienvenue dans l'application ! Appuyez sur Sélectionner une image ou Prendre une photo.");
            } else {
                Toast.makeText(this, "Échec de l'initialisation de la synthèse vocale", Toast.LENGTH_SHORT).show();
            }
        });

        // Bouton pour sélectionner une image
        selectImageButton.setOnClickListener(v -> {
            speak("Sélectionnez une image depuis votre galerie.");
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            imagePickerLauncher.launch(intent);
        });

        // Bouton pour prendre une photo
        captureImageButton.setOnClickListener(v -> {
            speak("Prenez une photo avec votre caméra.");
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            cameraLauncher.launch(intent);
        });
    }

    // Reconnaissance de texte à partir de l'image
    private void recognizeTextFromImage(Bitmap bitmap) {
        try {
            InputImage inputImage = InputImage.fromBitmap(bitmap, 0);
            TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

            recognizer.process(inputImage)
                    .addOnSuccessListener(result -> {
                        String recognizedText = result.getText();
                        textView.setText(recognizedText);
                        speak("Voici le texte reconnu : " + recognizedText);
                        Toast.makeText(this, recognizedText, Toast.LENGTH_LONG).show();
                    })
                    .addOnFailureListener(e -> {
                        e.printStackTrace();
                        Toast.makeText(this, "Échec de la reconnaissance du texte", Toast.LENGTH_SHORT).show();
                    });

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Erreur lors de la reconnaissance du texte", Toast.LENGTH_SHORT).show();
        }
    }

    // Synthèse vocale
    private void speak(String text) {
        if (textToSpeech != null) {
            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
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
