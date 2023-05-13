package javeriana.edu.co.taller3_compumovil

import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
class User {
    var name: String = ""
        get() = field
        set(value) {
            field = value
        }

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

    constructor()

    constructor(name: String, email: String, lat: Double, long: Double, disponible: Boolean) {
        this.name = name
        this.email = email
        this.lat = lat
        this.long = long
        this.disponible = disponible
    }

    @Exclude
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "name" to name,
            "email" to email,
            "lat" to lat,
            "long" to long,
            "disponible" to disponible
        )
    }

    override fun toString(): String {
        return "User(name='$name', email='$email', lat=$lat, long=$long, disponible=$disponible)"
    }

    @IgnoreExtraProperties
    data class User(
        val name: String?,
        val email: String?,
        val lat: Double?,
        val long: Double?,
        val disponible: Boolean?
    ) {
        // Null default values create a no-argument default constructor, which is needed
        // for deserialization from a DataSnapshot.
    }

}
