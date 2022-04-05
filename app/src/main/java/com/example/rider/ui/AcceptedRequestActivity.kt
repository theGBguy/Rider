package com.example.rider.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.rider.databinding.ActivityAcceptedRequestBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class AcceptedRequestActivity : AppCompatActivity() {
    private var binding: ActivityAcceptedRequestBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAcceptedRequestBinding.inflate(layoutInflater)
        setContentView(binding!!.root)

        Firebase.auth.currentUser?.uid?.let {
            Firebase.firestore
                .collection("users")
                .document(it)
                .addSnapshotListener { documentSnapshot: DocumentSnapshot?, _: FirebaseFirestoreException? ->
                    binding?.tvSuccessMsg?.text = documentSnapshot?.getString("firstName")
                }
        }

        binding?.btnBack?.setOnClickListener {
            Intent(this@AcceptedRequestActivity, VolunteerSideNavBarActivity::class.java).also {
                startActivity(it)
                finish()
            }
        }
    }
}