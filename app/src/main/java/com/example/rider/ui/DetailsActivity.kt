package com.example.rider.ui

import android.content.DialogInterface
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.UnderlineSpan
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import coil.load
import coil.transform.CircleCropTransformation
import com.example.rider.R
import com.example.rider.databinding.ActivityDetailsBinding
import com.example.rider.databinding.DialogUserProfileBinding
import com.example.rider.model.User
import com.example.rider.model.YatraRequest
import com.example.rider.utils.showShortToast
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


class DetailsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivityDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val yatraRequest = intent.getParcelableExtra("key_data") as YatraRequest?

        binding.arrivalholder.text = yatraRequest?.arrivalLocation?.split(",")?.get(0)

        binding.departureholder.text = yatraRequest?.departureLocation?.split(",")?.get(0)

        binding.msgholder.text = yatraRequest?.msg
        binding.arrivaltimeholder.text = yatraRequest?.arrivalTime
        binding.arrivaldateholder.text = yatraRequest?.arrivalDate
        binding.deptimeholder.text = yatraRequest?.departureTime
        binding.depdateholder.text = yatraRequest?.departureDate
        binding.weightholder.text = "Luggage Weight : ${yatraRequest?.weight.toString()}"
        binding.peopleholder.text = "People count : ${yatraRequest?.peopleCount.toString()}"

        if (yatraRequest?.acceptorId != null) {
            binding.backBtnId.visibility = View.GONE
            binding.acceptBtnId.visibility = View.GONE
        }

        AnimationUtils.loadAnimation(this, R.anim.arrow_animation).also {
            binding.ivArrow.startAnimation(it)
            it.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(p0: Animation?) {
                }

                override fun onAnimationEnd(p0: Animation?) {
                    binding.ivArrow.startAnimation(it)
                }

                override fun onAnimationRepeat(p0: Animation?) {
                }

            })
        }

        binding.backBtnId.setOnClickListener {
            finish()
        }

        binding.acceptBtnId.setOnClickListener {
            MaterialAlertDialogBuilder(this)
                .setTitle(R.string.app_name)
                .setIcon(R.mipmap.ic_launcher)
                .setMessage("Do you want to Accept?")
                .setCancelable(false)
                .setPositiveButton("Yes") { _: DialogInterface?, _: Int ->
                    Firebase.firestore
                        .collection("yatra_requests")
                        .document(yatraRequest?.requestId!!)
                        .update("acceptorId", Firebase.auth.currentUser?.uid)
                        .addOnCompleteListener {
                            if (it.isSuccessful) {
                                "Yatra ride accepted successfully!".showShortToast(this)
                                val intent = Intent(
                                    this@DetailsActivity,
                                    AcceptedRequestActivity::class.java
                                )
                                startActivity(intent)
                            } else {
                                "Error updating status : ${it.exception?.message}".showShortToast(
                                    this
                                )
                            }
                        }
                }
                .setNegativeButton("No") { dialog: DialogInterface, _: Int -> dialog.cancel() }
                .show()
        }

        Firebase.auth.currentUser?.uid?.let {
            Firebase.firestore.collection("users")
                .document(it)
                .get()
                .continueWithTask { task ->
                    if (task.isSuccessful) {
                        val type = task.result["type"] as Long
                        if (type.toInt() == User.TYPE_STUDENT) {
                            binding.nameholder.visibility = View.GONE
                            binding.backBtnId.visibility = View.GONE
                            binding.acceptBtnId.visibility = View.GONE
                            null
                        } else {
                            yatraRequest?.initiatorId?.let { initiatorId ->
                                Firebase.firestore.collection("users")
                                    .document(initiatorId)
                                    .get()
                            }
                        }
                    } else {
                        null
                    }
                }
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val user = task.result.toObject(User::class.java) as User

                        val plainUserName = "${user.firstName} ${user.lastName}"

                        val dialogBinding =
                            DialogUserProfileBinding.inflate(layoutInflater)
                        dialogBinding.tvUserName.text = plainUserName
                        dialogBinding.tvPhone.text = user.phoneNumber
                        dialogBinding.tvAddress.text = user.address.split(",").get(0)

                        val clickableSpan: ClickableSpan = object : ClickableSpan() {
                            override fun onClick(textView: View) {
                                dialogBinding.root.parent?.let {
                                    (it as ViewGroup).removeView(dialogBinding.root)
                                }
                                MaterialAlertDialogBuilder(this@DetailsActivity)
                                    .setView(dialogBinding.root)
                                    .show()
                                dialogBinding.ivUserProfilePic.load(user.profileImageLocation) {
                                    crossfade(true)
                                    placeholder(R.drawable.placeholder)
                                    fallback(R.drawable.icon_username)
                                    transformations(CircleCropTransformation())
                                }
                            }

                            @RequiresApi(Build.VERSION_CODES.M)
                            override fun updateDrawState(textPaint: TextPaint) {
                                super.updateDrawState(textPaint)
                                textPaint.color = getColor(R.color.md_theme_light_primary)
                                textPaint.isFakeBoldText = true
                            }
                        }

                        val userName = SpannableString(plainUserName)
                        userName.setSpan(clickableSpan, 0, plainUserName.length, 0)
                        userName.setSpan(UnderlineSpan(), 0, plainUserName.length, 0)

                        binding.nameholder.text =
                            SpannableStringBuilder("Requester name : ").append(userName).append("↗")
                        binding.nameholder.movementMethod = LinkMovementMethod.getInstance()
                    } else {
                        binding.nameholder.text = "Requester name : Unknown"
                        if (task.exception?.message?.lowercase()
                                ?.contains("continuation") != true
                        ) {
                            "Error fetching requestor's information : ${task.exception?.message}".showShortToast(
                                this
                            )
                        }

                    }
                }
        }
    }
}