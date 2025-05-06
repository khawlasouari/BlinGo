package com.example.myapplication.reconnaissanceTexte;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.myapplication.R;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity5 extends AppCompatActivity {

    SurfaceView mCameraView;
    TextView mTextView;
    CameraSource mCameraSource;
    TextToSpeech textToSpeech;
    GraphicOverlay graphicOverlay;
    String lastTextSpoken = "";
    long lastSpokenTime = 0;

    private static final String TAG = "MainActivity";
    private static final int requestPermissionID = 101;

    // Définir la langue détectée
    String detectedLanguage = "fr"; // Par défaut, l'anglais

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main5);

        mCameraView = findViewById(R.id.surfaceView);
        mTextView = findViewById(R.id.text_view);
        graphicOverlay = findViewById(R.id.graphicOverlay);

        // Initialiser TextToSpeech
        textToSpeech = new TextToSpeech(getApplicationContext(), status -> {
            // Détecter la langue en fonction du texte détecté
            if (detectedLanguage.equals("fr")) {
                textToSpeech.setLanguage(Locale.FRENCH);
            } else if (detectedLanguage.equals("en")) {
                textToSpeech.setLanguage(Locale.ENGLISH);
            }
        });

        startCameraSource();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode != requestPermissionID) {
            Log.d(TAG, "Unexpected permission result: " + requestCode);
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            return;
        }

        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            try {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                mCameraSource.start(mCameraView.getHolder());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void startCameraSource() {
        final TextRecognizer textRecognizer = new TextRecognizer.Builder(getApplicationContext()).build();

        if (!textRecognizer.isOperational()) {
            Log.w(TAG, "Detector dependencies not loaded yet");
            return;
        }

        mCameraSource = new CameraSource.Builder(getApplicationContext(), textRecognizer)
                .setFacing(CameraSource.CAMERA_FACING_BACK)
                .setRequestedPreviewSize(1280, 1024)
                .setAutoFocusEnabled(true)
                .setRequestedFps(2.0f)
                .build();


        mCameraView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                try {
                    if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(MainActivity5.this,
                                new String[]{Manifest.permission.CAMERA},
                                requestPermissionID);
                        return;
                    }
                    mCameraSource.start(mCameraView.getHolder());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {}

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                mCameraSource.stop();
            }
        });

        textRecognizer.setProcessor(new Detector.Processor<TextBlock>() {
            @Override
            public void release() {}

            @Override
            public void receiveDetections(Detector.Detections<TextBlock> detections) {
                final SparseArray<TextBlock> items = detections.getDetectedItems();
                if (items.size() != 0) {
                    mTextView.post(() -> {
                        StringBuilder stringBuilder = new StringBuilder();
                        List<TextBlock> blocks = new ArrayList<>();

                        for (int i = 0; i < items.size(); ++i) {
                            TextBlock item = items.valueAt(i);
                            stringBuilder.append(item.getValue()).append("\n");
                            blocks.add(item);
                        }

                        String detectedText = stringBuilder.toString().trim();
                        mTextView.setText(detectedText);
                        graphicOverlay.updateTextBlocks(blocks); // ➕ affiche les cadres

                        long currentTime = System.currentTimeMillis();

                        if (detectedText.length() > 8 &&
                                !detectedText.equalsIgnoreCase(lastTextSpoken) &&
                                currentTime - lastSpokenTime > 5000) {

                            lastTextSpoken = detectedText;
                            lastSpokenTime = currentTime;

                            // Détecter la langue avant de lire le texte
                            detectedLanguage = detectLanguage(detectedText);

                            new Handler(Looper.getMainLooper()).postDelayed(() ->
                                    textToSpeech.speak(detectedText, TextToSpeech.QUEUE_FLUSH, null, null), 1000);
                        }
                    });
                }
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mCameraSource != null) {
            mCameraSource.stop();
        }
        if (textToSpeech != null && textToSpeech.isSpeaking()) {
            textToSpeech.stop();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mCameraSource != null) {
            mCameraSource.release();
        }
        if (textToSpeech != null && textToSpeech.isSpeaking()) {
            textToSpeech.stop();
        }
    }

    // Méthode simple pour détecter la langue du texte (à remplacer par une approche plus sophistiquée si nécessaire)
    private String detectLanguage(String text) {
        // Vérifier si le texte contient des mots typiques du français ou de l'anglais
        if (text.toLowerCase().contains("bonjour") || text.toLowerCase().contains("français")) {
            return "fr"; // Langue française détectée
        } else {
            return "en"; // Par défaut, langue anglaise
        }
    }
}
