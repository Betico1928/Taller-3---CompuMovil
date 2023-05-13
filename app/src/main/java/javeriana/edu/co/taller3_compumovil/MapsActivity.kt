package javeriana.edu.co.taller3_compumovil

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
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
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import javeriana.edu.co.taller3_compumovil.databinding.ActivityMapsBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    // Map Vars
    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var mapsBinding : ActivityMapsBinding
    private var currentMarker: Marker? = null

    // Realtime DB and Auth Vars
    private lateinit var mAuth: FirebaseAuth

    private lateinit var database: DatabaseReference
    val PATH_USERS = "users/"

    var email: String? = ""
    var uid: String = ""
    var displayName: String? = ""
    var user: User? = null




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

        mapsBinding.disponibleButton.setOnClickListener {

            // Update button color, and change status.
            if (user?.disponible == false){
                user?.disponible = true
                mapsBinding.disponibleButton.setBackgroundColor(ContextCompat.getColor(this, R.color.green))
            } else {
                user?.disponible = false
                mapsBinding.disponibleButton.setBackgroundColor(ContextCompat.getColor(this, R.color.red))
            }

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

        // Update user info
        user?.lat = newLatLng.latitude
        user?.long = newLatLng.longitude
        Log.d("LoggedUserInfo", "User info updated = " + user.toString())

        // Write to realtimeDB
        writeUserToRealtimeDB()


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
        getFirebaseAuthUserInfo()
        initRealtimeDB()
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



    // Firebase funcs

    fun getFirebaseAuthUserInfo() {
        val currentUser = mAuth.currentUser

        if (currentUser != null) {
            // User is signed in, get the user's data
            email = currentUser.email
            uid = currentUser.uid
            displayName = currentUser.displayName

            Log.d("LoggedUserInfo", "User email: $email")
            Log.d("LoggedUserInfo", "User UID: $uid")
            Log.d("LoggedUserInfo", "display Name: $displayName")


            val userEmail: String = email?.toString()!!
            val userName: String = displayName?.toString()!!

            // Create the user.
            user = createUser(userName, userEmail, 0.0, 0.0, false)

            Log.d("LoggedUserInfo", "init user = " + user.toString())


        } else {
            Log.d("LoggedUserInfo", "No user is currently signed in")
        }
    }

    fun createUser(name: String, email: String, lat: Double, long: Double, disponible: Boolean): User {
        return User(name, email, lat, long, disponible)
    }

    fun initRealtimeDB() {
        database = Firebase.database.reference
        Log.d("RealtimeDB", "RealtimeDB init done.")

        subscribeToChanges()
    }

    fun writeUserToRealtimeDB(){
        database.child("users").child(mAuth.currentUser?.uid.toString()).setValue(user)
        Log.d("RealtimeDB", "RealtimeDB user data update.")
    }

    fun subscribeToChanges() {
        val userListener = object : ChildEventListener {
            override fun onChildAdded(dataSnapshot: DataSnapshot, previousChildName: String?) {
                // This method is called once for each child that is added
                val user = dataSnapshot.getValue<User>()
                Log.d("RealtimeDB", "User added: " + user.toString())
            }

            override fun onChildChanged(dataSnapshot: DataSnapshot, previousChildName: String?) {
                // This method is called whenever a child is updated
                val user = dataSnapshot.getValue<User>()
                Log.d("RealtimeDB", "User changed: "  + user.toString())
            }

            override fun onChildRemoved(dataSnapshot: DataSnapshot) {
                // This method is called when a child is removed
                val user = dataSnapshot.getValue<User>()
                Log.d("RealtimeDB", "User removed: "  + user.toString())
            }

            override fun onChildMoved(dataSnapshot: DataSnapshot, previousChildName: String?) {
                // This method is called when a child location is changed
                val user = dataSnapshot.getValue<User>()
                Log.d("RealtimeDB", "User moved: "  + user.toString())
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting User failed, log a message
                Log.d("RealtimeDB", "loadUser:onCancelled", databaseError.toException())
            }
        }

        database.child("users").addChildEventListener(userListener)
    }


}