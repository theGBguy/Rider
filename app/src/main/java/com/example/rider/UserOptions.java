package com.example.rider;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class UserOptions extends AppCompatActivity {
CardView student, volunteer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_options);

        student = findViewById(R.id.cv_student);
        volunteer = findViewById(R.id.cv_volunteer);

        student.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(UserOptions.this, StudentLogin.class);
                startActivity(intent);
            }
        });

        volunteer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(UserOptions.this, VolunteerLogin.class);
                startActivity(intent);
            }
        });
    }
}