package com.example.rider.ui.nav_fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.rider.databinding.FragmentVolunteerRequestBinding
import com.example.rider.model.YatraRequest
import com.example.rider.ui.YatraRequestListAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class VolunteerRequestFragment : Fragment() {
    private var binding: FragmentVolunteerRequestBinding? = null

    private lateinit var adapter: YatraRequestListAdapter
    private lateinit var query: Query
    private lateinit var options: FirestoreRecyclerOptions<YatraRequest>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentVolunteerRequestBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        query = Firebase.firestore
            .collection("yatra_requests")
            .whereEqualTo("acceptorId", Firebase.auth.currentUser?.uid)
        options = FirestoreRecyclerOptions.Builder<YatraRequest>()
            .setQuery(query, YatraRequest::class.java)
            .setLifecycleOwner(viewLifecycleOwner)
            .build()

        binding?.rvYatraRequests?.setHasFixedSize(true)
        binding?.rvYatraRequests?.layoutManager = LinearLayoutManager(context)
    }

    override fun onStart() {
        super.onStart()
        if (this::options.isInitialized) {
            adapter = YatraRequestListAdapter(options)
            binding?.rvYatraRequests?.adapter = adapter
        }
    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }
}