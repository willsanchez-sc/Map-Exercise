package com.example.mapexercise

import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import java.util.jar.Manifest

class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    lateinit var mGoogleMap: GoogleMap
    var mapFragment: SupportMapFragment? = null
    lateinit var locationRequest: LocationRequest
    var lastLocation: Location? = null
    internal var currMarker: Marker? = null
    internal var fusedLocationClient: FusedLocationProviderClient? = null



    internal var locationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            // Update location
            val locationList = locationResult.locations

//            Log.i("Loggin", "my log")


            if (locationList.isNotEmpty()) {

                val location = locationList.last()
                Log.i("YAAAa: " + location.latitude, "Longitude: " + location.longitude)
                lastLocation = location
                if (currMarker != null) {
                    currMarker?.remove()
                }



                // Move marker/camera to current location
                val geoPosition = LatLng(location.latitude, location.longitude)
                val markerOptions = MarkerOptions()
                markerOptions.position(geoPosition)
                //markerOptions.position(LatLng(50.0, 50.0))
                markerOptions.title("Current Location")
                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA))
                currMarker = mGoogleMap.addMarker(markerOptions)

                mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(geoPosition, 11.0F))

//                    addMarker(markerOptions)
//                    moveCamera(CameraUpdateFactory.newLatLngZoom(geoPosition, 11.0F))

            }

        }
    }

    



    // Set up activity and put on fragment
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // supportActionBar?.title = "test"

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)

    }


    override fun onMapReady(googleMap: GoogleMap) {
        mGoogleMap = googleMap
        mGoogleMap.mapType = GoogleMap.MAP_TYPE_HYBRID

        locationRequest = LocationRequest()
        locationRequest.priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
        locationRequest.interval = 6000
        locationRequest.fastestInterval = 6000



        // Check on permissions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(
                            this, android.Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED) {
                fusedLocationClient?.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper())
                mGoogleMap.isMyLocationEnabled = true
            } else {
                checkLocationPermission()
            }

        } else {
            fusedLocationClient?.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper())
            mGoogleMap.isMyLocationEnabled = true
        }
    }

    // Make sure user has location services on
    // if not, alert them
    private fun checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(
                        this,
                        android.Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                            this,
                            android.Manifest.permission.ACCESS_FINE_LOCATION
                    )) {
                AlertDialog.Builder(this)
                        .setTitle("Requesting Permission")
                        .setMessage("Location permission is needed")
                        .create()
                        .show()
            } else {
                ActivityCompat.requestPermissions(
                        this,
                        arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                        MY_PERMISSIONS_REQUEST_LOCATION
                )
            }
        }
    }

    // If permission is granted, then try to update locatoin
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            MY_PERMISSIONS_REQUEST_LOCATION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(
                                    this,
                                    android.Manifest.permission.ACCESS_FINE_LOCATION
                            ) == PackageManager.PERMISSION_GRANTED) {

                        fusedLocationClient?.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper())
                        mGoogleMap.isMyLocationEnabled = true
                    }
                } else {
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show()
                }
                return
            }
        }
    }

    // Get the location of the device
    private fun getCurrLocation() {
        try {
            val locationResult = fusedLocationClient?.lastLocation
            locationResult?.addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    lastLocation = task.result
                    if (lastLocation != null) {
                        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(lastLocation!!.latitude, lastLocation!!.longitude), 11.0F))
                    }
                }
            }

        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message)
        }
    }


    companion object {
        val MY_PERMISSIONS_REQUEST_LOCATION = 99
    }

}