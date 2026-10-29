package fr.vpm.changingtables.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume

class UserLocationProvider {

    companion object {
        private const val TAG = "UserLocationProvider"
    }

    fun hasLocationPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun isLocationEnabled(context: Context): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            locationManager.isLocationEnabled
        } else {
            @Suppress("DEPRECATION")
            locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        }
    }

    suspend fun getLocation(
        context: Context,
        timeoutMs: Long = 10_000
    ): android.location.Location? {
        if (!hasLocationPermission(context)) return null
        if (!isLocationEnabled(context)) return null

        val client: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
        val cancellationToken = CancellationTokenSource()

        return try {
            withTimeoutOrNull(timeoutMs) {
                suspendCancellableCoroutine { continuation ->
                    @Suppress("MissingPermission")
                    client.getCurrentLocation(
                        Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                        cancellationToken.token
                    ).addOnSuccessListener { location ->
                        continuation.resume(location)
                    }.addOnFailureListener { e ->
                        Log.e(TAG, "Failed to get current location", e)
                        continuation.resume(null)
                    }

                    continuation.invokeOnCancellation {
                        cancellationToken.cancel()
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception getting location", e)
            null
        }
    }
}
