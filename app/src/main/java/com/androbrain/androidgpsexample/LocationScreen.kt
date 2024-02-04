package com.androbrain.androidgpsexample

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.google.android.gms.location.LocationAvailability
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationToken
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.OnTokenCanceledListener

@Composable
fun LocationScreen() {
    var timestamp by remember { mutableLongStateOf(0L) }
    var currentLocation by remember { mutableStateOf("LOCATION: NONE") }
    val context = LocalContext.current
    val client = remember { LocationServices.getFusedLocationProviderClient(context) }
    val callback = remember {
        object : LocationCallback() {
            override fun onLocationAvailability(availability: LocationAvailability) {
                if (availability.isLocationAvailable) {
                    // Location available
                } else {
                    // Handle location being unavailable
                }
            }

            override fun onLocationResult(result: LocationResult) {
                result.locations.forEach { location ->
                    // Update data using location
                    currentLocation = "PERIODICAL: $location"
                    timestamp = location.time
                }
            }
        }
    }
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) {
                client.removeLocationUpdates(callback)
            }
            if (event == Lifecycle.Event.ON_START) {
                // TODO if you want to you can resume collecting the location here
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(text = "TIMESTAMP: $timestamp", textAlign = TextAlign.Center)
        Text(text = currentLocation, textAlign = TextAlign.Center)
        Button(onClick = {
            if (checkLocationPermission(context)) {
                client.lastLocation.addOnSuccessListener { location: Location? ->
                    currentLocation = "LAST LOCATION: ${location.toString()}"
                }
            }
        }) {
            Text(text = "Get last location")
        }

        Button(onClick = {
            if (checkLocationPermission(context)) {
                client.getCurrentLocation(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    // You can replace it with your required behaviour
                    object : CancellationToken() {
                        override fun onCanceledRequested(p0: OnTokenCanceledListener) =
                            CancellationTokenSource().token

                        override fun isCancellationRequested() = false
                    }
                ).addOnSuccessListener { location: Location? ->
                    currentLocation = "CURRENT LOCATION: ${location.toString()}"
                }
            }
        }) {
            Text(text = "Get current location")
        }

        Button(onClick = {
            val request = LocationRequest.Builder(
                /* Priority */ Priority.PRIORITY_HIGH_ACCURACY,
                /* intervalMillis */ 100
            ).build()
            client.requestLocationUpdates(
                request,
                callback,
                Looper.myLooper(),
            )
        }) {
            Text(text = "Get location periodically")
        }
    }
}

private fun checkLocationPermission(context: Context): Boolean = ActivityCompat.checkSelfPermission(
    context,
    Manifest.permission.ACCESS_FINE_LOCATION
) == PackageManager.PERMISSION_GRANTED ||
        ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
