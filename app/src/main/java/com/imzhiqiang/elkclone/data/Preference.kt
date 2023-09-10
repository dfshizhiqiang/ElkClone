package com.imzhiqiang.elkclone.data

import android.content.Context
import android.icu.util.Currency
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

object Preference {

    private val Context.preferenceDataStore: DataStore<Preferences> by preferencesDataStore(name = "preference")

    private val SourceCurrency = stringPreferencesKey("source_currency")
    private val TargetCurrency = stringPreferencesKey("target_currency")

    fun getSourceCurrencyFlow(context: Context): Flow<String> {
        return context.preferenceDataStore.data
            .map { preferences ->
                preferences[SourceCurrency] ?: "USD"
            }
    }

    fun getTargetCurrencyFlow(context: Context): Flow<String> {
        val locale = context.resources.configuration.locales[0]
        return context.preferenceDataStore.data
            .map { preferences ->
                preferences[TargetCurrency] ?: Currency.getInstance(locale).currencyCode
            }
    }

    suspend fun setSourceCurrency(context: Context, source: String) {
        context.preferenceDataStore.edit { preferences ->
            preferences[SourceCurrency] = source
        }
    }

    suspend fun setTargetCurrency(context: Context, target: String) {
        context.preferenceDataStore.edit { preferences ->
            preferences[TargetCurrency] = target
        }
    }

    suspend fun setSourceAndTargetCurrency(context: Context, source: String, target: String) {
        context.preferenceDataStore.edit { preferences ->
            preferences[SourceCurrency] = source
            preferences[TargetCurrency] = target
        }
    }
}

