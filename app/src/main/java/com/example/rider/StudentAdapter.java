package com.example.rider;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class StudentAdapter extends RecyclerView.Adapter<StudentAdapter.StudentViewHolder> {

    Context context;
    ArrayList<StudentForm> studentFormArrayList;

    public StudentAdapter(VolunteerHomeFragment context, ArrayList<StudentForm> studentFormArrayList) {
        this.context = context.getContext();
        this.studentFormArrayList = studentFormArrayList;
    }

    @NonNull
    @Override
    public StudentAdapter.StudentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.studentfrom_item, parent, false);
        return new StudentViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull StudentAdapter.StudentViewHolder holder, int position) {
        holder.Departure.setText(studentFormArrayList.get(position).getDeparture());
        holder.ArrDate.setText(studentFormArrayList.get(position).getDeparture());
        holder.Arrival.setText(studentFormArrayList.get(position).getArrival());
        holder.Msg.setText(studentFormArrayList.get(position).getArrival());
        holder.ArrTime.setText(studentFormArrayList.get(position).getDeparture());
        holder.DepTime.setText(studentFormArrayList.get(position).getArrival());
        holder.DepDate.setText(studentFormArrayList.get(position).getArrival());

        holder.Departure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(holder.Arrival.getContext(), DetailsActivity.class);
                intent.putExtra("Departure", studentFormArrayList.get(position).getDeparture());
                intent.putExtra("ArrDate", studentFormArrayList.get(position).getDeparture());
                intent.putExtra("Arrival", studentFormArrayList.get(position).getArrival());
                intent.putExtra("Msg", studentFormArrayList.get(position).getArrival());
                intent.putExtra("DepDate", studentFormArrayList.get(position).getDeparture());
                intent.putExtra("ArrTime", studentFormArrayList.get(position).getArrival());
                intent.putExtra("DepTime", studentFormArrayList.get(position).getArrival());
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                holder.Arrival.getContext().startActivity(intent);

            }
        });
//    holder.Arrival.setText(studentForm.Arrival);


    }

    @Override
    public int getItemCount() {
        return studentFormArrayList.size();
    }

    public class StudentViewHolder extends RecyclerView.ViewHolder {

        TextView Arrival, Departure, Msg, ArrDate, DepTime, DepDate, ArrTime;


        public StudentViewHolder(@NonNull View itemView) {
            super(itemView);
            Arrival = itemView.findViewById(R.id.tv_arrival);
            ArrDate = itemView.findViewById(R.id.tv_arr_date);
            Departure = itemView.findViewById(R.id.tv_departure);
            DepTime = itemView.findViewById(R.id.tv_dep_time);
            DepDate = itemView.findViewById(R.id.tv_dep_date);
            ArrTime = itemView.findViewById(R.id.tv_arr_time);
            Msg = itemView.findViewById(R.id.tv_msg);
        }
    }
}
