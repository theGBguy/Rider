package com.example.rider.ui

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import android.util.Patterns
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import coil.load
import com.example.rider.R
import com.example.rider.databinding.ActivityRegisterBinding
import com.example.rider.utils.showShortToast
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class RegisterActivity : AppCompatActivity() {
    private var binding: ActivityRegisterBinding? = null

    private var firebaseAuth: FirebaseAuth? = null
    private var firestore: FirebaseFirestore? = null

    private var userID: String? = null

    private lateinit var currentPhotoPath: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        firebaseAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        binding?.registerCancelBtnId?.setOnClickListener { finish() }
        binding?.registerRegisterBtnId?.setOnClickListener { performAuth() }
        binding?.registerSelectProfileId?.setOnClickListener { selectImage() }
    }

    private fun moveToLoginActivity() {
        when (binding?.radioGroupId?.checkedRadioButtonId) {
            R.id.rb_student_id -> {
                Intent(this, StudentLoginActivity::class.java).also {
                    startActivity(it)
                }
            }
            R.id.rb_volunteer_id -> {
                Intent(this, VolunteerLoginActivity::class.java).also {
                    startActivity(it)
                }
            }
        }
        finish()
    }

    private fun performAuth() {
        val email = binding?.registerEmailId?.text.toString().trim()
        val password = binding?.registerPasswordId?.text.toString().trim()
        val firstName = binding?.registerFirstnameId?.text.toString().trim()
        val lastName = binding?.registerLastnameId?.text.toString().trim()
        val address = binding?.registerAddressId?.text.toString().trim()
        val phoneNumber = binding?.registerPhoneId?.text.toString().trim()

        if (TextUtils.isEmpty(firstName)) {
            binding?.registerFirstnameId?.error = "FirstName is required"
            return
        }
        if (TextUtils.isEmpty(lastName)) {
            binding?.registerLastnameId?.error = "LastName is required"
            return
        }
        if (TextUtils.isEmpty(email)) {
            binding?.registerEmailId?.error = "Email is required"
            return
        }
        if (!email.matches(Patterns.EMAIL_ADDRESS.toRegex())) {
            binding?.registerEmailId?.error = "Email is badly formatted"
            return
        }
        if (TextUtils.isEmpty(password)) {
            binding?.registerPasswordId?.error = "Password is required"
            return
        }
        if (TextUtils.isEmpty(address)) {
            binding?.registerAddressId?.error = "Address is required"
            return
        }
        if (password.length < 8) {
            binding?.registerPasswordId?.error = "Password must be >= 8 characters"
            return
        }
        if (phoneNumber.length != 10) {
            binding?.registerPhoneId?.error = "Phone number must be equal to 10 digits"
            return
        }

        val dialog = ProgressDialog(this)
        dialog.setMessage("Signing in")
        dialog.setCanceledOnTouchOutside(false)
        dialog.show()

        firebaseAuth!!.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    "User Created".showShortToast(this@RegisterActivity)

                    userID = firebaseAuth?.currentUser?.uid
                    val documentReference = firestore?.collection("users")
                        ?.document(userID!!)

                    val user: MutableMap<String, Any> = HashMap()
                    user["firstName"] = firstName
                    user["lastName"] = lastName
                    user["email"] = email
                    user["phoneNumber"] = phoneNumber
                    user["address"] = address

                    documentReference?.set(user)?.addOnSuccessListener {
                        Log.d(TAG, "onSuccess: User Profile is created$userID")
                    }
                    moveToLoginActivity()
                } else {
                    "Error ! ${task.exception?.message}".showShortToast(this@RegisterActivity)
                }
                dialog.dismiss()
            }
    }

    private fun selectImage() {
        val options = arrayOf("Take Photo", "Choose from Gallery", "Cancel")

        MaterialAlertDialogBuilder(this)
            .setTitle("Add Photo!")
            .setItems(options) { dialogInterface, i ->
                when {
                    options[i] == "Take Photo" -> {
                        dispatchTakePictureIntent()
                    }
                    options[i] == "Choose from Gallery" -> {
                        Intent(
                            Intent.ACTION_PICK,
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                        ).also {
                            startActivityForResult(it, IMAGE_PICK_CODE)
                        }
                    }
                    options[i] == "Cancel" -> {
                        dialogInterface.dismiss()
                    }
                }
            }.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            if (requestCode == IMAGE_CAPTURE_CODE) {
                binding?.registerimageSelectId?.load(currentPhotoPath)
            } else if (requestCode == IMAGE_PICK_CODE) {
                binding?.registerimageSelectId?.load(data?.data)
            }
        }
    }

    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(packageManager)?.also {
                val photoFile: File? = try {
                    createImageFile()
                } catch (ex: IOException) {
                    null
                }
                photoFile?.also {
                    val photoURI: Uri = FileProvider.getUriForFile(
                        this,
                        "com.example.rider.fileprovider",
                        it
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(takePictureIntent, IMAGE_CAPTURE_CODE)
                }
            }
        }
    }

    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir).apply {
            currentPhotoPath = absolutePath
        }
    }

    companion object {
        const val TAG = "RegisterActivity"
        private const val IMAGE_PICK_CODE = 1000
        private const val IMAGE_CAPTURE_CODE = 1001
    }
}