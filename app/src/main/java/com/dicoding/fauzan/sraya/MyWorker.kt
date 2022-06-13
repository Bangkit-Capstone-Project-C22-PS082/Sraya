package com.dicoding.fauzan.sraya

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import okhttp3.OkHttp

class MyWorker(context: Context, workerParameters: WorkerParameters ) : Worker(context, workerParameters) {
    override fun doWork(): Result {
        return Result.retry()
    }

    private fun sendLocation() {

    }

    private fun showNotification() {
        val notificationManager = applicationContext
            .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationCompat = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setContentTitle("")
            .setContentText("")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel =
                NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH)

        }
        notificationManager.notify(NOTIFICATION_ID, notificationCompat)
    }
    companion object {
        private val TAG = MyWorker::class.java.simpleName
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "channel_1"
        private const val CHANNEL_NAME = ""
    }

}