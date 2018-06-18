package id.assel.caribengkel.model

import com.google.firebase.firestore.GeoPoint

data class Order (
        val uuid: String,
        val userUuid: String,
        val username: String,
        var workshopId: Int? = null,
        val createdAt: Long = System.currentTimeMillis(),
        var endAt: Long? = null,
        val location: GeoPoint,
        var status: String = ORDER_PENDING
) {
    //empty constructor required for firestore
    constructor(): this("", "","",0, 0L, 0L, GeoPoint(0.0,0.0), "")
    companion object {
        const val ORDER_PENDING ="PENDING"
        const val ORDER_ONGOING = "ONGOING"
        const val ORDER_FINISH = "FINISH"
        const val ORDER_USER_CANCEL = "USER_CANCEL"
        const val ORDER_MECHANIC_CANCEL = "MECHANIC_CANCEL"
    }
}