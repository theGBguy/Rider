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
import com.example.rider.databinding.ActivityStudentLoginBinding
import com.example.rider.model.User
import com.example.rider.utils.showShortToast
import com.google.android.gms.tasks.Task
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


class StudentLoginActivity : AppCompatActivity() {
    private var binding: ActivityStudentLoginBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityStudentLoginBinding.inflate(layoutInflater)
        setContentView(binding!!.root)

        binding?.loginRegisterTextViewId?.setOnClickListener {
            Intent(this@StudentLoginActivity, RegisterActivity::class.java).also {
                startActivity(it)
            }
        }

        binding?.loginSForgetpasswordId?.setOnClickListener {
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
                .setView(linearLayout)
                .setPositiveButton("Yes") { _: DialogInterface?, _: Int ->
                    val mail = resetMail.text.toString()
                    if (mail.matches(Patterns.EMAIL_ADDRESS.toRegex())) {
                        Firebase.auth.sendPasswordResetEmail(mail)
                            .addOnSuccessListener {
                                "Reset Link has been sent in your mail".showShortToast(this@StudentLoginActivity)
                            }
                            .addOnFailureListener { e: Exception ->
                                "Link is not sent : ${e.message}".showShortToast(this@StudentLoginActivity)
                            }
                    } else {
                        "Please add valid email address".showShortToast(this@StudentLoginActivity)
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

        val dialog = ProgressDialog(this@StudentLoginActivity)
        dialog.setMessage("Logging in...")
        dialog.setCanceledOnTouchOutside(false)
        dialog.show()

        // sign in and verify the current account as student
        val signInTask = Firebase.auth.signInWithEmailAndPassword(email, password)

        signInTask.continueWithTask {
            if (it.isSuccessful) {
                Firebase.auth.currentUser?.uid?.let { uid ->
                    Firebase.firestore.collection("users")
                        .document(uid)
                        .get()
                }
            } else {
                "Error ! ${it.exception?.message}".showShortToast(this@StudentLoginActivity)
                null
            }
        }.addOnCompleteListener(this) { task: Task<DocumentSnapshot> ->
            if (task.isSuccessful) {
                val user = task.result.toObject(User::class.java) as User
                if (user.type == User.TYPE_STUDENT) {
                    "Logged in".showShortToast(this@StudentLoginActivity)
                    Intent(this@StudentLoginActivity, StudentSideNavBarActivity::class.java).also {
                        it.putExtra("key_args", Bundle().apply {
                            putParcelable("key_user", user)
                        })
                        startActivity(it)
                    }
                } else {
                    "This is not an student account".showShortToast(this@StudentLoginActivity)
                }
            } else {
                if (task.exception?.message?.lowercase()?.contains("continuation") != true) {
                    "Error ! ${task.exception?.message}".showShortToast(this@StudentLoginActivity)
                }
            }
            dialog.dismiss()
        }
    }
}