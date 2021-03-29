package com.template.locationsettingssample

import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {
    private val locationRequest: LocationRequest = LocationRequest.create().apply {
        interval = TimeUnit.SECONDS.toMillis(3)
        fastestInterval = TimeUnit.SECONDS.toMillis(1) // 最速更新間隔(ms)
        priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<Button>(R.id.buttonCheckLocationSettings).setOnClickListener {
            checkLocationSettings(this@MainActivity)
        }
    }

    private fun checkLocationSettings(activity: Activity) {
        val builder = LocationSettingsRequest.Builder().apply {
            addLocationRequest(locationRequest)
        }
        val task: Task<LocationSettingsResponse> =
                LocationServices.getSettingsClient(activity).checkLocationSettings(builder.build())
        task.addOnCompleteListener { task ->
            kotlin.runCatching {
                val response = task.getResult(ApiException::class.java)
                Log.d(LOG_TAG, "isBlePresent: " + response.locationSettingsStates?.isBlePresent)
                Log.d(LOG_TAG, "isBleUsable: " + response.locationSettingsStates?.isBleUsable)
                Log.d(LOG_TAG, "isGpsPresent: " + response.locationSettingsStates?.isGpsPresent)
                Log.d(LOG_TAG, "isGpsUsable: " + response.locationSettingsStates?.isGpsUsable)
                Log.d(
                        LOG_TAG,
                        "isLocationPresent: " + response.locationSettingsStates?.isLocationPresent
                )
                Log.d(
                        LOG_TAG,
                        "isLocationUsable: " + response.locationSettingsStates?.isLocationUsable
                )
                Log.d(
                        LOG_TAG,
                        "isNetworkLocationPresent: " + response.locationSettingsStates?.isNetworkLocationPresent
                )
                Log.d(
                        LOG_TAG,
                        "isNetworkLocationUsable: " + response.locationSettingsStates?.isNetworkLocationUsable
                )
                showToast("All location settings are satisfied. The client can initialize location requests here.")
            }.onFailure {
                if (it !is ApiException) return@onFailure
                when (it.statusCode) {
                    LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> {
                        showToast("Location settings are not satisfied. But could be fixed by showing the user a dialog.")
                        try {
                            val resolvable: ResolvableApiException? = it as? ResolvableApiException
                            resolvable?.startResolutionForResult(
                                    activity,
                                    REQUEST_CHECK_SETTINGS
                            )
                        } catch (e: IntentSender.SendIntentException) {
                            Log.e(LOG_TAG, "Ignore the error: $e")
                        }
                    }
                    LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {
                        showToast("Location settings are not satisfied. However, we have no way to fix the settings so we won't show the dialog.")
                    }
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_CHECK_SETTINGS -> {
                val message = when (resultCode) {
                    Activity.RESULT_OK -> {
                        "All required changes were successfully made."
                    }
                    Activity.RESULT_CANCELED -> {
                        "The user was asked to change settings, but chose not to"
                    }
                    else -> "unknown..."
                }
                showToast(message)
                if (resultCode == Activity.RESULT_OK) {
                    val states: LocationSettingsStates? =
                            data?.let { LocationSettingsStates.fromIntent(it) }
                    Log.d(LOG_TAG, "isGpsPresent: " + states?.isGpsPresent)
                    Log.d(LOG_TAG, "isGpsUsable: " + states?.isGpsUsable)
                }
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this@MainActivity, message, Toast.LENGTH_SHORT).show()
    }


    companion object {
        const val LOG_TAG = "MainActivity"
        const val REQUEST_CHECK_SETTINGS = 100
    }
}