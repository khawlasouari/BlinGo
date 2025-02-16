package com.example.myapplication;


import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.List;

public class ViewAttendanceActivity extends AppCompatActivity {
    ListView attendanceListView;
    List<String> attendanceList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_attendance);

        attendanceListView = findViewById(R.id.attendanceListView);

        // Exemple de données
        attendanceList = new ArrayList<>();
        attendanceList.add("John Doe - Present");
        attendanceList.add("Jane Smith - Absent");
        attendanceList.add("Robert Brown - Present");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, attendanceList);
        attendanceListView.setAdapter(adapter);
    }
}
