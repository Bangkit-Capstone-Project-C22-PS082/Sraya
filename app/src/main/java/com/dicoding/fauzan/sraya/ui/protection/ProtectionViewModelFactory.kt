package com.dicoding.fauzan.sraya.ui.protection

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.dicoding.fauzan.sraya.Injection
import com.dicoding.fauzan.sraya.SessionPreferences

class ProtectionViewModelFactory(private val protectionRepository: ProtectionRepository) :
    ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProtectionViewModel::class.java)) {
            return ProtectionViewModel(protectionRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
    companion object {
        @Volatile
        private var INSTANCE: ProtectionViewModelFactory? = null
        fun getInstance(dataStore: DataStore<Preferences>): ProtectionViewModelFactory {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ProtectionViewModelFactory(Injection.provideProtectionRepository(dataStore))
            }
        }
    }
}