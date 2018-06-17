package id.assel.caribengkel.model

import com.google.firebase.firestore.GeoPoint

data class Workshop (
    var id: Int,
    var latLng: GeoPoint,
    var name: String,
    var active: Boolean
) {
    //empty constructor for firestore
    constructor(): this(0, GeoPoint(0.0,0.0), "", false)

    override fun toString(): String {
        return name
    }
}
