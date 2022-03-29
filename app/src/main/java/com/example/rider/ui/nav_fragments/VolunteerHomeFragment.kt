package com.example.rider.ui.nav_fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.rider.databinding.FragmentVolunteerHomeBinding
import com.example.rider.model.YatraRequest
import com.example.rider.ui.YatraRequestListAdapter
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot

class VolunteerHomeFragment : Fragment() {
    private var binding: FragmentVolunteerHomeBinding? = null

    private var recyclerView: RecyclerView? = null
    private var yatraRequestListFormAdapter: YatraRequestListAdapter? = null
    private var yatraRequestList: ArrayList<YatraRequest?>? = null
    private var db: FirebaseFirestore? = null

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

        db = FirebaseFirestore.getInstance()

        binding?.recyclerView?.setHasFixedSize(true)
        binding?.recyclerView?.layoutManager = LinearLayoutManager(context)

        yatraRequestList = ArrayList()
        yatraRequestListFormAdapter = YatraRequestListAdapter(yatraRequestList)

        recyclerView?.adapter = yatraRequestListFormAdapter

        setupEventChangeListener()
    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }

    private fun setupEventChangeListener() {
        db?.collection("students_form")
            ?.addSnapshotListener { value: QuerySnapshot?, error: FirebaseFirestoreException? ->
                if (error != null) {
                    error.message?.let { Log.e("arrival error", it) }
                    return@addSnapshotListener
                }
                for (dc in value?.documentChanges!!) {
                    if (dc.type == DocumentChange.Type.ADDED) {
                        yatraRequestList?.add(dc.document.toObject(YatraRequest::class.java))
                    }
                    yatraRequestListFormAdapter?.notifyDataSetChanged()
                }
            }
    }
}