package com.dicoding.fauzan.sraya

import android.os.Bundle

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.dicoding.fauzan.sraya.databinding.FragmentReportDialogBinding

class ReportDialogFragment : DialogFragment() {

    private var _binding: FragmentReportDialogBinding? = null

    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReportDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val bundle = arguments

        binding.btnReportDialogOk.setOnClickListener {
            dialog?.dismiss()
        }
        binding.tvReportDialogTime.setText("Waktu: ${Time.getTimeFormat()}")
        binding.tvReportDialogComplaint.setText("Keluhan: ${bundle?.getString(EXTRA_COMPLAINT)}")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    companion object {
        const val EXTRA_COMPLAINT = "extra_complaint"
    }
}