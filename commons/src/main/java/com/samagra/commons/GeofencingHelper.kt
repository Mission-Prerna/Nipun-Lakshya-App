package com.samagra.commons

import android.Manifest
import android.app.Activity
import android.content.Context
import android.location.Location
import android.os.Build
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import com.google.gson.Gson
import com.samagra.commons.models.geofencing.GeofencingConfigModel
import com.samagra.commons.models.schoolsresponsedata.SchoolsData
import com.samagra.commons.utils.RemoteConfigUtils
import timber.log.Timber

object GeofencingHelper {

    private const val LOCATION_REQUEST_INTERVAL_MS: Long = 10000

    fun compareUserLocationWithGivenRange(
        userLatitude: Double,
        userLongitude: Double,
        schoolData: SchoolsData,
        geofencingRadius: Int?,
        listener: SetMatchLocationListener
    ) {
        val targetLatitude = schoolData.schoolLat
        val targetLongitude = schoolData.schoolLong
        val givenRange: Int = geofencingRadius ?: 0
        Timber.d("School lat and long! $targetLatitude $targetLongitude")
        if (targetLatitude == null || targetLongitude == null) {
            listener.onLocationLatLongNull()
            return
        }
        val distance =
            calculateDistance(
                userLatitude,
                userLongitude,
                targetLatitude ?: 0.0,
                targetLongitude ?: 0.0
            )
        if (distance <= givenRange) {
            listener.onLocationRangeMatched()
        } else {
            listener.onLocationOutOfRange()
        }
    }

    interface SetMatchLocationListener {
        fun onLocationRangeMatched()
        fun onLocationOutOfRange()

        //Allow assessment if values are null
        fun onLocationLatLongNull()
    }

    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Float {
        val results = FloatArray(1)
        Location.distanceBetween(lat1, lon1, lat2, lon2, results)
        return results[0]
    }

    fun getFusedLocationProviderClient(context: Context): FusedLocationProviderClient {
        return LocationServices.getFusedLocationProviderClient(context)
    }

    fun getLocationRequest(): LocationRequest {
        return LocationRequest.create().apply {
            interval = LOCATION_REQUEST_INTERVAL_MS
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
    }

    fun createLocationRequest(): LocationRequest {
        return LocationRequest.create().apply {
            interval = 10000
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
    }

    fun parseGeofencingConfig(): GeofencingConfigModel? {
        return Gson().fromJson(
            RemoteConfigUtils.getFirebaseRemoteConfigInstance()
                .getString(RemoteConfigUtils.GEOFENCING_CONFIG),
            GeofencingConfigModel::class.java
        )
    }

    fun checkShowRequestPermissionRationale(
        activity: Activity,
        launchGeofence: () -> Unit,
        launchSettings: () -> Unit
    ) {
        if (checkPermissionRational(activity)) {
            launchGeofence()
        } else {
            launchSettings()
        }
    }

    private fun checkPermissionRational(activity: Activity): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            !ActivityCompat.shouldShowRequestPermissionRationale(
                activity,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        } else {
            true
        }
    }

    fun changeLocationSettings(
        activity: Activity,
        success: (LocationSettingsResponse) -> Unit,
        failure: (Exception) -> Unit
    ) {
        val builder = LocationSettingsRequest.Builder()
        builder.addLocationRequest(createLocationRequest())
        val client: SettingsClient = LocationServices.getSettingsClient(activity)
        val task = client.checkLocationSettings(builder.build())

        task.addOnSuccessListener { locationSettingsResponse: LocationSettingsResponse ->
            success(locationSettingsResponse)
        }
        task.addOnFailureListener { exception: Exception ->
            failure(exception)
        }
    }
}