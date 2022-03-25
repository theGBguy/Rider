package com.example.rider;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class DetailsActivity extends AppCompatActivity {

    TextView Arrival, Departure, Msg, ArrDate, ArrTime, DepDate, DepTime, Weight, People, Name;
    Button acceptBtn, backBtn;

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
        Name = findViewById(R.id.nameholder);
        acceptBtn = findViewById(R.id.acceptBtn_id);
        backBtn = findViewById(R.id.backBtn_id);
        Arrival.setText(getIntent().getStringExtra("Arrival").toString());
        Departure.setText(getIntent().getStringExtra("Departure").toString());
        Msg.setText(getIntent().getStringExtra("Msg").toString());
        ArrTime.setText(getIntent().getStringExtra("ArrTime").toString());
        ArrDate.setText(getIntent().getStringExtra("ArrDate").toString());
        DepTime.setText(getIntent().getStringExtra("DepTime").toString());
        DepDate.setText(getIntent().getStringExtra("DepDate").toString());
        Weight.setText(getIntent().getStringExtra("Weight").toString());
        People.setText(getIntent().getStringExtra("People").toString());
        Name.setText(getIntent().getStringExtra("Name").toString());

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        acceptBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(DetailsActivity.this);
                builder.setTitle(R.string.app_name);
                builder.setIcon(R.mipmap.ic_launcher);
                builder.setMessage("Do you want to Accept?")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Intent intent =  new Intent(DetailsActivity.this, AcceptedRequest.class);
                                startActivity(intent);
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();
            }
        });
    }
}
