package com.dicoding.fauzan.sraya.ui.report

import android.Manifest
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.dicoding.fauzan.sraya.R
import com.dicoding.fauzan.sraya.ReportDialogFragment
import com.dicoding.fauzan.sraya.databinding.FragmentReportBinding
import com.dicoding.fauzan.sraya.uriToFile
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class ReportFragment : Fragment() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()) {

    }
    private val launcherGallery = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == RESULT_OK) {
            val selectedImage = it.data?.data as Uri
            val file = uriToFile(selectedImage, requireContext())

            binding.ivReportPhoto.setImageURI(selectedImage)
        }
    }
    private var _binding: FragmentReportBinding? = null
    private lateinit var database: FirebaseFirestore
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentReportBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        database = Firebase.firestore
        binding.btnReportUpload.setOnClickListener {
            val intent = Intent()
            intent.action = Intent.ACTION_GET_CONTENT
            intent.type = "image/*"
            val chooser = Intent.createChooser(intent, "Choose a picture")
            launcherGallery.launch(chooser)
        }
        binding.btnReportCreate.setOnClickListener {

            val report = hashMapOf(
                "Kronologi" to binding.etReportHow.text.toString(),
                "When" to binding.etReportWhen.text.toString(),
                "Where" to binding.etReportWhere.text.toString())
            val reportDialogFragment = ReportDialogFragment()
            val bundle = Bundle()
            bundle.putString(ReportDialogFragment.EXTRA_COMPLAINT, binding.etReportHow.text.toString())
            reportDialogFragment.arguments = bundle
            reportDialogFragment.show(childFragmentManager, ReportDialogFragment::class.java.simpleName)
            database.collection("SrayaData")
                .document("report")
                .set(report)
                .addOnSuccessListener {
                Log.d(TAG, "Successfully added a document!")
            }.addOnFailureListener {
                Log.w(TAG, "An error occured", it)
            }
        }
        binding.etReportHow.text.clear()
        binding.etReportWhere.text.clear()
        binding.etReportWhen.text.clear()
        binding.ivReportPhoto.setImageResource(R.drawable.ic_baseline_image_24)
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private val TAG = this::class.java.simpleName
    }


}