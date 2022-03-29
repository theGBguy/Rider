package com.example.rider.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.rider.databinding.ActivityVolunteerHomeFragmentBinding
import com.example.rider.model.StudentForm
import com.google.firebase.database.DatabaseReference
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot

class VolunteerHomeFragment : Fragment() {
    private var binding: ActivityVolunteerHomeFragmentBinding? = null

    var recyclerView: RecyclerView? = null
    var studentFormAdapter: StudentAdapter? = null
    var studentFormList: ArrayList<StudentForm?>? = null
    var database: DatabaseReference? = null
    var db: FirebaseFirestore? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ActivityVolunteerHomeFragmentBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = FirebaseFirestore.getInstance()

        binding?.recyclerView?.setHasFixedSize(true)
        binding?.recyclerView?.layoutManager = LinearLayoutManager(context)

        studentFormList = ArrayList()
        studentFormAdapter = StudentAdapter(studentFormList)

        recyclerView?.adapter = studentFormAdapter

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
                        studentFormList?.add(dc.document.toObject(StudentForm::class.java))
                    }
                    studentFormAdapter?.notifyDataSetChanged()
                }
            }
    }
}