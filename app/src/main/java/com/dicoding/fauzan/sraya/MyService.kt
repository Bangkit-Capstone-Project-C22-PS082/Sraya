package com.dicoding.fauzan.sraya

import android.Manifest
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.dicoding.fauzan.sraya.ui.protection.ProtectionFragment
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.*
import java.util.concurrent.TimeUnit

class MyService : Service() {
    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private var locationCoordinates: LatLng? = null

    override fun onBind(p0: Intent?): IBinder? {
        throw UnsupportedOperationException("Not yet implemented")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(applicationContext)
        initLocationRequest()
        initLocationCallback()
        startLocationUpdates()
        serviceScope.launch {

            val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
                .setContentTitle("Service berjalan...")
                .setContentText("Lokasi: ${locationCoordinates?.latitude.toString()}, ${locationCoordinates?.longitude.toString()}")

                .setSmallIcon(R.drawable.ic_launcher_background)
                .build()
            val notificationManager = applicationContext
                .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val notificationChannel =
                    NotificationChannel(
                        CHANNEL_ID,
                        "Service notification",
                        NotificationManager.IMPORTANCE_HIGH
                    )
                notificationChannel.description = "Background processing"
                notificationManager.createNotificationChannel(notificationChannel)
            }
            notificationManager.notify(NOTIFICATION_ID, notification)
            Log.d(TAG, "Updating map")

        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceJob.cancel()
        Log.d(TAG, "Destroyed service")
        stopLocationUpdates()
    }

    fun sendPanic() {
        var sendPanicCurrentTimeMillis = 1000L
        var currentTimeMillis = System.currentTimeMillis()
        if (sendPanicCurrentTimeMillis + 1000L > currentTimeMillis) {
            sendPanicCurrentTimeMillis = currentTimeMillis

        }
    }

    private fun checkPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            applicationContext,
            permission
        ) == PackageManager.PERMISSION_GRANTED
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

        LocationServices.getSettingsClient(applicationContext)
            .checkLocationSettings(locationSettingsRequest).addOnSuccessListener {

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


            }
        }
    }

    private fun startLocationUpdates() {
        try {
            fusedLocationProviderClient.requestLocationUpdates(
                locationRequest, locationCallback, Looper.getMainLooper()
            )
        } catch (securityException: SecurityException) {
            Log.e(TAG, securityException.message.toString())
        }

    }

    private fun stopLocationUpdates() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
    }

    companion object {
        private val TAG = this::class.java.simpleName
        private val CHANNEL_ID = "channel_01"
        private const val NOTIFICATION_ID = 1
    }
}