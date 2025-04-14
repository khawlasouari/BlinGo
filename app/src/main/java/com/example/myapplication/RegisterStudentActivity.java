package com.example.myapplication;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class RegisterStudentActivity extends AppCompatActivity {
    EditText studentName, studentId;
    Button registerButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_student);

        studentName = findViewById(R.id.studentName);
        studentId = findViewById(R.id.studentId);
        registerButton = findViewById(R.id.registerButton);

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = studentName.getText().toString().trim();
                String id = studentId.getText().toString().trim();

                if (name.isEmpty() || id.isEmpty()) {
                    Toast.makeText(RegisterStudentActivity.this, "Please fill all fields!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(RegisterStudentActivity.this, "Student Registered: " + name, Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        });
    }
}
