package com.example.rider.ui

import android.app.DatePickerDialog
import android.app.ProgressDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TimePicker
import androidx.fragment.app.Fragment
import com.example.rider.databinding.ActivityStudentFragmentHomeBinding
import com.example.rider.utils.showShortToast
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class StudentHomeFragment : Fragment() {
    private var binding: ActivityStudentFragmentHomeBinding? = null

    var studentDbRef: DatabaseReference? = null
    var firestore: FirebaseFirestore? = null

    var dialog: ProgressDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ActivityStudentFragmentHomeBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val people = mutableListOf(
            "Number of People",
            "1", "2", "3", "4",
            "5", "6", "7", "8",
            "9", "10"
        )
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, people)
        binding?.peopleSpinnerId?.adapter = adapter

        firestore = FirebaseFirestore.getInstance()
        studentDbRef = FirebaseDatabase.getInstance().reference.child("StudentForm")

        binding?.pickDateBtnId?.setOnClickListener { _ ->
            with(Calendar.getInstance()) {
                DatePickerDialog(
                    requireActivity(),
                    { _, year: Int, month: Int, day: Int ->
                        val date = "$day/${month + 1}/$year"
                        binding?.etDate?.setText(date)
                    },
                    this[Calendar.YEAR], this[Calendar.MONTH], this[Calendar.DAY_OF_MONTH]
                ).show()
            }
        }

        binding?.pickDateBtnId2?.setOnClickListener { _ ->
            with(Calendar.getInstance()) {
                DatePickerDialog(
                    requireActivity(),
                    { _, year: Int, month: Int, day: Int ->
                        val date = "$day/${month + 1}/$year"
                        binding?.etDate2?.setText(date)
                    },
                    this[Calendar.YEAR], this[Calendar.MONTH], this[Calendar.DAY_OF_MONTH]
                ).show()
            }
        }

        binding?.pickTimeBtnId?.setOnClickListener { _ ->
            TimePickerDialog(
                context,
                { _: TimePicker?, hourOfDay: Int, minute: Int ->
                    val calendar = Calendar.getInstance()
                    calendar[0, 0, 0, hourOfDay] = minute
                    val time = "$hourOfDay-$minute"
                    binding?.etTime?.setText(time)
                }, 12, 0, false
            ).show()
        }

        binding?.pickTimeBtnId2?.setOnClickListener { _ ->
            TimePickerDialog(
                context,
                { _: TimePicker?, hourOfDay: Int, minute: Int ->
                    val calendar = Calendar.getInstance()
                    calendar[0, 0, 0, hourOfDay] = minute
                    val time = "$hourOfDay-$minute"
                    binding?.etTime2?.setText(time)
                }, 12, 0, false
            ).show()
        }

        binding?.studentSubmitBtnId?.setOnClickListener { _ ->
            val departure = binding?.departureLocationId?.text.toString()
            val arrival = binding?.arrivalLocationId?.text.toString()
            val departureDate = binding?.etDate?.text.toString()
            val arrivalDate = binding?.etDate2?.text.toString()
            val departureTime = binding?.etTime?.text.toString()
            val arrivalTime = binding?.etTime2?.text.toString()
            val weight = binding?.etWeight?.text.toString()
            val msg = binding?.etMsg?.text.toString()
            val name = binding?.etName?.text.toString()

            val people1 = binding?.peopleSpinnerId?.selectedItem.toString()

            if (TextUtils.isEmpty(departureDate)) {
                binding?.etDate?.error = "Select Departure Date"
                return@setOnClickListener
            }
            if (TextUtils.isEmpty(departureTime)) {
                binding?.etTime?.error = "Select Departure Time"
                return@setOnClickListener
            }
            if (TextUtils.isEmpty(departure)) {
                binding?.departureLocationId?.error = "It cannot be left empty"
                return@setOnClickListener
            }
            if (TextUtils.isEmpty(arrival)) {
                binding?.arrivalLocationId?.error = "It cannot be left empty"
                return@setOnClickListener
            }
            if (TextUtils.isEmpty(arrivalDate)) {
                binding?.etDate2?.error = "Select Arrival Date"
                return@setOnClickListener
            }
            if (TextUtils.isEmpty(arrivalTime)) {
                binding?.etTime?.error = "Select Arrival Time"
                return@setOnClickListener
            }
            if (TextUtils.isEmpty(msg)) {
                binding?.etMsg?.error = "Write Special Message"
                return@setOnClickListener
            }
            if (TextUtils.isEmpty(weight)) {
                binding?.etWeight?.error = "Input Luggage Weight"
                return@setOnClickListener
            }
            if (TextUtils.isEmpty(name)) {
                binding?.etName?.error = "Enter your Full Name"
                return@setOnClickListener
            }

            insertStudentData()

            dialog = ProgressDialog(context)
            dialog!!.setMessage("Sending Request")
            dialog!!.show()

            Intent(context, SubmitSuccessActivity::class.java).also {
                startActivity(it)
            }
        }
    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }

    private fun insertStudentData() {
        val items: MutableMap<String, String> = HashMap()

        items["Departure"] = binding?.departureLocationId?.text.toString().trim()
        items["Arrival"] = binding?.arrivalLocationId?.text.toString().trim()
        items["DepTime"] = binding?.etTime?.text.toString().trim()
        items["ArrTime"] = binding?.etTime2?.text.toString().trim()
        items["DepDate"] = binding?.etDate?.text.toString().trim()
        items["ArrDate"] = binding?.etDate2?.text.toString().trim()
        items["Msg"] = binding?.etMsg?.text.toString().trim()
        items["Weight"] = binding?.etWeight?.text.toString().trim()
        items["Name"] = binding?.etName?.text.toString().trim()
        items["People"] = binding?.peopleSpinnerId?.selectedItem.toString().trim()

        firestore!!.collection("students_form")
            .add(items)
            .addOnCompleteListener {
                dialog!!.dismiss()

                val id = studentDbRef!!.key
                binding?.departureLocationId?.setText("")
                binding?.arrivalLocationId?.setText("")
                binding?.etDate?.setText("")
                binding?.etDate2?.setText("")
                binding?.etTime?.setText("")
                binding?.etTime2?.setText("")
                binding?.etWeight?.setText("")
                binding?.etMsg?.setText("")
                binding?.etName?.setText("")

                "Request Sent".showShortToast(requireContext())
            }
    }
}