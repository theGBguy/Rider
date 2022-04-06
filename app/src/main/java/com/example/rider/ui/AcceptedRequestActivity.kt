package com.example.rider.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.rider.databinding.ActivityAcceptedRequestBinding
import com.example.rider.model.User
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
                    val userFirstName = documentSnapshot?.getString("firstName")
                    val state = intent.extras?.get(KEY_STATE)
                    if (state == STATE_ACCEPTED) {
                        binding?.tvState?.text = "SUCCESS"
                        binding?.tvStateMsg?.text = "Dear ${userFirstName}, you have accepted the request."
                    } else {
                        binding?.tvState?.text = "COMPLETED"
                        binding?.tvStateMsg?.text = "Dear ${userFirstName}, this request is completed. Thanks for trusting us."
                    }
                }
        }


        binding?.btnBack?.setOnClickListener {
//            Intent(this@AcceptedRequestActivity, VolunteerSideNavBarActivity::class.java).also {
//                startActivity(it)
//                finish()
//            }
            finish()
        }
    }

    companion object {
        const val KEY_STATE = "key_state"
        const val STATE_COMPLETED = 1
        const val STATE_ACCEPTED = 0
    }
}