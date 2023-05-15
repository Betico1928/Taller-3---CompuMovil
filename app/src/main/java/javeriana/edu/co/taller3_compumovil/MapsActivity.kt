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
import com.google.android.gms.maps.CameraUpdateFactory
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
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import javeriana.edu.co.taller3_compumovil.databinding.ActivityMapsBinding
import javeriana.edu.co.taller3_compumovil.pojos.User
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
    private var otherUserEmail: String? = null
    private var userMarker: Marker? = null

    // Realtime DB and Auth Vars
    private lateinit var mAuth: FirebaseAuth
    private lateinit var database: DatabaseReference
    var email: String? = ""
    var uid: String = ""
    var displayName: String? = ""
    var user: User? = null
    var codigo: String? =""
    var rawName: String? = ""

    // Global listener is required so it can be removed later
    val userListener = object : ChildEventListener {
        override fun onChildAdded(dataSnapshot: DataSnapshot, previousChildName: String?) {
            // This method is called once for each child that is added
            val user = dataSnapshot.getValue<User>()
            Log.d("RealtimeDB", "Listener -> User added: " + user.toString())
        }
        override fun onChildChanged(dataSnapshot: DataSnapshot, previousChildName: String?) {
            // This method is called whenever a child is updated
            val user = dataSnapshot.getValue<User>()
            Log.d("RealtimeDB", "Listener -> User changed: "  + user.toString())
        }
        override fun onChildRemoved(dataSnapshot: DataSnapshot) {
            // This method is called when a child is removed
            val user = dataSnapshot.getValue<User>()
            Log.d("RealtimeDB", "Listener -> User removed: "  + user.toString())
        }
        override fun onChildMoved(dataSnapshot: DataSnapshot, previousChildName: String?) {
            // This method is called when a child location is changed
            val user = dataSnapshot.getValue<User>()
            Log.d("RealtimeDB", "Listener -> User moved: "  + user.toString())
        }
        override fun onCancelled(databaseError: DatabaseError) {
            // Getting User failed, log a message
            Log.d("RealtimeDB", "Listener -> loadUser:onCancelled", databaseError.toException())
        }
    }

    // Map funcs

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        mapsBinding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(mapsBinding.root)

        encenderGPS()

        otherUserEmail = intent.getStringExtra("user")


        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment

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
        mapsBinding.verUsuariosButton.setOnClickListener {
            val intent = Intent(baseContext, ListActivity::class.java)
            startActivity(intent)
        }

        mapsBinding.logOutButton.setOnClickListener {
            mAuth.signOut()
            Toast.makeText(baseContext, "Cerrando sesión...", Toast.LENGTH_LONG).show()

            // Unsub from listener.
            unSubscribeToChanges()

            val retrocederALogIn = Intent(baseContext, MainActivity::class.java)
            startActivity(retrocederALogIn)
        }

        mapsBinding.disponibleButton.setOnClickListener {

            // Check that name is set. Get data again
            mAuth = FirebaseAuth.getInstance()
            val currentUser = mAuth.currentUser

            if (currentUser != null) {
                // User is signed in, get the user's data
                rawName = currentUser.displayName
                val parts = rawName?.split(";")
                email = currentUser.email
                uid = currentUser.uid
                displayName = parts?.getOrNull(0) ?: ""
                codigo = parts?.getOrNull(1) ?: ""

                val userEmail: String = email?.toString()!!
                val userName: String = displayName?.toString()!!
                val userID: String = codigo?.toString()!!

                user?.name ?: userName
                user?.codigo ?: userID

                Log.d("RealtimeDB", "UserName and Code Checked for Disponible change = " + user.toString())
            }

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

        otherUserEmail?.let { fetchUserByEmail(it) }

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


    private fun fetchUserByEmail(otherUserEmail: String)
    {
        val database = Firebase.database
        val usersRef = database.getReference("users")

        Log.i("Buscar usuario en la RTDB", "Email del usuario a buscar: $otherUserEmail")

        val query = usersRef.orderByChild("email").equalTo(otherUserEmail)

        query.addListenerForSingleValueEvent(object : ValueEventListener
        {
            override fun onDataChange(dataSnapshot: DataSnapshot)
            {
                val userSnapshot = dataSnapshot.children.iterator().next()

                // Extraer los datos y asignarlos a las variables
                val name = userSnapshot.child("name").value as String
                val lat = userSnapshot.child("lat").value as Double
                val long = userSnapshot.child("long").value as Double

                // Imprimir los valores de las variables
                Log.i("Buscar usuario en la RTDB", "Usuario encontrado ->")
                Log.i("Buscar usuario en la RTDB", "Usuario encontrado -> Nombre: $name")
                Log.i("Buscar usuario en la RTDB", "Usuario encontrado -> Latitud: $lat")
                Log.i("Buscar usuario en la RTDB", "Usuario encontrado -> Longitud: $long")

                val userPositionWithRTDB = LatLng(lat, long)

                // Eliminar el marcador antiguo, si existe
                userMarker?.remove()

                // Crear un nuevo marcador y asignarlo a la referencia del marcador
                userMarker = mMap.addMarker(MarkerOptions().position(userPositionWithRTDB).title(name).snippet(userPositionWithRTDB.toString()))
                Log.i("Buscar usuario en la RTDB", "Marcador añadido en: $userPositionWithRTDB")
                //mMap.moveCamera(CameraUpdateFactory.newLatLng(userPositionWithRTDB))
            }

            override fun onCancelled(databaseError: DatabaseError)
            {
                Log.i("Buscar usuario en la RTDB", "Error al buscar al usuario: ${databaseError.toException()}")
            }
        })
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
            else {
                // Permission denied
                AlertDialog.Builder(this)
                    .setTitle("Permiso de localizacion denegado")
                    .setMessage("Debe aceptar el permiso de la localizacion para poder ver su propia ubicacion en el mapa.\nSi deniega el permiso muchas veces, debera activarlo desde la configuracion")
                    .setPositiveButton("Conceder permiso") { _, _ ->
                        // Retry
                        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                            != PackageManager.PERMISSION_GRANTED
                        ) {
                            ActivityCompat.requestPermissions(
                                this,
                                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                                LOCATION_PERMISSION_REQUEST_CODE
                            )
                        }
                    }
                    .setNegativeButton("Ignorar", null)
                    .show()
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
            rawName = currentUser.displayName
            val parts = rawName?.split(";")
            email = currentUser.email
            uid = currentUser.uid
            displayName = parts?.getOrNull(0) ?: ""
            codigo = parts?.getOrNull(1) ?: ""

            // If something is null, make it empty
            // displayName = parts.getOrNull(0) ?: ""
            // codigo = parts.getOrNull(1) ?: ""


            Log.d("LoggedUserInfo", "User email: $email")
            Log.d("LoggedUserInfo", "User UID: $uid")
            Log.d("LoggedUserInfo", "display Name: $displayName")


            val userEmail: String = email?.toString()!!
            val userName: String = displayName?.toString()!!
            val userID: String = codigo?.toString()!!

            // Create the user.
            user = createUser(userName, userEmail, 0.0, 0.0, false, userID)

            Log.d("LoggedUserInfo", "init user = " + user.toString())


        } else {
            Log.d("LoggedUserInfo", "No user is currently signed in")
        }
    }

    fun createUser(name: String, email: String, lat: Double, long: Double, disponible: Boolean, codigo:String): User {
        return User(name, email, lat, long, disponible,codigo)
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
        // Add already defined listener
        database.child("users").addChildEventListener(userListener)
        Log.d("RealtimeDB", "Subbed to changes.")
    }

    fun unSubscribeToChanges() {
        // Add already defined listener
        database.child("users").removeEventListener(userListener)
        Log.d("RealtimeDB", "UNSubbed to changes.")
    }


}