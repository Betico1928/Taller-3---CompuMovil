package javeriana.edu.co.taller3_compumovil

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import javeriana.edu.co.taller3_compumovil.databinding.ActivityMainBinding


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

}