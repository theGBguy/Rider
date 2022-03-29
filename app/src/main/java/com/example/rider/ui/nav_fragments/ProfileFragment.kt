package com.example.rider.ui.nav_fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.rider.databinding.FragmentProfileBinding
import com.example.rider.ui.RegisterActivity
import com.example.rider.utils.showShortToast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileFragment : Fragment() {
    private var binding: FragmentProfileBinding? = null

    private var fAuth: FirebaseAuth? = null
    private var fStore: FirebaseFirestore? = null
    private var userID: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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
        documentReference.addSnapshotListener { documentSnapshot, e ->
            binding?.profileAddressId?.setText(documentSnapshot?.getString("address"))
            binding?.profileEmailId?.setText(documentSnapshot?.getString("email"))
            binding?.profilePhoneId?.setText(documentSnapshot?.getString("phoneNumber"))
            binding?.profileFirstnameId?.setText(documentSnapshot?.getString("firstName"))
            binding?.profileLastnameId?.setText(documentSnapshot?.getString("lastName"))
        }
        binding?.updateBtnId?.setOnClickListener { updateProfile() }
    }

    override fun onStart() {
        super.onStart()
        userID = fAuth!!.currentUser!!.uid
        val documentReference = fStore!!.collection("users").document(
            userID!!
        )
        documentReference.get()
            .addOnCompleteListener { task ->
                if (task.result.exists()) {
                    val firstNameResult = task.result.getString("firstName")
                    val lastNameResult = task.result.getString("lastName")
                    val emailResult = task.result.getString("email")
                    val addressResult = task.result.getString("address")
                    val phoneResult = task.result.getString("phoneNumber")

                    binding?.profileFirstnameId?.setText(firstNameResult)
                    binding?.profileLastnameId?.setText(lastNameResult)
                    binding?.profileEmailId?.setText(emailResult)
                    binding?.profilePhoneId?.setText(phoneResult)
                    binding?.profileAddressId?.setText(addressResult)

                } else {
                    "No profile".showShortToast(requireContext())
                }
            }
    }

    private fun updateProfile() {
        val firstName = binding?.profileFirstnameId?.text.toString()
        val lastName = binding?.profileLastnameId?.text.toString()
        val address = binding?.profileAddressId?.text.toString()
        val email = binding?.profileEmailId?.text.toString()
        val phoneNumber = binding?.profilePhoneId?.text.toString()

        val sDoc = fStore!!.collection("users").document(
            userID!!
        )
        fStore!!.runTransaction<Void> { transaction ->
            transaction.update(sDoc, "firstName", firstName)
            transaction.update(sDoc, "lastName", lastName)
            transaction.update(sDoc, "email", email)
            transaction.update(sDoc, "phoneNumber", phoneNumber)
            transaction.update(sDoc, "address", address)
            null
        }.addOnSuccessListener { Toast.makeText(context, "successs", Toast.LENGTH_SHORT).show() }
            .addOnFailureListener { Toast.makeText(context, "failed", Toast.LENGTH_SHORT).show() }
    }
}