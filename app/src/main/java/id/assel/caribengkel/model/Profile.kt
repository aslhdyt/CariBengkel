package id.assel.caribengkel.model

data class Profile (
    val name: String,
    val address: String,
    val phoneNumber: String
    ) {
    var photoUrl: String? = null
    //empty constructor for firestore
    constructor(): this("","","")
}