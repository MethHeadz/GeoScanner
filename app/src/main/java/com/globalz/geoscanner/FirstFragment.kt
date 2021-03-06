package com.globalz.geoscanner

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_first.*

private const val PERMISSION_REQUEST = 10
private const val ALPHA_DISABLED = 0.3F
private const val ALPHA_ENABLED = 1F
private var globalCoordinateModus: CoordinateMode = CoordinateMode.UNKNOWN

public enum class CoordinateMode {
    GET_INITIAL_COORDS,
    GET_ACTUAL_COORDS,
    UNKNOWN
}

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment() {

    lateinit var locationManager: LocationManager
    private var hasGps = false
    private var hasNetwork = false
    private var locationGps: Location? = null
    private var locationNetwork: Location? = null

    private var permissions = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_first, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initGetCoordinateButtons()
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            if (!checkPermission(permissions))
                requestPermissions(permissions, PERMISSION_REQUEST)
        }
    }

    private fun UpdateCoordField(actualText: String) {

        when (globalCoordinateModus) {
            CoordinateMode.GET_INITIAL_COORDS -> {
                tv_start_location.text = ""
                tv_start_location.append(actualText)
                globalCoordinateModus = CoordinateMode.UNKNOWN
            }

            CoordinateMode.GET_ACTUAL_COORDS -> {
                tv_actual_location.text = ""
                tv_actual_location.append(actualText)
                globalCoordinateModus = CoordinateMode.UNKNOWN
            }

            CoordinateMode.UNKNOWN -> return
        }
    }

    @SuppressLint("MissingPermission")
    private fun getLocations() {
        ((activity as MainActivity).getSystemService(AppCompatActivity.LOCATION_SERVICE) as LocationManager).also { locationManager = it }
        hasGps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        hasNetwork = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        if (hasGps || hasNetwork) {

            // GPS
            if (hasGps) {
                Log.d("CoseAndroidLocation", "hasGps")
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 0F, object:
                    LocationListener {
                    override fun onLocationChanged(location: Location) {
                        locationGps = location

                        val txtCoord = "${locationGps!!.latitude} / ${locationGps!!.longitude}"
                        UpdateCoordField(txtCoord)

                        Log.d("CodeAndroidLocation", " GPS Latitude: " + locationGps!!.latitude)
                        Log.d("CodeAndroidLocation", " GPS Longitude: " + locationGps!!.longitude)
                    }
                })
            }

            var localGpsLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            if (localGpsLocation != null)
                locationGps = localGpsLocation

            // NETWORK
            if (hasNetwork) {
                Log.d("CoseAndroidLocation", "hasGps")
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 0F, object:
                    LocationListener {
                    override fun onLocationChanged(location: Location) {
                        locationNetwork  = location

                        val txtCoord = "${locationGps!!.latitude} / ${locationGps!!.longitude}"
                        UpdateCoordField(txtCoord)

                        Log.d("CodeAndroidLocation", " Network Latitude: " + locationNetwork!!.latitude)
                        Log.d("CodeAndroidLocation", " Network Longitude: " + locationNetwork!!.longitude)
                    }
                })
            }

            var localNetworkLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            if (localNetworkLocation  != null)
                locationNetwork = localNetworkLocation

            // GET one of both!
            if (locationGps != null && locationNetwork != null)
            {
                if (locationGps!!.accuracy > locationNetwork!!.accuracy) {

                    val txtCoord = "${locationNetwork!!.latitude} / ${locationNetwork!!.longitude}"
                    UpdateCoordField(txtCoord)

                    Log.d("CodeAndroidLocation", " Network Latitude: " + locationNetwork!!.latitude)
                    Log.d("CodeAndroidLocation", " Network Longitude: " + locationNetwork!!.longitude)
                }
                else
                {
                    val txtCoord = "${locationGps!!.latitude} / ${locationGps!!.longitude}"
                    UpdateCoordField(txtCoord)

                    Log.d("CodeAndroidLocation", " GPS Latitude: " + locationGps!!.latitude)
                    Log.d("CodeAndroidLocation", " GPS Longitude: " + locationGps!!.longitude)
                }
            }
        }
        else {
            startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
        }
    }

    @SuppressLint("MissingPermission")
    private fun getLocations__NoListener() {

        ((activity as MainActivity).getSystemService(AppCompatActivity.LOCATION_SERVICE) as LocationManager).also { locationManager = it }

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0L, 0F, LocationListener {  })

        hasGps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        hasNetwork = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        if (hasGps || hasNetwork) {

            // GPS
            if (hasGps) {
                Log.d("CoseAndroidLocation", "hasGps")
                locationGps = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            }

            // NETWORK
            if (hasNetwork) {
                Log.d("CoseAndroidLocation", "hasNetwork")
                locationNetwork = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            }

            // GET one of both!
            if (locationGps != null && locationNetwork != null)
            {
                if (locationGps!!.accuracy > locationNetwork!!.accuracy) {

                    val txtCoord = "${locationGps!!.latitude} / ${locationGps!!.longitude}"
                    UpdateCoordField(txtCoord)

                    Log.d("CodeAndroidLocation", " Network Latitude: " + locationNetwork!!.latitude)
                    Log.d("CodeAndroidLocation", " Network Longitude: " + locationNetwork!!.longitude)
                }
                else
                {
                    val txtCoord = "${locationGps!!.latitude} / ${locationGps!!.longitude}"
                    UpdateCoordField(txtCoord)

                    Log.d("CodeAndroidLocation", " GPS Latitude: " + locationGps!!.latitude)
                    Log.d("CodeAndroidLocation", " GPS Longitude: " + locationGps!!.longitude)
                }
            }
        }
        else {
            startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
        }
    }

    private fun checkPermission(permissionArray: Array<String>): Boolean {
        var allSuccess = true
        for (i in permissionArray.indices) {
            if ((activity as MainActivity).checkCallingOrSelfPermission(permissionArray[i]) == PackageManager.PERMISSION_DENIED)
                allSuccess = false
        }
        return allSuccess
    }

    private fun initGetCoordinateButtons() {
        // Initial Coords Button
        (activity as MainActivity).btnGetMyInitialCoords.isEnabled = true
        (activity as MainActivity).btnGetMyInitialCoords?.setOnClickListener {
            globalCoordinateModus = CoordinateMode.GET_INITIAL_COORDS

            // Buttons initialisieren
            freeCoordinatesButton()

            getLocations__NoListener()
        }

        (activity as MainActivity).btnResetCoordinates?.setOnClickListener {
            resetApp()
        }

        // Actual Coords Button
        (activity as MainActivity).btnGetActualCoords.isEnabled = false
        (activity as MainActivity).btnGetActualCoords.alpha = ALPHA_DISABLED
        (activity as MainActivity).btnGetActualCoords?.setOnClickListener {
            globalCoordinateModus = CoordinateMode.GET_ACTUAL_COORDS
            getLocations__NoListener()
        }
    }

    private fun freeCoordinatesButton() {
        (activity as MainActivity).btnGetMyInitialCoords.isEnabled = false
        (activity as MainActivity).btnGetMyInitialCoords.alpha = ALPHA_DISABLED

        (activity as MainActivity).btnGetActualCoords.isEnabled = true
        (activity as MainActivity).btnGetActualCoords.alpha = ALPHA_ENABLED
        Toast.makeText(activity, "Ahora actualiza tu actual posici??n", Toast.LENGTH_LONG).show()
    }

    private fun resetApp() {
        val builder = AlertDialog.Builder(activity)
        builder.setMessage("Deseas reiniciar?")
            .setCancelable(false)
            .setPositiveButton("S??") { dialog, id ->
                tv_start_location.text = ""
                tv_actual_location.text = ""
                (activity as MainActivity).btnGetMyInitialCoords.isEnabled = true
                (activity as MainActivity).btnGetMyInitialCoords.alpha = ALPHA_ENABLED

                (activity as MainActivity).btnGetActualCoords.isEnabled = false
                (activity as MainActivity).btnGetActualCoords.alpha = ALPHA_DISABLED

                Toast.makeText(activity, "Aplicaci??n reiniciada.", Toast.LENGTH_LONG).show()
            }
            .setNegativeButton("No") { dialog, id ->
                // Dismiss the dialog
                dialog.dismiss()
            }
        val alert = builder.create()
        alert.show()
    }
}