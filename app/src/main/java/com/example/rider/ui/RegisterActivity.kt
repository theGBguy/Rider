package com.example.rider.ui

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Patterns
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import coil.load
import com.example.rider.R
import com.example.rider.databinding.ActivityRegisterBinding
import com.example.rider.model.User
import com.example.rider.utils.showShortToast
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.google.firebase.storage.ktx.storageMetadata
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class RegisterActivity : AppCompatActivity() {
    private var binding: ActivityRegisterBinding? = null

    private var userID: String? = null
    private var userType: Int = User.TYPE_STUDENT

    private lateinit var currentPhotoUri: Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        binding?.registerCancelBtnId?.setOnClickListener { finish() }
        binding?.registerRegisterBtnId?.setOnClickListener { performAuth() }
        binding?.registerSelectProfileId?.setOnClickListener { selectImage() }
        binding?.radioGroupId?.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rb_student_id -> userType = User.TYPE_STUDENT
                R.id.rb_volunteer_id -> userType = User.TYPE_VOLUNTEER
            }
        }
    }

    private fun moveToLoginActivity() {
        when (userType) {
            User.TYPE_STUDENT -> {
                Intent(this, StudentLoginActivity::class.java).also {
                    startActivity(it)
                }
            }
            else -> {
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
        dialog.setMessage("Registering your details...")
        dialog.setCanceledOnTouchOutside(false)
        dialog.show()

        val user = User(
            firstName = firstName,
            lastName = lastName,
            email = email,
            password = password,
            phoneNumber = phoneNumber,
            address = address,
            type = userType
        )

        val createUserTask = Firebase.auth.createUserWithEmailAndPassword(email, password)
//        val storeUserDetailsTask = Firebase.firestore.collection("users")
//            .document(userID!!)
//            .set(user)
//        val storeUserImageTask = Firebase.storage
//            .reference
//            .child("images/${userID}/${currentPhotoUri.lastPathSegment}")
//            .putFile(currentPhotoUri, storageMetadata { contentType = "image/jpeg" })

        // register -> upload image -> get image url -> push user details

        createUserTask.continueWithTask {
            if (it.isSuccessful) {
                Firebase.auth.currentUser?.uid?.let { userID ->
                    Firebase.storage
                        .reference
                        .child("images/${userID}/${user.firstName}-profile")
                        .putFile(currentPhotoUri, storageMetadata { contentType = "image/jpeg" })
                }
            } else {
                "Error ! ${it.exception?.message}".showShortToast(this@RegisterActivity)
                null
            }
        }.continueWithTask {
            Firebase.auth.currentUser?.uid?.let { userID ->
                Firebase.storage
                    .reference
                    .child("images/${userID}/${user.firstName}-profile")
                    .downloadUrl
            }
        }.continueWithTask {
            if (it.isSuccessful) {
                user.imageLocation = it.result.toString()
                Firebase.auth.uid?.let { userID ->
                    Firebase.firestore.collection("users")
                        .document(userID)
                        .set(user)
                }
            } else {
                "Error ! ${it.exception?.message}".showShortToast(this@RegisterActivity)
                null
            }
        }.addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                "User Created".showShortToast(this@RegisterActivity)
                // sign out as creating account will sign in automatically
                Firebase.auth.signOut()
                moveToLoginActivity()
            } else {
                if (task.exception?.message?.lowercase()?.contains("continuation") != true) {
                    "Error ! ${task.exception?.message}".showShortToast(this@RegisterActivity)
                }
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
                binding?.registerimageSelectId?.load(currentPhotoUri)
            } else if (requestCode == IMAGE_PICK_CODE) {
                binding?.registerimageSelectId?.load(data?.data.also {
                    if (it != null) {
                        currentPhotoUri = it
                    }
                })
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
            currentPhotoUri = Uri.fromFile(File(absolutePath))
        }
    }

    companion object {
        const val TAG = "RegisterActivity"
        private const val IMAGE_PICK_CODE = 1000
        private const val IMAGE_CAPTURE_CODE = 1001
    }
}