package com.example.rider.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.rider.databinding.ActivityVolunteerRequestFragmentBinding

class VolunteerRequestFragment : Fragment() {
    private var binding: ActivityVolunteerRequestFragmentBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ActivityVolunteerRequestFragmentBinding.inflate(inflater, container, false)
        return binding!!.root
    }
}