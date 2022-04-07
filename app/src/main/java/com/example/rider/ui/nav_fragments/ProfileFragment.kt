package com.example.rider.ui.nav_fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import coil.load
import coil.transform.CircleCropTransformation
import com.example.rider.R
import com.example.rider.databinding.FragmentProfileBinding
import com.example.rider.ui.RegisterActivity
import com.example.rider.utils.showShortToast
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


class ProfileFragment : Fragment() {
    private var binding: FragmentProfileBinding? = null

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

        val currentUser = Firebase.auth.currentUser
        if (currentUser != null) {
            Log.d(RegisterActivity.TAG, "onCreate: \"+user.getDisplayName()")
        }
        currentUser?.uid?.let { uid ->
            Firebase.firestore.collection("users")
                .document(uid)
                .addSnapshotListener { documentSnapshot, e ->
                    binding?.profileImage?.load(documentSnapshot?.getString("profileImageLocation")) {
                        crossfade(true)
                        placeholder(R.drawable.yatra)
                        fallback(R.drawable.icon_user)
                        transformations(CircleCropTransformation())
                    }
                    binding?.legalImage?.load(documentSnapshot?.getString("idImageLocation")) {
                        crossfade(true)
                        placeholder(R.drawable.yatra)
                        fallback(R.drawable.icon_profile)
                    }
                    binding?.profileAddressId?.setText(documentSnapshot?.getString("address"))
                    binding?.profileEmailId?.setText(documentSnapshot?.getString("email"))
                    binding?.profilePhoneId?.setText(documentSnapshot?.getString("phoneNumber"))
                    binding?.profileFirstnameId?.setText(documentSnapshot?.getString("firstName"))
                    binding?.profileLastnameId?.setText(documentSnapshot?.getString("lastName"))
                }
        }
        binding?.cancelBtnId?.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
        binding?.updateBtnId?.setOnClickListener { updateProfile() }
    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }

    private fun updateProfile() {
        val firstName = binding?.profileFirstnameId?.text.toString()
        val lastName = binding?.profileLastnameId?.text.toString()
        val address = binding?.profileAddressId?.text.toString()
        val email = binding?.profileEmailId?.text.toString()
        val phoneNumber = binding?.profilePhoneId?.text.toString()

        Firebase.auth.currentUser?.uid?.let { uid ->
            val userDocRef = Firebase.firestore.collection("users").document(uid)
            Firebase.firestore.runTransaction<Void> { transaction ->
                transaction.update(userDocRef, "firstName", firstName)
                transaction.update(userDocRef, "lastName", lastName)
                transaction.update(userDocRef, "email", email)
                transaction.update(userDocRef, "phoneNumber", phoneNumber)
                transaction.update(userDocRef, "address", address)
                null
            }.addOnCompleteListener {
                if (it.isSuccessful) {
                    "Updated the profile successfully".showShortToast(requireContext())
                } else {
                    "Profile update failed".showShortToast(requireContext())
                }
            }
        }
    }
}