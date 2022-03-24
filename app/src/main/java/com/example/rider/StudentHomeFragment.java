package com.example.rider;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StudentHomeFragment extends Fragment {
EditText etDate, etDate2, departureLocation, arrivalLocation, etTime, etTime2, etweight, etmsg;
Button btnDate, btnDate2, btnTime,btnTime2, btnStudentSubmit;
Spinner peopleSpinner;
DatePickerDialog.OnDateSetListener setListener;
FirebaseFirestore fstore;
DatabaseReference studentDbRef;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater,container,savedInstanceState);
        View v = inflater.inflate(R.layout.activity_student_fragment_home,container,false);

        ArrayList<String> people =  new ArrayList<>();
        people.add("Number of People");
        people.add("1");
        people.add("2");
        people.add("3");
        people.add("4");
        people.add("5");
        people.add("6");
        people.add("7");
        people.add("8");
        people.add("9");
        people.add("10");
        ArrayAdapter <String> adapter = new ArrayAdapter<String>(
                getContext(),
                android.R.layout.simple_list_item_1,
                people
        );
        peopleSpinner = (Spinner) v.findViewById(R.id.peopleSpinner_id);
        peopleSpinner.setAdapter(adapter);
        etDate = (EditText) v.findViewById(R.id.et_date);
        etTime = (EditText) v.findViewById(R.id.et_time);
        etmsg = (EditText) v.findViewById(R.id.et_msg);
        etweight = (EditText) v.findViewById(R.id.et_weight);
        etTime2 = (EditText) v.findViewById(R.id.et_time2);
        btnDate = (Button) v.findViewById(R.id.pickDateBtn_id);
        btnTime = (Button) v.findViewById(R.id.pickTimeBtn_id);
        fstore = FirebaseFirestore.getInstance();
        departureLocation = (EditText) v.findViewById(R.id.departureLocation_id);
        arrivalLocation = (EditText) v.findViewById(R.id.arrivalLocation_id);

        etDate2 = (EditText) v.findViewById(R.id.et_date2);
        btnDate2 = (Button) v.findViewById(R.id.pickDateBtn_id2);
        btnTime2 = (Button) v.findViewById(R.id.pickTimeBtn_id2);
        btnStudentSubmit = (Button) v.findViewById(R.id.studentSubmitBtn_id);

        studentDbRef = FirebaseDatabase.getInstance().getReference().child("StudentForm");



        btnDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar calendar = Calendar.getInstance();
                int year =  calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int day = calendar.get(Calendar.DAY_OF_MONTH);
                DatePickerDialog datePickerDialog = new DatePickerDialog(
                        getActivity(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int day) {
                        month = month + 1;
                        String date = day + "/" + month + "/" + year;
                        etDate.setText(date);

                    }
                },year, month, day);
                datePickerDialog.show();

            }
        });
        btnDate2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar calendar = Calendar.getInstance();
                int year =  calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int day = calendar.get(Calendar.DAY_OF_MONTH);
                DatePickerDialog datePickerDialog = new DatePickerDialog(
                        getActivity(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int day) {
                        month = month + 1;
                        String date = day + "/" + month + "/" + year;
                        etDate2.setText(date);

                    }
                },year, month, day);
                datePickerDialog.show();
            }
        });

        btnTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
             TimePickerDialog timePickerDialog = new TimePickerDialog(
                     getContext(),
                     new TimePickerDialog.OnTimeSetListener() {
                         @Override
                         public void onTimeSet(TimePicker timePicker, int hourOfDay, int minute) {
                            int hour = hourOfDay;
                            int min = minute;
                            Calendar calendar = Calendar.getInstance();
                            calendar.set(0,0,0,hour,min);
                            String time = hour + "-"+ min;
                            etTime.setText(time);

                         }
                     },12,0,false

             );
             timePickerDialog.show();
            }
        });

        btnTime2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TimePickerDialog timePickerDialog = new TimePickerDialog(
                        getContext(),
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker timePicker, int hourOfDay, int minute) {
                                int hour = hourOfDay;
                                int min = minute;
                                Calendar calendar = Calendar.getInstance();
                                calendar.set(0,0,0,hour,min);
                                String time = hour + "-"+ min;
                                etTime2.setText(time);

                            }
                        },12,0,false

                );
                timePickerDialog.show();
            }
        });

        btnStudentSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final   String departure = departureLocation.getText().toString();
                final  String arrival = arrivalLocation.getText().toString();
                final  String departureDate = etDate.getText().toString();
                final  String arrivalDate = etDate2.getText().toString();
                final  String departureTime = etTime.getText().toString();
                final  String arrivalTime = etTime2.getText().toString();
                final  String weight = etweight.getText().toString();
                final  String msg = etmsg.getText().toString();
                final  String people = peopleSpinner.getSelectedItem().toString();

                if (TextUtils.isEmpty(departureDate)){
                    etDate.setError("Select Departure Date");
                    return;
                }
                if (TextUtils.isEmpty(departureTime)){
                    etTime.setError("Select Departure Time");
                    return;
                }
                if (TextUtils.isEmpty(departure)){
                    departureLocation.setError("It cannot be left empty");
                    return;
                }
                if (TextUtils.isEmpty(arrival)){
                    arrivalLocation.setError("It cannot be left empty");
                    return;
                }


                if (TextUtils.isEmpty(arrivalDate)){
                    etDate2.setError("Select Arrival Date");
                    return;
                }

                if (TextUtils.isEmpty(arrivalTime)){
                    etTime.setError("Select Arrival Time");
                    return;
                }
                if (TextUtils.isEmpty(msg)){
                    etmsg.setError("Write Special Message");
                    return;
                }

                if (TextUtils.isEmpty(weight)){
                    etweight.setError("Input Luggage Weight");
                    return;
                }
                insertStudentData();

            }
        });

        return v;
    }

    private void insertStudentData() {
        Map  <String, String> items =  new HashMap<>();
        items.put("Departure",departureLocation.getText().toString().trim());
        items.put("Arrival",arrivalLocation.getText().toString().trim());
        items.put("DepTime",etTime.getText().toString().trim());
        items.put("ArrTime",etTime2.getText().toString().trim());
        items.put("DepDate",etDate.getText().toString().trim());
        items.put("ArrDate",etDate2.getText().toString().trim());
        items.put("Msg",etmsg.getText().toString().trim());
        items.put("Weight",etweight.getText().toString().trim());
        items.put("People",peopleSpinner.getSelectedItem().toString().trim());
       fstore.collection("students_form").add(items)
               .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                   @Override
                   public void onComplete(@NonNull Task<DocumentReference> task) {
                    departureLocation.setText("");
                    arrivalLocation.setText("");
                       Toast.makeText(getContext(), "Data Inserted", Toast.LENGTH_SHORT).show();
                   }
               });

    }


}