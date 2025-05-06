package com.example.myapplication.detectionObjet;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.AspectRatio;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.myapplication.R;
import com.example.myapplication.databinding.ActivityMain6Binding;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RequiresApi(api = Build.VERSION_CODES.N)
public class MainActivity6 extends AppCompatActivity implements Detector.DetectorListener {

    private ActivityMain6Binding binding;
    private boolean isFrontCamera = false;

    private Preview preview;
    private ImageAnalysis imageAnalyzer;
    private Camera camera;
    private ProcessCameraProvider cameraProvider;
    private Detector detector;

    private ExecutorService cameraExecutor;
    private TextToSpeech textToSpeech;

    private static final String TAG = "Camera";
    private static final int REQUEST_CODE_PERMISSIONS = 10;
    private static final String[] REQUIRED_PERMISSIONS = {
            Manifest.permission.CAMERA
    };

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMain6Binding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        cameraExecutor = Executors.newSingleThreadExecutor();

        cameraExecutor.execute(() -> {
            try {
                detector = new Detector(getBaseContext(), Constants.MODEL_PATH, Constants.LABELS_PATH, this);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        // Initialisation de TextToSpeech
        textToSpeech = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech.setLanguage(Locale.FRANCE); // Utiliser Locale.US pour l'anglais
            } else {
                Log.e(TAG, "Échec de l'initialisation de TextToSpeech");
            }
        });

        if (allPermissionsGranted()) {
            startCamera();
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }

        bindListeners();
    }

    private void bindListeners() {
        binding.isGpu.setOnCheckedChangeListener((buttonView, isChecked) -> {
            cameraExecutor.submit(() -> detector.restart(isChecked));
            if (!isChecked) {
                buttonView.setBackgroundColor(ContextCompat.getColor(getBaseContext(), R.color.gray));
            } else {
                buttonView.setBackgroundColor(ContextCompat.getColor(getBaseContext(), R.color.orange));
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void startCamera() {
        ProcessCameraProvider.getInstance(this).addListener(() -> {
            try {
                cameraProvider = ProcessCameraProvider.getInstance(MainActivity6.this).get();
                bindCameraUseCases();
            } catch (ExecutionException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        }, ContextCompat.getMainExecutor(this));
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void bindCameraUseCases() {
        if (cameraProvider == null) throw new IllegalStateException("Échec de l'initialisation de la caméra.");

        int rotation = binding.viewFinder.getDisplay().getRotation();

        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

        preview = new Preview.Builder()
                .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                .setTargetRotation(rotation)
                .build();

        imageAnalyzer = new ImageAnalysis.Builder()
                .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setTargetRotation(binding.viewFinder.getDisplay().getRotation())
                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                .build();

        imageAnalyzer.setAnalyzer(cameraExecutor, imageProxy -> {
            Bitmap bitmapBuffer = Bitmap.createBitmap(imageProxy.getWidth(), imageProxy.getHeight(), Bitmap.Config.ARGB_8888);
            bitmapBuffer.copyPixelsFromBuffer(imageProxy.getPlanes()[0].getBuffer());

            Matrix matrix = new Matrix();
            matrix.postRotate(imageProxy.getImageInfo().getRotationDegrees());

            if (isFrontCamera) {
                matrix.postScale(-1f, 1f, imageProxy.getWidth(), imageProxy.getHeight());
            }

            Bitmap rotatedBitmap = Bitmap.createBitmap(bitmapBuffer, 0, 0, bitmapBuffer.getWidth(), bitmapBuffer.getHeight(), matrix, true);

            detector.detect(rotatedBitmap);

            imageProxy.close();
        });

        cameraProvider.unbindAll();

        try {
            camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalyzer);
            preview.setSurfaceProvider(binding.viewFinder.getSurfaceProvider());
        } catch (Exception exc) {
            Log.e(TAG, "Échec de la liaison des cas d'utilisation", exc);
        }
    }

    private boolean allPermissionsGranted() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(getBaseContext(), permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private final ActivityResultLauncher<String[]> requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                if (result.get(Manifest.permission.CAMERA)) {
                    startCamera();
                }
            });

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (detector != null) {
            detector.close();
        }
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        cameraExecutor.shutdown();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (allPermissionsGranted()) {
            startCamera();
        } else {
            requestPermissionLauncher.launch(REQUIRED_PERMISSIONS);
        }
    }

    @Override
    public void onEmptyDetect() {
        runOnUiThread(() -> binding.overlay.clear());
    }

    @Override
    public void onDetect(List<BoundingBox> boundingBoxes, long inferenceTime) {
        runOnUiThread(() -> {
            binding.inferenceTime.setText(inferenceTime + "ms");
            binding.overlay.setResults(boundingBoxes);
            binding.overlay.invalidate();

            // Annonce vocale des objets détectés un par un
            if (!boundingBoxes.isEmpty()) {
                StringBuilder detectedObjects = new StringBuilder();
                for (BoundingBox box : boundingBoxes) {
                    detectedObjects.append(box.getClsName()).append(", ");
                }

                // Appel à la fonction speakOut une seule fois, avec les objets détectés
                speakOutSequentially(detectedObjects.toString());
            }
        });
    }

    private void speakOutSequentially(String detectedObjects) {
        if (textToSpeech != null && !detectedObjects.isEmpty()) {
            String[] objects = detectedObjects.split(", ");

            for (String object : objects) {
                textToSpeech.speak(object, TextToSpeech.QUEUE_ADD, null, null);
            }
        }
    }


    private void speakOut(String text) {
        if (textToSpeech != null && !text.isEmpty()) {
            textToSpeech.speak(text, TextToSpeech.QUEUE_ADD, null, null);
        }
    }

}
