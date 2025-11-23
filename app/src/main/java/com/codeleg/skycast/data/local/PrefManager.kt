package com.codeleg.skycast.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object PrefManager {
    private const val PREFS_NAME = "skycast_prefs"

    private const val KEY_IS_LOCATION_SET = "is_location_set"
    private const val KEY_LATITUDE = "location_latitude_bits"
    private const val KEY_LONGITUDE = "location_longitude_bits"

    private fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /**
     * Suspend-friendly
     * check whether a location is stored.
     * Uses Dispatchers.IO to avoid blocking the main thread.
     */
    suspend fun isLocationSet(context: Context): Boolean = withContext(Dispatchers.IO) {
        prefs(context).getBoolean(KEY_IS_LOCATION_SET, false)
    }

    /**
     * Suspend-friendly one-shot read of the stored location.
     * Returns Pair(lat, lon) or null if not set.
     */
    suspend fun getLocation(context: Context): Pair<Double, Double>? = withContext(Dispatchers.IO) {
        val sp = prefs(context)
        val isSet = sp.getBoolean(KEY_IS_LOCATION_SET, false)
        if (!isSet) return@withContext null
        val latBits = sp.getLong(KEY_LATITUDE, Long.MIN_VALUE)
        val lonBits = sp.getLong(KEY_LONGITUDE, Long.MIN_VALUE)
        if (latBits == Long.MIN_VALUE || lonBits == Long.MIN_VALUE) return@withContext null
        return@withContext Pair(Double.fromBits(latBits), Double.fromBits(lonBits))
    }

    /**
     * Suspend-friendly save of a location (atomic within SharedPreferences.edit transaction).
     */
    suspend fun setLocation(context: Context, latitude: Double, longitude: Double) = withContext(Dispatchers.IO) {
        prefs(context).edit(commit = false) {
            putLong(KEY_LATITUDE, latitude.toBits())
            putLong(KEY_LONGITUDE, longitude.toBits())
            putBoolean(KEY_IS_LOCATION_SET, true)
        }
    }

    /**
     * Suspend-friendly clear of stored location.
     */
    suspend fun clearLocation(context: Context) = withContext(Dispatchers.IO) {
        prefs(context).edit(commit = false) {
            remove(KEY_LATITUDE)
            remove(KEY_LONGITUDE)
            putBoolean(KEY_IS_LOCATION_SET, false)
        }
    }
}