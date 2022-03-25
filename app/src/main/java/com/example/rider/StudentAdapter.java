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
        holder.DepTime.setText(studentFormArrayList.get(position).getDepTime());
        holder.DepDate.setText(studentFormArrayList.get(position).getDepDate());
        holder.Arrival.setText(studentFormArrayList.get(position).getArrival());
        holder.ArrDate.setText(studentFormArrayList.get(position).getArrDate());
        holder.ArrTime.setText(studentFormArrayList.get(position).getArrTime());
        holder.Msg.setText(studentFormArrayList.get(position).getMsg());
        holder.Weight.setText(studentFormArrayList.get(position).getWeight());
        holder.People.setText(studentFormArrayList.get(position).getPeople());
        holder.Name.setText(studentFormArrayList.get(position).getName());

        holder.Departure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(holder.Arrival.getContext(), DetailsActivity.class);
                intent.putExtra("Departure", studentFormArrayList.get(position).getDeparture());
                intent.putExtra("ArrDate", studentFormArrayList.get(position).getArrDate());
                intent.putExtra("Arrival", studentFormArrayList.get(position).getArrival());
                intent.putExtra("Msg", studentFormArrayList.get(position).getMsg());
                intent.putExtra("DepDate", studentFormArrayList.get(position).getDepDate());
                intent.putExtra("ArrTime", studentFormArrayList.get(position).getArrTime());
                intent.putExtra("DepTime", studentFormArrayList.get(position).getDepTime());
                intent.putExtra("Weight", studentFormArrayList.get(position).getWeight());
                intent.putExtra("People", studentFormArrayList.get(position).getPeople());
                intent.putExtra("Name", studentFormArrayList.get(position).getName());
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                holder.Arrival.getContext().startActivity(intent);

            }
        });



    }

    @Override
    public int getItemCount() {
        return studentFormArrayList.size();
    }

    public class StudentViewHolder extends RecyclerView.ViewHolder {

        TextView Arrival, Departure, Msg, ArrDate, DepTime, DepDate, ArrTime,Weight, People, Name;


        public StudentViewHolder(@NonNull View itemView) {
            super(itemView);
            Arrival = itemView.findViewById(R.id.tv_arrival);
            ArrDate = itemView.findViewById(R.id.tv_arr_date);
            Departure = itemView.findViewById(R.id.tv_departure);
            DepTime = itemView.findViewById(R.id.tv_dep_time);
            DepDate = itemView.findViewById(R.id.tv_dep_date);
            ArrTime = itemView.findViewById(R.id.tv_arr_time);
            Msg = itemView.findViewById(R.id.tv_msg);
            Weight = itemView.findViewById(R.id.tv_weight);
            People = itemView.findViewById(R.id.tv_people);
            Name = itemView.findViewById(R.id.tv_name);

        }
    }
}