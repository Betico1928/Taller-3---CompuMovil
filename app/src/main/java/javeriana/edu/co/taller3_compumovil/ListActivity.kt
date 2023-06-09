package javeriana.edu.co.taller3_compumovil

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import javeriana.edu.co.taller3_compumovil.adapters.CustomListAdapter
import javeriana.edu.co.taller3_compumovil.adapters.Item
import javeriana.edu.co.taller3_compumovil.databinding.ActivityListBinding
import javeriana.edu.co.taller3_compumovil.pojos.RTDUser
import javeriana.edu.co.taller3_compumovil.pojos.User

private lateinit var storage: FirebaseStorage
private lateinit var binding: ActivityListBinding

class ListActivity : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    private lateinit var itemList: ArrayList<Item>
    private lateinit var adapter: CustomListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        storage = FirebaseStorage.getInstance()

        database = FirebaseDatabase.getInstance().reference.child("users")

        val userList = mutableListOf<RTDUser>()
        itemList = ArrayList<Item>()
        adapter = CustomListAdapter(this, itemList)


        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d("ListActivity", "onDataChange called")
                for (userSnapshot in snapshot.children) {
                    val user = userSnapshot.getValue(RTDUser::class.java)
                    if (user != null && user.disponible) {
                        val imagePath = "images/profile/${user.email}/profile.jpg"
                        Log.d("IMASD", "Added item to list: $imagePath")

                        val storageReference = storage.getReference(imagePath)
                        val itemText = user.name

                        storageReference.downloadUrl.addOnSuccessListener { uri ->
                            val item = Item(uri, itemText, View.OnClickListener {
                                // Handle button click here
                                val mostrarAlUsuarioEnElMapa = Intent(baseContext, MapsActivity::class.java)
                                mostrarAlUsuarioEnElMapa.putExtra("user", user.email)
                                startActivity(mostrarAlUsuarioEnElMapa)
                            })
                            if (!itemList.any { it.text == itemText }) { // check if itemText already exists
                                itemList.add(item)
                                Log.d("ListActivity", "Added item to list: $item")
                                Log.d("ListActivity", "itemList size: ${itemList.size}")
                                adapter.notifyDataSetChanged()
                                binding.list.adapter = adapter
                            }
                        }.addOnFailureListener { exception ->
                            Log.e("ListActivity", "Error downloading image: ${exception.message}")
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ListActivity", "onCancelled: ${error.message}")
            }
        })

    }
}
