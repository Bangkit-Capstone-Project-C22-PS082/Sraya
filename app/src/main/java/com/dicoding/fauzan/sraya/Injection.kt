package com.dicoding.fauzan.sraya

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.dicoding.fauzan.sraya.ui.protection.ProtectionRepository

object Injection {
    fun provideProtectionRepository(dataStore: DataStore<Preferences>): ProtectionRepository{
        val sessionPreferences = SessionPreferences.getInstance(dataStore)
        return ProtectionRepository(sessionPreferences)
    }

    fun provideLoginRepository(dataStore: DataStore<Preferences>, context: Context): LoginRepository {
        val sessionPreferences = SessionPreferences.getInstance(dataStore)
        return LoginRepository(sessionPreferences, context)
    }
}