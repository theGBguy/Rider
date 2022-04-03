package com.example.rider.ui.nav_fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.rider.databinding.FragmentStudentRequestBinding
import com.example.rider.model.YatraRequest
import com.example.rider.ui.YatraRequestListAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class StudentRequestFragment : Fragment() {
    private var binding: FragmentStudentRequestBinding? = null

    private lateinit var adapter: YatraRequestListAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentStudentRequestBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val query = Firebase.firestore
            .collection("yatra_requests")
            .whereEqualTo("initiatorId", Firebase.auth.currentUser?.uid)

        val options = FirestoreRecyclerOptions.Builder<YatraRequest>()
            .setQuery(query, YatraRequest::class.java)
            .setLifecycleOwner(viewLifecycleOwner)
            .build()

        adapter = YatraRequestListAdapter(options)

        binding?.rvYatraRequests?.setHasFixedSize(true)
        binding?.rvYatraRequests?.layoutManager = LinearLayoutManager(context)
        binding?.rvYatraRequests?.adapter = adapter
    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }
}