package com.yatra.yatraapp.ui.nav_fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.yatra.yatraapp.databinding.FragmentVolunteerHomeBinding
import com.yatra.yatraapp.model.YatraRequest
import com.yatra.yatraapp.ui.YatraRequestListAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class VolunteerHomeFragment : Fragment() {
    private var binding: FragmentVolunteerHomeBinding? = null

    private lateinit var adapter: YatraRequestListAdapter
    private lateinit var query: Query
    private lateinit var options: FirestoreRecyclerOptions<YatraRequest>

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

        // only shows request which are not yet accepted or
        // are accepted by this volunteer account
        query = Firebase.firestore
            .collection("yatra_requests")
            .whereIn("acceptorId", mutableListOf("", Firebase.auth.currentUser?.uid))

        options = FirestoreRecyclerOptions.Builder<YatraRequest>()
            .setQuery(query, YatraRequest::class.java)
            .setLifecycleOwner(viewLifecycleOwner)
            .build()

        binding?.recyclerView?.setHasFixedSize(true)
        binding?.recyclerView?.layoutManager = LinearLayoutManager(context)
    }

    override fun onStart() {
        super.onStart()
        if (this::options.isInitialized) {
            adapter = YatraRequestListAdapter(options)
            binding?.recyclerView?.adapter = adapter
        }
    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }
}