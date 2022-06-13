package com.dicoding.fauzan.sraya.ui.protection

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.dicoding.fauzan.sraya.SessionPreferences

class ProtectionViewModel(val protectionRepository: ProtectionRepository) : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is dashboard Fragment"
    }
    val text: LiveData<String> = _text

    fun sendPanic() {
        var sendPanicCurrentTimeMillis = 1000L
        var currentTimeMillis = System.currentTimeMillis()
        if (sendPanicCurrentTimeMillis + 1000L > currentTimeMillis) {
            sendPanicCurrentTimeMillis = currentTimeMillis

        }
    }
}