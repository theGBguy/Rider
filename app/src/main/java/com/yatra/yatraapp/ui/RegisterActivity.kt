package com.yatra.yatraapp.ui

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import android.util.Patterns
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.widget.doOnTextChanged
import coil.load
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.google.firebase.storage.ktx.storageMetadata
import com.mapbox.search.*
import com.mapbox.search.result.SearchResult
import com.mapbox.search.result.SearchSuggestion
import com.yatra.yatraapp.R
import com.yatra.yatraapp.databinding.ActivityRegisterBinding
import com.yatra.yatraapp.model.User
import com.yatra.yatraapp.utils.showShortToast
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class RegisterActivity : AppCompatActivity() {
    private var binding: ActivityRegisterBinding? = null

    private var userType: Int = User.TYPE_STUDENT

    private lateinit var currentProfilePhotoUri: Uri
    private lateinit var currentLegalPhotoUri: Uri
    private var isProfile: Boolean? = null

    private lateinit var location: String
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0

    private lateinit var searchEngine: SearchEngine
    private lateinit var searchRequestTask: SearchRequestTask

    private val searchCallback = object : SearchSelectionCallback {

        override fun onSuggestions(
            suggestions: List<SearchSuggestion>,
            responseInfo: ResponseInfo
        ) {
            if (suggestions.isEmpty()) {
                "No suggestions found".showShortToast(this@RegisterActivity)
            } else {
                val adapter = ArrayAdapter(
                    this@RegisterActivity,
                    android.R.layout.select_dialog_item,
                    suggestions.map { it.name }
                )

                binding?.registerAddressId?.setAdapter(adapter)
                binding?.registerAddressId?.setOnItemClickListener { _, _, pos, _ ->
                    searchRequestTask = searchEngine.select(suggestions[pos], this)
                }
            }
        }

        override fun onResult(
            suggestion: SearchSuggestion,
            result: SearchResult,
            responseInfo: ResponseInfo
        ) {
            location = result.name
            latitude = result.coordinate?.latitude()!!
            longitude = result.coordinate?.longitude()!!
            "$location $latitude $longitude".showShortToast(this@RegisterActivity)
        }

        override fun onCategoryResult(
            suggestion: SearchSuggestion,
            results: List<SearchResult>,
            responseInfo: ResponseInfo
        ) {
            Log.i("SearchApiExample", "Category search results: $results")
        }

        override fun onError(e: Exception) {
            Log.i("SearchApiExample", "Search error", e)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        searchEngine = MapboxSearchSdk.getSearchEngine()

        binding?.registerAddressId?.threshold = 3
        binding?.registerAddressId?.doOnTextChanged { text, _, _, _ ->
            searchRequestTask = searchEngine.search(
                text?.trim().toString(),
                SearchOptions(limit = 3),
                searchCallback
            )
        }

        binding?.registerCancelBtnId?.setOnClickListener { finish() }
        binding?.registerRegisterBtnId?.setOnClickListener { performAuth() }
        binding?.registerSelectProfileId?.setOnClickListener {
            isProfile = true
            selectImage()
        }
        binding?.btnSelectLegalId?.setOnClickListener {
            isProfile = false
            selectImage()
        }
        binding?.radioGroupId?.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rb_student_id -> userType = User.TYPE_STUDENT
                R.id.rb_volunteer_id -> userType = User.TYPE_VOLUNTEER
            }
        }

    }

    private fun performAuth() {
        val email = binding?.registerEmailId?.text.toString().trim()
        val password = binding?.registerPasswordId?.text.toString().trim()
        val firstName = binding?.registerFirstnameId?.text.toString().trim()
        val lastName = binding?.registerLastnameId?.text.toString().trim()
        val address = binding?.registerAddressId?.text.toString().trim()
        val phoneNumber = binding?.registerPhoneId?.text.toString().trim()

        if (!this::currentProfilePhotoUri.isInitialized) {
            "Please select a profile picture".showShortToast(this)
        }
        if (!this::currentLegalPhotoUri.isInitialized) {
            "Please select a picture of national id".showShortToast(this)
        }

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
        dialog.setMessage("Registering your details, please wait...")
        dialog.setCanceledOnTouchOutside(false)
        dialog.show()

        val user = User(
            firstName = firstName,
            lastName = lastName,
            email = email,
            password = password,
            phoneNumber = phoneNumber,
            address = address,
            latitude = latitude,
            longitude = longitude,
            type = userType
        )

        val createUserTask = Firebase.auth.createUserWithEmailAndPassword(email, password)

        // register -> upload images -> get image urls -> push user details

        dialog.setMessage("Creating user account...")
        createUserTask.continueWithTask {
            if (it.isSuccessful) {
                dialog.setMessage("Uploading profile image...")
                Firebase.auth.currentUser?.uid?.let { userID ->
                    Firebase.storage
                        .reference
                        .child("images/${userID}/${user.firstName}-profile")
                        .putFile(
                            currentProfilePhotoUri,
                            storageMetadata { contentType = "image/jpeg" })
                }
            } else {
                "Error ! ${it.exception?.message}".showShortToast(this@RegisterActivity)
                null
            }
        }.continueWithTask {
            if (it.isSuccessful) {
                Firebase.auth.currentUser?.uid?.let { userID ->
                    Firebase.storage
                        .reference
                        .child("images/${userID}/${user.firstName}-profile")
                        .downloadUrl
                }
            } else {
                "Error ! ${it.exception?.message}".showShortToast(this@RegisterActivity)
                null
            }
        }.continueWithTask {
            if (it.isSuccessful) {
                user.profileImageLocation = it.result.toString()
                dialog.setMessage("Uploading ID documents...")
                Firebase.auth.currentUser?.uid?.let { userID ->
                    Firebase.storage
                        .reference
                        .child("images/${userID}/${user.firstName}-id")
                        .putFile(
                            currentLegalPhotoUri,
                            storageMetadata { contentType = "image/jpeg" })
                }
            } else {
                "Error ! ${it.exception?.message}".showShortToast(this@RegisterActivity)
                null
            }
        }.continueWithTask {
            if (it.isSuccessful) {
                Firebase.auth.currentUser?.uid?.let { userID ->
                    Firebase.storage
                        .reference
                        .child("images/${userID}/${user.firstName}-id")
                        .downloadUrl
                }
            } else {
                "Error ! ${it.exception?.message}".showShortToast(this@RegisterActivity)
                null
            }
        }.continueWithTask {
            if (it.isSuccessful) {
                user.idImageLocation = it.result.toString()
                dialog.setMessage("Uploading user details...")
                Firebase.auth.uid?.let { userID ->
                    user.id = userID
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
                finish()
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
            if (isProfile == true) {
                if (requestCode == IMAGE_CAPTURE_CODE) {
                    binding?.registerimageSelectId?.load(currentProfilePhotoUri)
                } else if (requestCode == IMAGE_PICK_CODE) {
                    binding?.registerimageSelectId?.load(data?.data.also {
                        if (it != null) {
                            currentProfilePhotoUri = it
                        }
                    })
                }
            } else {
                if (requestCode == IMAGE_CAPTURE_CODE) {
                    binding?.ivLegalId?.load(currentLegalPhotoUri)
                } else if (requestCode == IMAGE_PICK_CODE) {
                    binding?.ivLegalId?.load(data?.data.also {
                        if (it != null) {
                            currentLegalPhotoUri = it
                        }
                    })
                }
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
        return File.createTempFile(
            "${if (isProfile == true) "PROFILE" else "ID"}_JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        ).apply {
            if (isProfile == true) {
                currentProfilePhotoUri = Uri.fromFile(File(absolutePath))
            } else {
                currentLegalPhotoUri = Uri.fromFile(File(absolutePath))
            }
        }
    }

    override fun onDestroy() {
        searchRequestTask.cancel()
        super.onDestroy()
    }

    companion object {
        const val TAG = "RegisterActivity"
        private const val IMAGE_PICK_CODE = 1000
        private const val IMAGE_CAPTURE_CODE = 1001
    }
}

