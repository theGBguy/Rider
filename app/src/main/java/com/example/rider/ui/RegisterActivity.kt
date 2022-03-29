package com.example.rider.ui

import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.rider.R
import com.example.rider.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import java.io.*

class RegisterActivity : AppCompatActivity() {
    private var binding: ActivityRegisterBinding? = null

    private var dialog: ProgressDialog? = null

    private var fAuth: FirebaseAuth? = null
    private var fUser: FirebaseUser? = null
    private var fStore: FirebaseFirestore? = null
    private var userID: String? = null
    var setImageURI: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        fAuth = FirebaseAuth.getInstance()
        fUser = fAuth!!.currentUser
        fStore = FirebaseFirestore.getInstance()

        binding?.registerCancelBtnId?.setOnClickListener { finish() }
        binding?.registerRegisterBtnId?.setOnClickListener {
            performAuth()
            checkButton()
            dialog = ProgressDialog(this@RegisterActivity)
            dialog!!.setMessage("Signing in")
            dialog!!.show()
        }
        binding?.registerSelectProfileId?.setOnClickListener { selectImage() }
    }

    private fun checkButton() {
        when (binding?.radioGroupId?.checkedRadioButtonId) {
            R.id.rb_student_id -> {
                val Aintent = Intent(applicationContext, StudentLoginActivity::class.java)
                startActivity(Aintent)
            }
            R.id.rb_volunteer_id -> {
                val Cintent = Intent(applicationContext, VolunteerLoginActivity::class.java)
                startActivity(Cintent)
            }
        }
    }

    private fun performAuth() {
        val email = binding?.registerEmailId?.text.toString().trim { it <= ' ' }
        val password = binding?.registerPasswordId?.text.toString().trim { it <= ' ' }
        val firstName = binding?.registerFirstnameId?.text.toString().trim { it <= ' ' }
        val lastName = binding?.registerLastnameId?.text.toString().trim { it <= ' ' }
        val address = binding?.registerAddressId?.text.toString().trim { it <= ' ' }
        val phoneNumber = binding?.registerPhoneId?.text.toString().trim { it <= ' ' }
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
        if (email.matches(Patterns.EMAIL_ADDRESS.toRegex())) {
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
        if (password.length < 6) {
            binding?.registerPasswordId?.error = "Password must be >= 8 characters"
            return
        }
        if (phoneNumber.length < 10) {
            binding?.registerPhoneId?.error = "Password must be = 10 digits"
            return
        }


//        else {
//            progressDialog.setMessage("Plz wait while registration");
//            progressDialog.setTitle("Registration");
//            progressDialog.setCanceledOnTouchOutside(false);
//            progressDialog.show();
//        }
        fAuth!!.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(this@RegisterActivity, "User Created", Toast.LENGTH_SHORT).show()
                userID = fAuth!!.currentUser!!.uid
                val documentReference = fStore!!.collection("users").document(
                    userID!!
                )
                //                    Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                val user: MutableMap<String, Any> = HashMap()
                user["firstName"] = firstName
                user["lastName"] = lastName
                user["email"] = email
                user["phoneNumber"] = phoneNumber
                user["address"] = address
                documentReference.set(user).addOnSuccessListener {
                    Log.d(
                        TAG,
                        "onSuccess: user Profile is created$userID"
                    )
                }
                dialog!!.dismiss()
            } else {
                Toast.makeText(
                    this@RegisterActivity,
                    "Error !" + task.exception!!.message,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun selectImage() {
        val options = arrayOf<CharSequence>("Take Photo", "Choose from Gallery", "cancel")
        val builder = AlertDialog.Builder(this@RegisterActivity)
        builder.setTitle("Add Photo!")
        builder.setItems(options) { dialogInterface, i ->
            if (options[i] == "Take Photo") {
                val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                val f = File(Environment.getExternalStorageDirectory(), "temp.jpg")
                intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f))
                startActivityForResult(intent, 1)
            } else if (options[i] == "Choose from Gallery") {
                val intent =
                    Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(intent, 2)
            } else if (options[i] == "Cancel") {
                dialogInterface.dismiss()
            }
        }
        builder.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            if (requestCode == 1) {
                var f = File(Environment.getExternalStorageDirectory().toString())
                for (temp in f.listFiles()) {
                    if (temp.name == "temp.jpg") {
                        f = temp
                        break
                    }
                }
                try {
                    val bitmap: Bitmap
                    val bitmapOptions = BitmapFactory.Options()
                    bitmap = BitmapFactory.decodeFile(
                        f.absolutePath,
                        bitmapOptions
                    )
                    binding?.registerimageSelectId?.setImageBitmap(bitmap)
                    val path = (Environment
                        .getExternalStorageDirectory()
                        .toString() + File.separator
                            + "Phoenix" + File.separator + "default")
                    f.delete()
                    var outFile: OutputStream? = null
                    val file = File(path, System.currentTimeMillis().toString() + ".jpg")
                    try {
                        outFile = FileOutputStream(file)
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outFile)
                        outFile.flush()
                        outFile.close()
                    } catch (e: FileNotFoundException) {
                        e.printStackTrace()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else if (requestCode == 2) {
                val selectedImage = data!!.data
                val filePath = arrayOf(MediaStore.Images.Media.DATA)
                val c = contentResolver.query(selectedImage!!, filePath, null, null, null)
                c!!.moveToFirst()
                val columnIndex = c.getColumnIndex(filePath[0])
                val picturePath = c.getString(columnIndex)
                c.close()
                val thumbnail = BitmapFactory.decodeFile(picturePath)
                binding?.registerimageSelectId?.setImageBitmap(thumbnail)
            }
        }
    }

    companion object {
        const val TAG = "RegisterActivity"
        private const val IMAGE_PICK_CODE = 1000
        private const val PERRMISSION_CODE = 1001
    }
}