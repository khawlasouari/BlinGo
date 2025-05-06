package com.example.myapplication.detectionObjet;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.SystemClock;

import androidx.annotation.RequiresApi;

import com.example.myapplication.detectionObjet.BoundingBox;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.gpu.CompatibilityList;
import org.tensorflow.lite.gpu.GpuDelegate;
import org.tensorflow.lite.support.common.FileUtil;
import org.tensorflow.lite.support.common.ops.CastOp;
import org.tensorflow.lite.support.common.ops.NormalizeOp;
import org.tensorflow.lite.support.image.ImageProcessor;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Detector {

    private final Interpreter interpreter;
    private final List<String> labels = new ArrayList<>();

    private final int tensorWidth;
    private final int tensorHeight;
    private final int numChannel;
    private final int numElements;

    private final ImageProcessor imageProcessor;

    private static final float INPUT_MEAN = 0f;
    private static final float INPUT_STANDARD_DEVIATION = 255f;
    private static final DataType INPUT_IMAGE_TYPE = DataType.FLOAT32;
    private static final DataType OUTPUT_IMAGE_TYPE = DataType.FLOAT32;
    private static final float CONFIDENCE_THRESHOLD = 0.3F;
    private static final float IOU_THRESHOLD = 0.5F;

    private final DetectorListener detectorListener;

    public Detector(Context context, String modelPath, String labelPath, DetectorListener detectorListener) throws IOException {
        this.detectorListener = detectorListener;

        imageProcessor = new ImageProcessor.Builder()
                .add(new NormalizeOp(INPUT_MEAN, INPUT_STANDARD_DEVIATION))
                .add(new CastOp(INPUT_IMAGE_TYPE))
                .build();

        Interpreter.Options options = new Interpreter.Options();
        CompatibilityList compatList = new CompatibilityList();

        if (compatList.isDelegateSupportedOnThisDevice()) {
            GpuDelegate.Options delegateOptions = compatList.getBestOptionsForThisDevice();
            GpuDelegate delegate = new GpuDelegate();


            options.addDelegate(delegate);
        } else {
            options.setNumThreads(4);
        }

        ByteBuffer modelBuffer = FileUtil.loadMappedFile(context, modelPath);
        interpreter = new Interpreter(modelBuffer, options);

        int[] inputShape = interpreter.getInputTensor(0).shape();
        int[] outputShape = interpreter.getOutputTensor(0).shape();

        int width = inputShape[1];
        int height = inputShape[2];
        if (width == 3) {
            width = inputShape[2];
            height = inputShape[3];
        }
        tensorWidth = width;
        tensorHeight = height;

        numChannel = outputShape[1];
        numElements = outputShape[2];

        InputStream inputStream = context.getAssets().open(labelPath);
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        while ((line = reader.readLine()) != null && !line.trim().isEmpty()) {
            labels.add(line);
        }
        reader.close();
        inputStream.close();
    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    public void detect(Bitmap frame) {
        if (tensorWidth == 0 || tensorHeight == 0 || numChannel == 0 || numElements == 0) return;

        long inferenceTime = SystemClock.uptimeMillis();

        Bitmap resizedBitmap = Bitmap.createScaledBitmap(frame, tensorWidth, tensorHeight, false);
        TensorImage tensorImage = new TensorImage(INPUT_IMAGE_TYPE);
        tensorImage.load(resizedBitmap);
        TensorImage processedImage = imageProcessor.process(tensorImage);
        ByteBuffer imageBuffer = processedImage.getBuffer();

        TensorBuffer output = TensorBuffer.createFixedSize(new int[]{1, numChannel, numElements}, OUTPUT_IMAGE_TYPE);
        interpreter.run(imageBuffer, output.getBuffer());

        List<BoundingBox> bestBoxes = bestBox(output.getFloatArray());
        inferenceTime = SystemClock.uptimeMillis() - inferenceTime;

        if (bestBoxes == null || bestBoxes.isEmpty()) {
            detectorListener.onEmptyDetect();
        } else {
            detectorListener.onDetect(bestBoxes, inferenceTime);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private List<BoundingBox> bestBox(float[] array) {
        List<BoundingBox> boundingBoxes = new ArrayList<>();

        for (int c = 0; c < numElements; c++) {
            float maxConf = CONFIDENCE_THRESHOLD;
            int maxIdx = -1;
            int j = 4;
            int arrayIdx = c + numElements * j;
            while (j < numChannel) {
                if (array[arrayIdx] > maxConf) {
                    maxConf = array[arrayIdx];
                    maxIdx = j - 4;
                }
                j++;
                arrayIdx += numElements;
            }

            if (maxConf > CONFIDENCE_THRESHOLD) {
                String clsName = labels.get(maxIdx);
                float cx = array[c];
                float cy = array[c + numElements];
                float w = array[c + numElements * 2];
                float h = array[c + numElements * 3];
                float x1 = cx - (w / 2F);
                float y1 = cy - (h / 2F);
                float x2 = cx + (w / 2F);
                float y2 = cy + (h / 2F);

                if (x1 < 0F || x1 > 1F || y1 < 0F || y1 > 1F || x2 < 0F || x2 > 1F || y2 < 0F || y2 > 1F) continue;

                boundingBoxes.add(new BoundingBox(x1, y1, x2, y2, cx, cy, w, h, maxConf, clsName=clsName, maxIdx));
            }
        }

        return applyNMS(boundingBoxes);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private List<BoundingBox> applyNMS(List<BoundingBox> boxes) {
        List<BoundingBox> sortedBoxes = new ArrayList<>(boxes);
        Collections.sort(sortedBoxes, new Comparator<BoundingBox>() {
            @Override
            public int compare(BoundingBox a, BoundingBox b) {
                return Float.compare(b.getCnf(), a.getCnf());
            }
        });

        List<BoundingBox> selectedBoxes = new ArrayList<>();

        while (!sortedBoxes.isEmpty()) {
            BoundingBox first = sortedBoxes.remove(0);
            selectedBoxes.add(first);

            sortedBoxes.removeIf(nextBox -> calculateIoU(first, nextBox) >= IOU_THRESHOLD);
        }

        return selectedBoxes;
    }
    public void restart(boolean isGpu) {
        // Laisser vide ou ajouter une logique si tu veux recharger le modèle avec GPU/CPU
    }

    public void close() {
        if (interpreter != null) {
            interpreter.close();
        }
    }

    private float calculateIoU(BoundingBox box1, BoundingBox box2) {
        float x1 = Math.max(box1.getX1(), box2.getX1());
        float y1 = Math.max(box1.getY1(), box2.getY1());
        float x2 = Math.min(box1.getX2(), box2.getX2());
        float y2 = Math.min(box1.getY2(), box2.getY2());
        float intersectionArea = Math.max(0F, x2 - x1) * Math.max(0F, y2 - y1);
        float box1Area = box1.getW() * box1.getH();
        float box2Area = box2.getW() * box2.getH();
        return intersectionArea / (box1Area + box2Area - intersectionArea);
    }

    public interface DetectorListener {
        void onEmptyDetect();
        void onDetect(List<BoundingBox> boundingBoxes, long inferenceTime);
    }
}