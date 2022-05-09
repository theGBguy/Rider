package com.yatra.yatraapp.ui.nav_fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.yatra.yatraapp.databinding.FragmentDonateBinding

class DonateFragment : Fragment() {
    private var binding: FragmentDonateBinding? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDonateBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }
}