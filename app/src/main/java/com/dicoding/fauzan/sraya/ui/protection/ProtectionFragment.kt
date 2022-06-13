package com.dicoding.fauzan.sraya.ui.protection

import android.Manifest
import android.app.Activity.RESULT_CANCELED
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.work.*
import com.dicoding.fauzan.sraya.MyService
import com.dicoding.fauzan.sraya.MyWorker
import com.dicoding.fauzan.sraya.R
import com.dicoding.fauzan.sraya.databinding.FragmentProtectionBinding
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.*
import java.util.concurrent.TimeUnit

class ProtectionFragment : Fragment(), OnMapReadyCallback {

    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("session")
    private lateinit var workManger: WorkManager
    private lateinit var periodicWorkRequest: PeriodicWorkRequest
    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private var isReady = false
    private var _binding: FragmentProtectionBinding? = null
    private lateinit var database: FirebaseFirestore
    private lateinit var locationCoordinates: LatLng
    private lateinit var viewModel: ProtectionViewModel
    private var isRunning = false
    private var requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()) {
        when {
            it[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false -> {
                getInitialLocation()
            }
            it[Manifest.permission.ACCESS_FINE_LOCATION] ?: false -> {
                getInitialLocation()
            }
            else -> {

            }
        }
    }

    private var resolutionLauncher = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()) {
        when (it.resultCode) {
            RESULT_OK -> {
                Log.i(TAG, "Izin lokasi telah terpenuhi")
            }
            RESULT_CANCELED -> {
                Toast.makeText(this.context, "Butuh izin lokasi", Toast.LENGTH_SHORT).show()
            }
        }
    }
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentProtectionBinding.inflate(inflater, container, false)
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        val mapFragment = childFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)



        /*
        viewModel = ViewModelProvider(
            this,
            ProtectionViewModelFactory.getInstance(
                dataStore))[ProtectionViewModel::class.java]


         */
        database = Firebase.firestore


        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnProtectionProtect.setOnClickListener {
            var locationName = ""
            if (Geocoder.isPresent()) {
                val geocoder = Geocoder(requireActivity())
                    .getFromLocation(locationCoordinates.latitude, locationCoordinates.longitude, 5)
                val address = geocoder.first()
                locationName = "${address.thoroughfare} ${address.subThoroughfare}, " +
                        "${address.subLocality}, ${address.subAdminArea}, ${address.adminArea}" +
                        " ${address.postalCode}"
                Log.d(TAG, "Admin Area: ${address.adminArea}")
                Log.d(TAG, "Country name: ${address.countryName}")
                Log.d(TAG, "Feature name: ${address.featureName}")
                Log.d(TAG, "Country code: ${address.countryCode}")
                Log.d(TAG, "Locality: ${address.locality}")
                Log.d(TAG, "Postal code: ${address.postalCode}")
                Log.d(TAG, "Premises: ${address.premises}")
                Log.d(TAG, "Sub admin area: ${address.subAdminArea}")
                Log.d(TAG, "Sub locality: ${address.subLocality}")
                Log.d(TAG, "Sub thorugh fare: ${address.subThoroughfare}")
                Log.d(TAG, "Through fare: ${address.thoroughfare}")
            }
            Log.d(TAG, locationName)
            val geoPoint = GeoPoint(locationCoordinates.latitude, locationCoordinates.longitude)
            val location = hashMapOf(
                "GPS" to geoPoint,
                "Location" to locationName,
                "Time" to Timestamp(Date()))
            val protection = hashMapOf(
                "Geolokasi" to geoPoint,
                "NIK" to "0",
                "Nama" to "0"
            )
            database.collection("SrayaData")
                .document("location")
                .set(location)
                .addOnSuccessListener {
                    Log.d(TAG, "Successfully added a document!")
                }
                .addOnFailureListener {
                    Log.w(TAG, "An error occured", it)
                }
            database.collection("ReportProtection")
                .add(protection)
                .addOnSuccessListener {
                    Log.d(TAG, "Successfully added a document!")
                }
                .addOnFailureListener {
                    Log.w(TAG, "An error occured", it)
                }
            Toast.makeText(
                requireActivity(),
                "Panic button telah ditekan. Lokasi telah dikirim",
                Toast.LENGTH_SHORT).show()
        }
        binding.btnProtectionToggle.setOnClickListener {
            val serviceIntent = Intent(requireActivity(), MyService::class.java)
            if (!isRunning) {
                binding.btnProtectionToggle.setText("Off")
                view.context.startService(serviceIntent)
                isRunning = true
            } else {
                binding.btnProtectionToggle.setText("On")
                view.context.stopService(serviceIntent)
                isRunning = false
            }
        }
    }



    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        /**
         * Manipulates the map once available.
         * This callback is triggered when the map is ready to be used.
         * This is where we can add markers or lines, add listeners or move the camera.
         * In this case, we just add a marker near Sydney, Australia.
         * If Google Play services is not installed on the device, the user will be prompted to
         * install it inside the SupportMapFragment. This method will only be triggered once the
         * user has installed Google Play services and returned to the app.
         */


        initLocationRequest()

        initLocationCallback()

        startLocationUpdates()
        isReady = true
    }

    override fun onResume() {
        super.onResume()
        if (isReady) {
            startLocationUpdates()
        }
    }

    override fun onPause() {
        super.onPause()
        if (isReady) {
            stopLocationUpdates()
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null

    }
    private fun checkPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(requireContext(), permission) == PackageManager.PERMISSION_GRANTED
    }
    private fun getInitialLocation() {
        if (checkPermission(Manifest.permission.ACCESS_COARSE_LOCATION) &&
            checkPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {

        } else {
            requestPermissionLauncher.launch(arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION))
        }
    }
    private fun initLocationRequest() {
        locationRequest = LocationRequest.create().apply {
            interval = TimeUnit.SECONDS.toMillis(1)
            maxWaitTime = TimeUnit.SECONDS.toMillis(1)
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        val locationSettingsRequest = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
            .build()

        LocationServices.getSettingsClient(requireActivity())
            .checkLocationSettings(locationSettingsRequest).addOnSuccessListener {

            }
            .addOnFailureListener {
                try {
                    if (it is ResolvableApiException) {
                        resolutionLauncher.launch(
                            IntentSenderRequest.Builder(it.resolution).build()
                        )
                    }
                } catch (exception: IntentSender.SendIntentException) {
                    Toast.makeText(this.context, exception.message, Toast.LENGTH_SHORT).show()
                }
            }
    }
    private fun initLocationCallback() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val lastLocation = locationResult.lastLocation
                val latLng = LatLng(lastLocation.latitude, lastLocation.longitude)
                locationCoordinates = latLng
                Log.d(TAG, latLng.latitude.toString())
                Log.d(TAG, latLng.longitude.toString())

                mMap.addMarker(MarkerOptions()
                    .position(latLng)
                    .title("You're here"))
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
            }
        }
    }

    private fun startLocationUpdates() {
        if (checkPermission(Manifest.permission.ACCESS_COARSE_LOCATION) &&
            checkPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
            fusedLocationProviderClient.requestLocationUpdates(
                locationRequest, locationCallback, Looper.getMainLooper()
            )
        } else {
            requestPermissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION))
        }

    }

    private fun stopLocationUpdates() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
    }




    companion object {
        private const val TAG = "ProtectionFragment"
    }
}