package javeriana.edu.co.taller3_compumovil.pojos

import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
class User {
    var name: String = ""

    var email: String = ""
        get() = field
        set(value) {
            field = value
        }

    var lat: Double = 0.0
        get() = field
        set(value) {
            field = value
        }

    var long: Double = 0.0
        get() = field
        set(value) {
            field = value
        }

     var disponible: Boolean = false
        get() = field
        set(value) {
            field = value
        }
    var codigo: String =""

    constructor()
    constructor(
        name: String,
        email: String,
        lat: Double,
        long: Double,
        disponible: Boolean,
        codigo: String
    ) {
        this.name = name
        this.email = email
        this.lat = lat
        this.long = long
        this.disponible = disponible
        this.codigo = codigo
    }


    @Exclude
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "name" to name,
            "email" to email,
            "lat" to lat,
            "long" to long,
            "disponible" to disponible,
            "codigo" to codigo
        )
    }

    override fun toString(): String {
        return "User(name='$name', email='$email', lat=$lat, long=$long, disponible=$disponible, codigo='$codigo')"
    }


    @IgnoreExtraProperties
    data class User(
        val name: String?,
        val email: String?,
        val lat: Double?,
        val long: Double?,
        val disponible: Boolean?,
        val codigo: String?
    ) {
        // Null default values create a no-argument default constructor, which is needed
        // for deserialization from a DataSnapshot.
    }

}
