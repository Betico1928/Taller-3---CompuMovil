package javeriana.edu.co.taller3_compumovil.pojos



data class RTDUser(
    val codigo: String,
    val disponible: Boolean,
    val email: String,
    val lat: Double,
    val long: Double,
    val name: String
) {
    constructor() : this("", false, "", 0.0, 0.0, "")
}