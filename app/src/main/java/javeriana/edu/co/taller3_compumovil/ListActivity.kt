package javeriana.edu.co.taller3_compumovil

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FileDownloadTask
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.File
import java.io.IOException


private lateinit var storage: FirebaseStorage
private lateinit var database: FirebaseDatabase
val mStorageRef = FirebaseStorage.getInstance().reference
private lateinit var databaseRef: DatabaseReference

class ListActivity : AppCompatActivity() {





    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        storage = FirebaseStorage.getInstance()
        database = FirebaseDatabase.getInstance()
        databaseRef = Firebase.database.reference

        

        setContentView(R.layout.activity_list)
    }
}


fun listUsers(): MutableList<String> {

    val users: MutableList<String> = mutableListOf()

    return users
}