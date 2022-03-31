package com.example.rider.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.rider.databinding.ActivityAcceptedRequestBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException

class AcceptedRequestActivity : AppCompatActivity() {
    private var binding: ActivityAcceptedRequestBinding? = null
    private var fAuth: FirebaseAuth? = null
    private var fStore: FirebaseFirestore? = null
    private var userID: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAcceptedRequestBinding.inflate(layoutInflater)
        setContentView(binding!!.root)

        fAuth = FirebaseAuth.getInstance()
        fStore = FirebaseFirestore.getInstance()
        userID = fAuth!!.currentUser!!.uid

        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            Log.d(RegisterActivity.TAG, "onCreate: \"+user.getDisplayName()")
        }
        val documentReference = fStore!!.collection("users").document(
            userID!!
        )
        documentReference.addSnapshotListener { documentSnapshot: DocumentSnapshot?, e: FirebaseFirestoreException? ->
            binding!!.successUsernameId.text = documentSnapshot!!.getString("firstName")
        }
        binding!!.homeBtnId.setOnClickListener { view: View? ->
            val intent = Intent(this@AcceptedRequestActivity, VolunteerSideNavBarActivity::class.java)
            startActivity(intent)
        }
    }
}