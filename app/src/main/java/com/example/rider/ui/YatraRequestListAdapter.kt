package com.example.rider.ui

import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.rider.databinding.YatraRequestRowBinding
import com.example.rider.model.YatraRequest
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions

class YatraRequestListAdapter(
    options: FirestoreRecyclerOptions<YatraRequest>
) : FirestoreRecyclerAdapter<YatraRequest, YatraRequestViewHolder>(options) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        position: Int
    ): YatraRequestViewHolder {
        return YatraRequestViewHolder.create(parent)
    }

    override fun onBindViewHolder(
        holder: YatraRequestViewHolder,
        position: Int,
        model: YatraRequest
    ) {
        holder.bind(model)
    }

    override fun onViewDetachedFromWindow(holder: YatraRequestViewHolder) {
        super.onViewDetachedFromWindow(holder)
        holder.unbind()
    }
}

class YatraRequestViewHolder(
    private var binding: YatraRequestRowBinding?
) : RecyclerView.ViewHolder(binding?.root!!) {

    fun bind(yatraRequest: YatraRequest) {
        binding?.tvDeparture?.text =
            "Departure : ${yatraRequest.departureLocation}"
        binding?.tvArrival?.text =
            "Arrival : ${yatraRequest.arrivalLocation}"
//        binding?.tvName?.text =
//            if (yatraRequest.name?.isBlank() == true) "Name : Not available" else "Name : ${yatraRequest.name}"
        binding?.tvStatus?.apply {
            text = if (yatraRequest.acceptorId?.isBlank() == true) {
                "Not accepted"
            } else {
                if (yatraRequest.completed == true) {
                    "Completed"
                } else {
                    "Accepted"
                }
            }
            setBackgroundColor(if (yatraRequest.acceptorId?.isBlank() == true) Color.RED else Color.GREEN)
        }

        binding?.root?.setOnClickListener {
            val intent = Intent(it.context, DetailsActivity::class.java)
            intent.putExtra("key_data", yatraRequest)
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