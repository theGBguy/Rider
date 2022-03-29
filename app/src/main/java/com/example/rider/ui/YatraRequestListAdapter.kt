package com.example.rider.ui

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.rider.databinding.YatraRequestRowBinding
import com.example.rider.model.YatraRequest

class YatraRequestListAdapter(
    private var yatraRequestList: ArrayList<YatraRequest?>?
) : RecyclerView.Adapter<YatraRequestViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): YatraRequestViewHolder {
        return YatraRequestViewHolder.create(parent)
    }

    override fun onBindViewHolder(holder: YatraRequestViewHolder, position: Int) {
        yatraRequestList?.get(position)?.let { holder.bind(it) }
    }

    override fun onViewDetachedFromWindow(holder: YatraRequestViewHolder) {
        super.onViewDetachedFromWindow(holder)
        holder.unbind()
    }

    override fun getItemCount(): Int {
        return yatraRequestList!!.size
    }
}

class YatraRequestViewHolder(
    private var binding: YatraRequestRowBinding?
) : RecyclerView.ViewHolder(binding?.root!!) {

    fun bind(yatraRequest: YatraRequest) {
        binding?.tvDeparture?.text = yatraRequest.departure
        binding?.tvDepTime?.text = yatraRequest.departureTime
        binding?.tvDepDate?.text = yatraRequest.departureDate
        binding?.tvArrival?.text = yatraRequest.arrival
        binding?.tvArrTime?.text = yatraRequest.arrivalTime
        binding?.tvMsg?.text = yatraRequest.msg
        binding?.tvWeight?.text = yatraRequest.weight
        binding?.tvPeople?.text = yatraRequest.peopleCount.toString()
        binding?.tvName?.text = yatraRequest.name

        binding?.tvDeparture?.setOnClickListener {
            val intent = Intent(it.context, DetailsActivity::class.java)
            intent.putExtra("Departure", yatraRequest.departure)
            intent.putExtra("ArrDate", yatraRequest.arrivalDate)
            intent.putExtra("Arrival", yatraRequest.arrival)
            intent.putExtra("Msg", yatraRequest.msg)
            intent.putExtra("DepDate", yatraRequest.departureDate)
            intent.putExtra("ArrTime", yatraRequest.arrivalTime)
            intent.putExtra("DepTime", yatraRequest.departureTime)
            intent.putExtra("Weight", yatraRequest.weight)
            intent.putExtra("People", yatraRequest.peopleCount)
            intent.putExtra("Name", yatraRequest.name)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            it.context.startActivity(intent)
        }
    }

    fun unbind() {
        binding = null
    }

    companion object {
        fun create(parent: ViewGroup): YatraRequestViewHolder {
            return YatraRequestViewHolder(
                YatraRequestRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            )
        }
    }
}