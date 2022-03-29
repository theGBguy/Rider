package com.example.rider.ui.nav_fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.rider.databinding.FragmentVolunteerRequestBinding

class VolunteerRequestFragment : Fragment() {
    private var binding: FragmentVolunteerRequestBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentVolunteerRequestBinding.inflate(inflater, container, false)
        return binding!!.root
    }
}