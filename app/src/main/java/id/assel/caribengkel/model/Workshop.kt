package id.assel.caribengkel.model

import com.google.firebase.firestore.GeoPoint

data class Workshop constructor(
    var id: Int,
    var latLng: GeoPoint,
    var name: String,
    var active: Boolean,
    var profile: Profile? = null
) {
    var currentOrderUuid: String? = null


    //empty constructor for firestore
    constructor(): this(0, GeoPoint(0.0,0.0), "", false, Profile())

    override fun toString(): String {
        return name
    }
}
