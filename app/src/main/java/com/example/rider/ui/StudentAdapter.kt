package com.example.rider.ui

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.rider.databinding.StudentfromItemBinding
import com.example.rider.model.StudentForm

class StudentAdapter(
    private var studentFormList: ArrayList<StudentForm?>?
) : RecyclerView.Adapter<StudentViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StudentViewHolder {
        return StudentViewHolder.create(parent)
    }

    override fun onBindViewHolder(holder: StudentViewHolder, position: Int) {
        studentFormList?.get(position)?.let { holder.bind(it) }
    }

    override fun onViewDetachedFromWindow(holder: StudentViewHolder) {
        super.onViewDetachedFromWindow(holder)
        holder.unbind()
    }

    override fun getItemCount(): Int {
        return studentFormList!!.size
    }
}

class StudentViewHolder(
    private var binding: StudentfromItemBinding?
) : RecyclerView.ViewHolder(binding?.root!!) {

    fun bind(studentForm: StudentForm) {
        binding?.tvDeparture?.text = studentForm.departure
        binding?.tvDepTime?.text = studentForm.departureTime
        binding?.tvDepDate?.text = studentForm.departureDate
        binding?.tvArrival?.text = studentForm.arrival
        binding?.tvArrTime?.text = studentForm.arrivalTime
        binding?.tvMsg?.text = studentForm.msg
        binding?.tvWeight?.text = studentForm.weight
        binding?.tvPeople?.text = studentForm.people
        binding?.tvName?.text = studentForm.name

        binding?.tvDeparture?.setOnClickListener {
            val intent = Intent(it.context, DetailsActivity::class.java)
            intent.putExtra("Departure", studentForm.departure)
            intent.putExtra("ArrDate", studentForm.arrivalDate)
            intent.putExtra("Arrival", studentForm.arrival)
            intent.putExtra("Msg", studentForm.msg)
            intent.putExtra("DepDate", studentForm.departureDate)
            intent.putExtra("ArrTime", studentForm.arrivalTime)
            intent.putExtra("DepTime", studentForm.departureTime)
            intent.putExtra("Weight", studentForm.weight)
            intent.putExtra("People", studentForm.people)
            intent.putExtra("Name", studentForm.name)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            it.context.startActivity(intent)
        }
    }

    fun unbind() {
        binding = null
    }

    companion object {
        fun create(parent: ViewGroup): StudentViewHolder {
            return StudentViewHolder(
                StudentfromItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            )
        }
    }
}