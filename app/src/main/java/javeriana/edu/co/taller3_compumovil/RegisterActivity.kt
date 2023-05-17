package javeriana.edu.co.taller3_compumovil


import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.*
import com.google.firebase.storage.FirebaseStorage
import javeriana.edu.co.taller3_compumovil.databinding.ActivityRegisterBinding
import javeriana.edu.co.taller3_compumovil.pojos.User


class RegisterActivity : AppCompatActivity() {
    private lateinit var mAuth: FirebaseAuth
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var uri: Uri


    val mStorageRef = FirebaseStorage.getInstance().reference


    private val galleryrequest = registerForActivityResult(ActivityResultContracts.GetContent(), ActivityResultCallback { result: Uri? -> loadImage(result) })
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mAuth = FirebaseAuth.getInstance()
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.register.setOnClickListener {
            registerUser(binding.email.text.toString(),binding.password.text.toString())


        }

        binding.upload.setOnClickListener {

            galleryrequest.launch("image/*")

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
                                upcrb.displayName = binding.name.text.toString() + " " + binding.surname.text.toString()+";"+binding.identification.text.toString()


                                // upcrb.photoUri =
                                //    Uri.parse("path/to/pic") //fake uri, use Firebase Storage
                                user.updateProfile(upcrb.build())

                                val imageRef = mStorageRef.child("images/profile/${binding.email.text.toString()}/profile.jpg")
                                imageRef.putFile(uri)




                                updateUI(user)
                            }
                        }
                        if (!task.isSuccessful) {
                            val exception = task.exception

                            if (exception is FirebaseAuthUserCollisionException) {
                                // Email already exists
                                Toast.makeText(
                                    baseContext, "The email address is already in use by another account",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            else if  (exception is FirebaseAuthInvalidCredentialsException) {
                                // Email already exists
                                Toast.makeText(
                                    baseContext, "The email address is badly formatted",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            else{
                                Toast.makeText(
                                    baseContext, "Task failed" + task.exception.toString(),
                                    Toast.LENGTH_SHORT
                                ).show()
                                Log.e("Register", task.exception.toString())

                            }

                        }
                    })
        }
    }

    fun createUser(name: String, email: String, lat: Double, long: Double, disponible: Boolean, codigo:String): User {
        return User(name, email, lat, long, disponible,codigo)
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
        }
        val codigo: String = binding.identification.text.toString()
        if(TextUtils.isEmpty(codigo)){
            binding.identification.error = "Required."
            valid = false

        }
        if (binding.imageView.drawable == null) { // add image validation
            Toast.makeText(this, "Please select an image.", Toast.LENGTH_SHORT).show()
            valid = false
        }
        else{
            binding.surname.error = null
        }


        return valid
    }

    private fun updateUI(currentUser: FirebaseUser?) {
        if (currentUser != null) {
            val intent = Intent(baseContext, MainActivity::class.java)
            intent.putExtra("user", binding.identification.text.toString())

            Handler().postDelayed({
                Log.d("RealtimeDB", "Waiting 3 seconds")
                startActivity(intent)
            }, 3000) // 3000 milliseconds = 3 seconds
        } else {
            binding.name.setText("")
            binding.password.setText("")
            binding.email.setText("")
            binding.surname.setText("")
        }
    }

    private fun loadImage(result: Uri?){
        val imageStream = contentResolver.openInputStream(result!!)
        val image = BitmapFactory.decodeStream(imageStream)
        binding.imageView.setImageBitmap(image)
        val maxWidth = resources.getDimensionPixelSize(R.dimen.max_image_width)
        val maxHeight = resources.getDimensionPixelSize(R.dimen.max_image_height)
        val params = binding.imageView.layoutParams
        params.width = resources.getDimensionPixelSize(R.dimen.max_image_width)
        params.height = resources.getDimensionPixelSize(R.dimen.max_image_height)
        binding.imageView.scaleType = ImageView.ScaleType.FIT_CENTER
        uri = result


    }


}