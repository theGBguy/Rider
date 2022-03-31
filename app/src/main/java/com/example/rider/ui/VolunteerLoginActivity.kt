package com.example.rider.ui

import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Patterns
import android.widget.EditText
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.example.rider.R
import com.example.rider.databinding.ActivityVolunteerLoginBinding
import com.example.rider.utils.showShortToast
import com.google.android.gms.tasks.Task
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth

class VolunteerLoginActivity : AppCompatActivity() {
    private var binding: ActivityVolunteerLoginBinding? = null

    private var firebaseAuth: FirebaseAuth? = null

    override fun onBackPressed() {
        if (firebaseAuth?.currentUser != null) {
            MaterialAlertDialogBuilder(this@VolunteerLoginActivity)
                .setTitle(R.string.app_name)
                .setIcon(R.mipmap.ic_launcher)
                .setMessage("Do you want to Log Out?")
                .setCancelable(false)
                .setPositiveButton("Yes") { _: DialogInterface?, _: Int ->
                    firebaseAuth?.signOut()
                    finish()
                }
                .setNegativeButton("No") { dialog: DialogInterface?, _: Int -> dialog?.cancel() }
                .show()
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityVolunteerLoginBinding.inflate(layoutInflater)
        setContentView(binding!!.root)

        firebaseAuth = FirebaseAuth.getInstance()

        binding?.loginRegisterTextViewId?.setOnClickListener { _ ->
            Intent(this@VolunteerLoginActivity, RegisterActivity::class.java).also {
                startActivity(it)
            }
        }

        binding?.loginForgetpasswordId?.setOnClickListener {
            val linearLayout = LinearLayout(this)
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            linearLayout.orientation = LinearLayout.VERTICAL
            linearLayout.layoutParams = params
            linearLayout.setPadding(24, 12, 12, 24)

            val resetMail = EditText(this)
            linearLayout.addView(resetMail)

            MaterialAlertDialogBuilder(this)
                .setTitle("Reset Password")
                .setMessage("Enter your Mail to receive Reset Link")
                .setView(resetMail)
                .setPositiveButton("Yes") { _: DialogInterface?, _: Int ->
                    val mail = resetMail.text.toString()
                    if (mail.isNotBlank()) {
                        firebaseAuth!!.sendPasswordResetEmail(mail)
                            .addOnSuccessListener {
                                "Reset Link has been sent in your mail".showShortToast(this@VolunteerLoginActivity)
                            }
                            .addOnFailureListener { e: Exception ->
                                "Link is not sent : ${e.message}".showShortToast(this@VolunteerLoginActivity)
                            }
                    }
                }.show()
        }

        binding?.loginLoginBtnId?.setOnClickListener { performLogin() }
    }

    private fun performLogin() {
        val email = binding?.loginEmailId?.text.toString().trim()
        val password = binding?.loginPasswordId?.text.toString().trim()

        if (TextUtils.isEmpty(email)) {
            binding?.loginEmailId?.error = "Email is required"
            return
        }
        if (!email.matches(Patterns.EMAIL_ADDRESS.toRegex())) {
            binding?.loginEmailId?.error = "Email is badly formatted"
            return
        }
        if (TextUtils.isEmpty(password)) {
            binding?.loginPasswordId?.error = "Password is required"
            return
        }

        val dialog = ProgressDialog(this@VolunteerLoginActivity)
        dialog.setMessage("Logging in")
        dialog.setCanceledOnTouchOutside(false)
        dialog.show()

        firebaseAuth?.signInWithEmailAndPassword(email, password)
            ?.addOnCompleteListener { task: Task<AuthResult?>? ->
                if (task?.isSuccessful == true) {
                    "Logged in".showShortToast(this@VolunteerLoginActivity)

                    Intent(this@VolunteerLoginActivity, VolunteerSideNavBarActivity::class.java).also {
                        startActivity(it)
                    }
                } else {
                    "Error ! ${task?.exception?.message}".showShortToast(this@VolunteerLoginActivity)
                }
                dialog.dismiss()
            }
    }
}