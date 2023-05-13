package javeriana.edu.co.taller3_compumovil

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.GoogleMap
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import javeriana.edu.co.taller3_compumovil.databinding.ActivityMainBinding
import javeriana.edu.co.taller3_compumovil.services.BackgroundBootService


class MainActivity : AppCompatActivity()
{
    private lateinit var mAuth:FirebaseAuth
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mAuth = FirebaseAuth.getInstance()

        binding.login.setOnClickListener {
            signInUser(binding.usrName.text.toString(),binding.psw.text.toString())

        }

        binding.register.setOnClickListener {
            val intent = Intent(baseContext, RegisterActivity::class.java)
            startActivity(intent)
        }


        // Always check notification permission.
        notificationPermissionAndListenerServiceStart()

    }

    fun notificationPermissionAndListenerServiceStart() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                MainActivity.NOTIFICATIONS_PERMISSION_REQUEST_CODE
            )
        } else {
            // Init service if device has not been restarted.
            val intent = Intent(this, BackgroundBootService::class.java)
            applicationContext.startForegroundService(intent)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            MainActivity.NOTIFICATIONS_PERMISSION_REQUEST_CODE -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted
                    enableNotifications()
                } else {
                    // Permission denied
                    AlertDialog.Builder(this)
                        .setTitle("Permiso de notification denegado")
                        .setMessage("Debe aceptar el permiso de las notificaciones para poder recibir actualizaciones sobre otros usuarios.\n" +
                                "Si deniega el permiso muchas veces, debera activarlo desde la configuracion")
                        .setPositiveButton("Conceder permiso") { _, _ ->
                            // Retry
                            notificationPermissionAndListenerServiceStart()
                        }
                        .setNegativeButton("Ignorar", null)
                        .show()
                }
            }
        }
    }

    private fun enableNotifications() {
        // TODO: Add code to enable notifications
    }


    override fun onStart() {
        super.onStart()
        val currentUser = mAuth.currentUser
        updateUI(currentUser)
    }

    private fun updateUI(currentUser: FirebaseUser?) {
        if (currentUser != null) {
            val intent = Intent(baseContext, MapsActivity::class.java)
            intent.putExtra("user", currentUser.email)
            startActivity(intent)
        } else {
            binding.usrName.setText("")
            binding.psw.setText("")
        }
    }

    private fun validateForm(): Boolean {
        var valid = true
        val email: String = binding.usrName.text.toString()
        if (TextUtils.isEmpty(email)) {
            binding.usrName.setError("Required.")
            valid = false
        } else {
            binding.usrName.setError(null)
        }
        val password: String = binding.psw.text.toString()
        if (TextUtils.isEmpty(password)) {
            binding.psw.setError("Required.")
            valid = false
        } else {
            binding.psw.setError(null)
        }
        return valid
    }

    private fun signInUser(email: String, password: String) {
        Log.i("creds",email+password)
        if (validateForm()) {
            mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(
                    this
                ) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI
                        Log.d("Auth", "signInWithEmail:success")
                        val user = mAuth.currentUser
                        updateUI(user)
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w("Auth", "signInWithEmail:failure", task.exception)
                        Toast.makeText(
                            this@MainActivity, "Authentication failed.",
                            Toast.LENGTH_SHORT
                        ).show()
                        updateUI(null)
                    }
                }
        }
    }

    companion object {
        private const val NOTIFICATIONS_PERMISSION_REQUEST_CODE = 1000
    }

}