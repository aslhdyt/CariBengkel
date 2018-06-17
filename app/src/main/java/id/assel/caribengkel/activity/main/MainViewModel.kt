package id.assel.caribengkel.activity.main

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.location.Location
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import id.assel.caribengkel.model.Order

import id.assel.caribengkel.model.WorkshopListLiveData
import java.lang.Exception
import java.util.*

class MainViewModel(application: Application) : AndroidViewModel(application) {

    var workshopLocation = WorkshopListLiveData(getApplication())
    lateinit var user: FirebaseUser

    fun postOrder(location: Location, callback: UserActivivityCallback) {
        //todo find workshop

        //create order
        val order = Order(
                uuid = UUID.randomUUID().toString(),
                userUuid = user.uid,
                location = GeoPoint(location.latitude, location.longitude)
                )
        //post order
        FirebaseFirestore.getInstance()
                .document("order/${order.uuid}")
                .set(order)
                .addOnCompleteListener {
                    val e = it.exception
                    if (e != null) {
                        callback.onFailure(e)
                        return@addOnCompleteListener
                    }
                    callback.onOrderPosted()
                }
    }

    interface UserActivivityCallback {
        fun onOrderPosted()
        fun onFailure(exception: Exception)
    }

}
