package com.example.rider;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class VolunteerHomeFragment extends Fragment {

    RecyclerView recyclerView;
    StudentAdapter studentFormAdapter;
    ArrayList<StudentForm> studentFormArrayList;
    DatabaseReference database;
    FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View v = inflater.inflate(R.layout.activity_volunteer_home_fragment, container, false);



        recyclerView = v.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        db = FirebaseFirestore.getInstance();
        studentFormArrayList  = new ArrayList<StudentForm>();
        studentFormAdapter = new StudentAdapter(VolunteerHomeFragment.this, studentFormArrayList);

        recyclerView.setAdapter(studentFormAdapter);

        EventChangeListener();
        return v;
    }

    private void EventChangeListener() {
        db.collection("students_form")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {

                        if (error != null ){
                            Log.e("arrival error", error.getMessage());
                            return;
                        }
                        for (DocumentChange dc : value.getDocumentChanges()){
                            if (dc.getType() == DocumentChange.Type.ADDED){
                                studentFormArrayList.add(dc.getDocument().toObject(StudentForm.class));
                            }

                            studentFormAdapter.notifyDataSetChanged();


                        }

                    }
                });
    }
}
