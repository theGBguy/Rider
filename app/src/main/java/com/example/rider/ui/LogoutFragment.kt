package com.example.rider.ui

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.rider.R
import com.example.rider.databinding.ActivityLogoutFragmentBinding
import com.example.rider.utils.showShortToast
import com.google.firebase.auth.FirebaseAuth

class LogoutFragment : Fragment(), View.OnClickListener {
    private var binding: ActivityLogoutFragmentBinding? = null
    private var fAuth: FirebaseAuth? = null
    private var dialog: ProgressDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ActivityLogoutFragmentBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding!!.yesBtnId.setOnClickListener(this)
        binding!!.noBtnId.setOnClickListener(this)
    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }

    fun logOut(view: View?) {
        fAuth!!.signOut()
    }

    override fun onClick(view: View) {
        val id = view.id
        if (id == R.id.yesBtn_id) {
            "Logged Out".showShortToast(requireContext())

            fAuth!!.signOut()

            val i = Intent(activity, UserOptionsActivity::class.java)
            i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(i)
        } else if (id == R.id.noBtn_id) {
            "Good Decision".showShortToast(requireContext())

            val intent = Intent(requireContext(), StudentSideNavBarActivity::class.java)
            startActivity(intent)
        }
    }
}