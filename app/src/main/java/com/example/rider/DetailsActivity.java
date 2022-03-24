package com.example.rider;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

public class DetailsActivity extends AppCompatActivity {

    TextView Arrival, Departure, Msg, ArrDate, ArrTime, DepDate, DepTime, Weight, People;

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
        Weight= findViewById(R.id.weightholder);
        People= findViewById(R.id.peopleholder);

        Arrival.setText(getIntent().getStringExtra("Arrival").toString());
       Departure.setText(getIntent().getStringExtra("Departure").toString());
       Msg.setText(getIntent().getStringExtra("Msg").toString());
       ArrTime.setText(getIntent().getStringExtra("ArrDate").toString());
       ArrDate.setText(getIntent().getStringExtra("ArrDate").toString());
       DepTime.setText(getIntent().getStringExtra("DepTime").toString());
       DepDate.setText(getIntent().getStringExtra("DepDate").toString());
       Weight.setText(getIntent().getStringExtra("Weight").toString());
       People.setText(getIntent().getStringExtra("People").toString());
    }
}