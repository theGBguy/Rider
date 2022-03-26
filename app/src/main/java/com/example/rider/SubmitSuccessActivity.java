package com.example.rider;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class SubmitSuccessActivity extends AppCompatActivity {
Button submitHomeBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_submit_success);

        submitHomeBtn =  findViewById(R.id.submitHomeBtn_id);
        submitHomeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SubmitSuccessActivity.this, StudentSideNavBar.class);
                startActivity(intent);
            }
        });
    }
}

