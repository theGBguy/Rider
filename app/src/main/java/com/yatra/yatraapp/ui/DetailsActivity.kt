package com.yatra.yatraapp.ui

import android.animation.ArgbEvaluator
import android.animation.FloatEvaluator
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
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
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.yatra.yatraapp.R
import com.yatra.yatraapp.databinding.ActivityDetailsBinding
import com.yatra.yatraapp.databinding.DialogUserProfileBinding
import com.yatra.yatraapp.model.User
import com.yatra.yatraapp.model.YatraRequest
import com.yatra.yatraapp.ui.nav_fragments.CurrentLocationFragment
import com.yatra.yatraapp.utils.showShortToast


class DetailsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailsBinding
    private var isStudent: Boolean? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        actionBar?.setDisplayHomeAsUpEnabled(true)
        actionBar?.title = "Yatra Request Details"

        val yatraRequest = intent.getParcelableExtra("key_data") as YatraRequest?

        binding.arrivalholder.text = yatraRequest?.arrivalLocation
        binding.departureholder.text = yatraRequest?.departureLocation
        binding.msgholder.text = yatraRequest?.msg
        binding.arrivaltimeholder.text = yatraRequest?.arrivalTime
        binding.arrivaldateholder.text = yatraRequest?.arrivalDate
        binding.deptimeholder.text = yatraRequest?.departureTime
        binding.depdateholder.text = yatraRequest?.departureDate
        binding.weightholder.text = "${yatraRequest?.weight.toString()}(Luggage Weight in kg)"
        binding.peopleholder.text = "${yatraRequest?.peopleCount.toString()}(Passenger count)"

        if (yatraRequest?.acceptorId?.isBlank() == true) {
            binding.btnSeeLiveLocation.visibility = View.GONE
            binding.btnMarkComplete.visibility = View.GONE
        } else {
            binding.backBtnId.visibility = View.GONE
            binding.acceptBtnId.visibility = View.GONE
        }
        if (yatraRequest?.completed == true) {
            binding.btnSeeLiveLocation.visibility = View.GONE
            binding.btnMarkComplete.visibility = View.GONE
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

        startBlinkEffect()

        binding.btnSeeLiveLocation.setOnClickListener {
            isStudent?.let { isStud ->
                CurrentLocationFragment.newInstance(
                    isStud,
                    (if (isStud) yatraRequest?.acceptorId else yatraRequest?.initiatorId)!!,
                    yatraRequest?.arrivalLatitude!!,
                    yatraRequest.arrivalLongitude!!,
                    yatraRequest.departureLatitude!!,
                    yatraRequest.departureLongitude!!,
                ).show(supportFragmentManager, "live_location")
            }
        }

        binding.btnMarkComplete.setOnClickListener {
            MaterialAlertDialogBuilder(this)
                .setTitle(R.string.app_name)
                .setIcon(R.mipmap.ic_launcher)
                .setMessage("Do you want to mark this Yatra Request as completed?")
                .setCancelable(false)
                .setPositiveButton("Yes") { _: DialogInterface?, _: Int ->
                    Firebase.firestore
                        .collection("yatra_requests")
                        .document(yatraRequest?.requestId!!)
                        .update("completed", true)
                        .addOnCompleteListener {
                            if (it.isSuccessful) {
                                "Yatra ride completed successfully!".showShortToast(this)
                                Intent(
                                    this@DetailsActivity,
                                    AcceptedRequestActivity::class.java
                                ).also { intent -> startActivity(intent) }
                                finish()
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
                                Intent(
                                    this@DetailsActivity,
                                    AcceptedRequestActivity::class.java
                                ).also { intent -> startActivity(intent) }
                                finish()
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

        Firebase.auth.currentUser?.uid?.let { uid ->
            Firebase.firestore.collection("users")
                .document(uid)
                .get()
                .continueWithTask { task ->
                    if (task.isSuccessful) {
                        val type = task.result["type"] as Long
                        if (type.toInt() == User.TYPE_STUDENT) {
                            isStudent = true
                            binding.backBtnId.visibility = View.GONE
                            binding.acceptBtnId.visibility = View.GONE
                            if (yatraRequest?.acceptorId?.isNotBlank() == true) {
                                Firebase.firestore.collection("users")
                                    .document(yatraRequest.acceptorId)
                                    .get()
                            } else {
                                binding.nameholder.visibility = View.GONE
                                null
                            }
                        } else {
                            isStudent = false
                            binding.btnMarkComplete.visibility = View.GONE
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
                        dialogBinding.tvAddress.text = user.address

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
                                    placeholder(R.drawable.yatra)
                                    fallback(R.drawable.icon_user)
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
                            SpannableStringBuilder("Yatra ${if (isStudent == true) "Acceptor" else "Requestor"} Name\n").append(
                                userName
                            ).append("↗")
                        binding.nameholder.movementMethod = LinkMovementMethod.getInstance()
                    } else {
                        binding.nameholder.text =
                            "Yatra ${if (isStudent == true) "Acceptor" else "Requestor"} Name : Unknown"
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

    private fun startBlinkEffect() {
        ObjectAnimator.ofInt(
            binding.btnSeeLiveLocation,
            "backgroundColor",
            resources.getColor(R.color.md_theme_light_primaryContainer),
            resources.getColor(R.color.md_theme_light_secondaryContainer),
            resources.getColor(R.color.md_theme_light_tertiaryContainer)
        ).apply {
            duration = 700
            setEvaluator(ArgbEvaluator())
            repeatMode = ValueAnimator.REVERSE
            repeatCount = Animation.INFINITE
            start()
        }

        ObjectAnimator.ofFloat(
            binding.btnSeeLiveLocation,
            "alpha",
            .5f, 1f
        ).apply {
            duration = 700
            setEvaluator(FloatEvaluator())
            repeatMode = ValueAnimator.REVERSE
            repeatCount = Animation.INFINITE
            start()
        }
    }
}