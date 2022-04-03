package com.example.rider.ui.nav_fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.rider.databinding.FragmentVolunteerHomeBinding
import com.example.rider.model.YatraRequest
import com.example.rider.ui.YatraRequestListAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class VolunteerHomeFragment : Fragment() {
    private var binding: FragmentVolunteerHomeBinding? = null
    private lateinit var adapter: YatraRequestListAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentVolunteerHomeBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val query = Firebase.firestore
            .collection("yatra_requests")
            .orderBy("departureDate")

        val options = FirestoreRecyclerOptions.Builder<YatraRequest>()
            .setQuery(query, YatraRequest::class.java)
            .setLifecycleOwner(viewLifecycleOwner)
            .build()

        adapter = YatraRequestListAdapter(options)

        binding?.recyclerView?.setHasFixedSize(true)
        binding?.recyclerView?.layoutManager = LinearLayoutManager(context)
        binding?.recyclerView?.adapter = adapter
    }

    override fun onDestroyView() {
        adapter.stopListening()
        binding = null
        super.onDestroyView()
    }
}