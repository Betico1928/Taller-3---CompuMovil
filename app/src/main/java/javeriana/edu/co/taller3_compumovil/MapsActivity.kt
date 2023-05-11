package javeriana.edu.co.taller3_compumovil

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import javeriana.edu.co.taller3_compumovil.databinding.ActivityMapsBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var mapsBinding : ActivityMapsBinding
    private var currentMarker: Marker? = null
    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mapsBinding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(mapsBinding.root)

        encenderGPS()

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        locationCallback = object : LocationCallback()
        {
            override fun onLocationResult(locationResult: LocationResult)
            {
                locationResult
                for (location in locationResult.locations)
                {
                    updateMarker(location)
                }
            }
        }

        mapsBinding.logOutButton.setOnClickListener {
            mAuth.signOut()
            Toast.makeText(baseContext, "Cerrando sesión...", Toast.LENGTH_LONG).show()

            val retrocederALogIn = Intent(baseContext, MainActivity::class.java)
            startActivity(retrocederALogIn)
        }
    }

    override fun onMapReady(googleMap: GoogleMap)
    {
        mMap = googleMap

        loadLocationsFromJson()

        // Zoom
        mMap.uiSettings.isZoomGesturesEnabled = true
        // Controles de zoom
        mMap.uiSettings.isZoomControlsEnabled = true

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
            enableMyLocation()
        }
    }

    private fun encenderGPS()
    {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
        {
            val builder = AlertDialog.Builder(this)
            builder.setMessage("El GPS está apagado, ¿desea encenderlo?").setCancelable(false).setPositiveButton("Sí") { _, _ ->
                startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            }
                .setNegativeButton("No") { dialog, _ ->
                    dialog.cancel()
                }
            val alert = builder.create()
            alert.show()
        }
    }

    private fun enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        ) {
            mMap.isMyLocationEnabled = true
            startLocationUpdates()
        }
    }

    private fun startLocationUpdates() {
        val locationRequest = LocationRequest.create().apply {
            interval = 10000
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
        }
    }

    private fun updateMarker(location: Location) {
        val newLatLng = LatLng(location.latitude, location.longitude)

        if (currentMarker == null)
        {
            currentMarker = mMap.addMarker(MarkerOptions().position(newLatLng).title("Ubicacion Actual"))
        }
        else
        {
            currentMarker?.position = newLatLng
        }

        // Que la camara se mueva con la ubicación
        //mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(newLatLng, 15f))
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableMyLocation()
            }
        }
    }

    override fun onStart()
    {
        super.onStart()

        mAuth = FirebaseAuth.getInstance()
    }

    override fun onResume() {
        super.onResume()
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        ) {
            startLocationUpdates()
        }
    }

    override fun onPause() {
        super.onPause()
        stopLocationUpdates()
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    companion object
    {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1000
    }


    private fun loadLocationsFromJson()
    {
        GlobalScope.launch(Dispatchers.IO)
        {
            val jsonFile = assets.open("locations.json").bufferedReader().use{ it.readText() }

            val jsonObject = JSONObject(jsonFile)
            val locationsArray: JSONArray = jsonObject.getJSONArray("locationsArray")

            withContext(Dispatchers.Main)
            {
                for (i in 0 until locationsArray.length())
                {
                    val locationObject = locationsArray.getJSONObject(i)
                    val latitude = locationObject.getDouble("latitude")
                    val longitude = locationObject.getDouble("longitude")
                    val name = locationObject.getString("name")
                    val latLng = LatLng(latitude, longitude)
                    mMap.addMarker(MarkerOptions().position(latLng).title(name))
                }
            }
        }
    }
}