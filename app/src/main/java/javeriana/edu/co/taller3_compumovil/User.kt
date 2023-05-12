package javeriana.edu.co.taller3_compumovil

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

    constructor(name: String, email: String, lat: Double, long: Double) {
        this.name = name
        this.email = email
        this.lat = lat
        this.long = long
    }

    override fun toString(): String {
        return "User(name='$name', email='$email', lat=$lat, long=$long)"
    }
}
