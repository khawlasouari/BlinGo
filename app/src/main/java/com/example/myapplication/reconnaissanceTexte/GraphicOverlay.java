package com.example.myapplication.reconnaissanceTexte;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import com.google.android.gms.vision.text.TextBlock;

import java.util.ArrayList;
import java.util.List;

public class GraphicOverlay extends View {

    private final Paint rectPaint;
    private List<TextBlock> textBlocks = new ArrayList<>(); // <-- Ancien TextBlock

    public GraphicOverlay(Context context, AttributeSet attrs) {
        super(context, attrs);
        rectPaint = new Paint();
        rectPaint.setColor(Color.RED);
        rectPaint.setStyle(Paint.Style.STROKE);
        rectPaint.setStrokeWidth(4.0f);
    }

    public void updateTextBlocks(List<TextBlock> blocks) {
        this.textBlocks = blocks;
        invalidate(); // Redessine la vue
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        for (TextBlock block : textBlocks) {
            if (block != null && block.getBoundingBox() != null) {
                RectF rect = new RectF(block.getBoundingBox());
                canvas.drawRect(rect, rectPaint);
            }
        }
    }
}
