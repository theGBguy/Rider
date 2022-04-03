package com.example.rider.ui

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.rider.R
import com.example.rider.databinding.ActivityDetailsBinding
import com.example.rider.model.User
import com.example.rider.model.YatraRequest
import com.example.rider.utils.showShortToast
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class DetailsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivityDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val yatraRequest = intent.getParcelableExtra("key_data") as YatraRequest?

        binding.arrivalholder.text = yatraRequest?.arrivalLocation?.split(",")?.get(0)
        binding.departureholder.text = yatraRequest?.departureLocation?.split(",")?.get(0)
        binding.msgholder.text = yatraRequest?.msg
        binding.arrivaltimeholder.text = yatraRequest?.arrivalTime
        binding.arrdateholder.text = yatraRequest?.arrivalDate
        binding.deptimeholder.text = yatraRequest?.departureTime
        binding.depdateholder.text = yatraRequest?.departureDate
        binding.weightholder.text = yatraRequest?.weight.toString()
        binding.peopleholder.text = yatraRequest?.peopleCount.toString()

        Firebase.auth.currentUser?.uid?.let {
            Firebase.firestore.collection("users")
                .document(it)
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val type = task.result["type"] as Long
                        if (type.toInt() == User.TYPE_STUDENT) {
                            binding.nameholder.visibility = View.GONE
                            binding.backBtnId.visibility = View.GONE
                            binding.acceptBtnId.visibility = View.GONE
                        }
                    }
                }
        }

        binding.backBtnId.setOnClickListener { finish() }

        binding.acceptBtnId.setOnClickListener {
            MaterialAlertDialogBuilder(this)
                .setTitle(R.string.app_name)
                .setIcon(R.mipmap.ic_launcher)
                .setMessage("Do you want to Accept?")
                .setCancelable(false)
                .setPositiveButton("Yes") { _: DialogInterface?, _: Int ->
                    Firebase.firestore
                        .collection("yatra_requests")
                        .document(yatraRequest?.requestId!!)
                        .update("acceptorId", Firebase.auth.currentUser?.uid)
                        .addOnCompleteListener {
                            if (it.isSuccessful) {
                                "Yatra ride accepted successfully!".showShortToast(this)
                                val intent = Intent(
                                    this@DetailsActivity,
                                    AcceptedRequestActivity::class.java
                                )
                                startActivity(intent)
                            } else {
                                "Error updating status : ${it.exception?.message}".showShortToast(
                                    this
                                )
                            }
                        }
                }
                .setNegativeButton("No") { dialog: DialogInterface, _: Int -> dialog.cancel() }
                .show()
        }
    }
}