package javeriana.edu.co.taller3_compumovil


import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.renderscript.ScriptGroup.Binding
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import javeriana.edu.co.taller3_compumovil.databinding.ActivityRegisterBinding


class RegisterActivity : AppCompatActivity() {
    private lateinit var mAuth: FirebaseAuth
    private lateinit var binding: ActivityRegisterBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mAuth = FirebaseAuth.getInstance()
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.register.setOnClickListener {
            registerUser(binding.email.text.toString(),binding.password.text.toString())

            }





    }
    private fun registerUser(email: String, password: String){
        if(validateForm()){
            mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this,
                    OnCompleteListener<AuthResult?> { task ->
                        if (task.isSuccessful) {
                            Log.d("Register", "createUserWithEmail:onComplete:" + task.isSuccessful)
                            val user: FirebaseUser? = mAuth.currentUser
                            if (user != null) { //Update user Info
                                val upcrb = UserProfileChangeRequest.Builder()
                                upcrb.displayName =
                                    binding.name.text.toString() + " " + binding.surname.getText()
                                        .toString()
                                // upcrb.photoUri =
                                //    Uri.parse("path/to/pic") //fake uri, use Firebase Storage
                                user.updateProfile(upcrb.build())
                                updateUI(user)
                            }
                        }
                        if (!task.isSuccessful) {
                            Toast.makeText(
                                baseContext, "Task failed" + task.exception.toString(),
                                Toast.LENGTH_SHORT
                            ).show()
                            Log.e("Register", task.exception.toString())
                        }
                    })
        }
    }

    private fun validateForm(): Boolean {
        var valid = true
        val name: String = binding.name.text.toString()
        if (TextUtils.isEmpty(name)) {

            binding.name.error = "Required."
            valid = false
        } else {
            binding.name.error = null
        }
        val password: String = binding.password.text.toString()
        if (TextUtils.isEmpty(password)) {
            binding.password.error = "Required."
            valid = false
        } else {
            binding.password.error = null
        }
        val email: String  = binding.email.text.toString()
        if (TextUtils.isEmpty(email)) {
            binding.email.error = "Required."
            valid = false
        } else {
            binding.email.error = null
        }
        val surname: String  = binding.surname.text.toString()
        if (TextUtils.isEmpty(surname)) {
            binding.surname.error = "Required."
            valid = false
        } else {
            binding.surname.error = null
        }


        return valid
    }


    private fun updateUI(currentUser: FirebaseUser?) {
        if (currentUser != null) {
            val intent = Intent(baseContext, MapsActivity::class.java)
            intent.putExtra("user", currentUser.email)
            startActivity(intent)
        } else {
            binding.name.setText("")
            binding.password.setText("")
            binding.email.setText("")
            binding.surname.setText("")
        }
    }
}