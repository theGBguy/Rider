package com.example.rider;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

public class DetailsActivity extends AppCompatActivity {

    TextView Arrival, Departure, Msg, ArrDate, ArrTime, DepDate, DepTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        Arrival= findViewById(R.id.arrivalholder);
        Departure= findViewById(R.id.departureholder);
        ArrDate= findViewById(R.id.arrdateholder);
        ArrTime= findViewById(R.id.arrivaltimeholder);
        DepDate= findViewById(R.id.depdateholder);
        DepTime= findViewById(R.id.deptimeholder);
        Msg= findViewById(R.id.msgholder);

        Arrival.setText(getIntent().getStringExtra("Arrival").toString());
        ArrDate.setText(getIntent().getStringExtra("ArrDate").toString());
        Departure.setText(getIntent().getStringExtra("Departure").toString());
        ArrTime.setText(getIntent().getStringExtra("ArrTime").toString());
        DepTime.setText(getIntent().getStringExtra("DepTime").toString());
        DepDate.setText(getIntent().getStringExtra("DepDate").toString());
        Msg.setText(getIntent().getStringExtra("Msg").toString());
    }
}