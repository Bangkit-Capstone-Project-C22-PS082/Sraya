package com.dicoding.fauzan.sraya

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SessionPreferences private constructor(val dataStore: DataStore<Preferences>) {

    fun getSession(): Flow<Session> {
        return dataStore.data.map {
            Session(
                it[USER_ID_KEY] ?: "",
                it[NAME_KEY] ?: "",
            )
        }
    }

    suspend fun login(session: Session) {
        dataStore.edit {
            it[USER_ID_KEY] = session.userId
            it[NAME_KEY] = session.name
        }
    }

    suspend fun logout() {
        dataStore.edit {
            it[USER_ID_KEY] = ""
            it[NAME_KEY] = ""
        }
    }
    companion object {
        @Volatile
        private var INSTANCE: SessionPreferences? = null

        private var USER_ID_KEY = stringPreferencesKey("user_id")
        private var NAME_KEY = stringPreferencesKey("name")

        fun getInstance(dataStore: DataStore<Preferences>): SessionPreferences {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: SessionPreferences(dataStore).also {
                    INSTANCE = it
                }
            }
        }
    }
}