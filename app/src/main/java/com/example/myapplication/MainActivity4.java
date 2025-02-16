package com.example.myapplication;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity4 extends AppCompatActivity {
    Button registerStudent, takeAttendance, viewAttendance, exitApp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main4);

        registerStudent = findViewById(R.id.registerStudent);
        takeAttendance = findViewById(R.id.takeAttendance);
        viewAttendance = findViewById(R.id.viewAttendance);
        exitApp = findViewById(R.id.exitApp);

        registerStudent.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity4.this, RegisterStudentActivity.class);
            startActivity(intent);
        });

        takeAttendance.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity4.this, TakeAttendanceActivity.class);
            startActivity(intent);
        });

        viewAttendance.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity4.this, ViewAttendanceActivity.class);
            startActivity(intent);
        });

        exitApp.setOnClickListener(v -> finish());
    }
}
