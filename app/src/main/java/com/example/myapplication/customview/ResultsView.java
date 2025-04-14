package com.example.myapplication.customview;


import java.util.List;
import com.example.myapplication.tflite.Classifier.Recognition;

public interface ResultsView {
    public void setResults(final List<Recognition> results);
}
