package com.example.rider.ui

import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Patterns
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.example.rider.R
import com.example.rider.databinding.StudentLoginBinding
import com.example.rider.utils.showShortToast
import com.google.android.gms.tasks.Task
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth

class StudentLoginActivity : AppCompatActivity() {
    private var binding: StudentLoginBinding? = null

    var dialog: ProgressDialog? = null

    var firebaseAuth: FirebaseAuth? = null

    override fun onBackPressed() {
        if (firebaseAuth?.currentUser != null) {
            MaterialAlertDialogBuilder(this)
                .setTitle(R.string.app_name)
                .setIcon(R.mipmap.ic_launcher)
                .setMessage("Do you want to Log Out?")
                .setCancelable(false)
                .setPositiveButton("Yes") { _: DialogInterface?, _: Int ->
                    firebaseAuth!!.signOut()
                    finish()
                }
                .setNegativeButton("No") { dialog: DialogInterface, _: Int -> dialog.cancel() }
                .show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = StudentLoginBinding.inflate(layoutInflater)
        setContentView(binding!!.root)

        firebaseAuth = FirebaseAuth.getInstance()

        binding?.loginRegisterTextViewId?.setOnClickListener {
            Intent(this@StudentLoginActivity, RegisterActivity::class.java).also {
                startActivity(it)
            }
        }

        binding?.loginSForgetpasswordId?.setOnClickListener {
            val resetMail = EditText(this@StudentLoginActivity)

            MaterialAlertDialogBuilder(this@StudentLoginActivity)
                .setTitle("Reset Password")
                .setMessage("Enter your Mail to receive Reset Link")
                .setView(resetMail)
                .setPositiveButton("Yes") { _: DialogInterface?, _: Int ->
                    val mail = resetMail.text.toString()
                    firebaseAuth!!.sendPasswordResetEmail(mail)
                        .addOnSuccessListener {
                            "Reset Link has been sent in your mail".showShortToast(this@StudentLoginActivity)
                        }
                        .addOnFailureListener { e: Exception ->
                            "Link is not sent : ${e.message}".showShortToast(this@StudentLoginActivity)
                        }
                }
        }

        binding?.loginLoginBtnId?.setOnClickListener {
            performLogin()
            dialog = ProgressDialog(this@StudentLoginActivity)
            dialog!!.setMessage("Logging in")
            dialog!!.show()
        }
    }

    private fun performLogin() {
        val email = binding?.loginEmailId?.text.toString().trim()
        val password = binding?.loginPasswordId?.text.toString().trim()

        if (TextUtils.isEmpty(email)) {
            binding?.loginEmailId?.error = "Email is required"
            return
        }

        if (email.matches(Patterns.EMAIL_ADDRESS.toRegex())) {
            binding?.loginEmailId?.error = "Email is badly formatted"
            return
        }

        if (TextUtils.isEmpty(password)) {
            binding?.loginPasswordId?.error = "Password is required"
            return
        }

        firebaseAuth!!.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task: Task<AuthResult?> ->
                if (task.isSuccessful) {
                    "Logged in".showShortToast(this@StudentLoginActivity)
                    Intent(this@StudentLoginActivity, StudentSideNavBarActivity::class.java).also {
                        startActivity(it)
                    }
                    dialog!!.dismiss()
                } else {
                    "Error ! ${task.exception!!.message}".showShortToast(this@StudentLoginActivity)
                }
            }
    }
}