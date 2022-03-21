package com.example.rider;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class StudentLogin extends AppCompatActivity {
        TextView no_account;
        Button loginBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.student_login);

        no_account = findViewById(R.id.login_registerTextView_id);
        loginBtn = findViewById(R.id.login_loginBtn_id);

        no_account.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(StudentLogin.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(StudentLogin.this, StudentSideNavBar.class);
                startActivity(intent);
            }
        });

    }
}