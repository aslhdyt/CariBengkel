package id.assel.caribengkel.activity.main

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.location.Location
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import id.assel.caribengkel.model.Order
import id.assel.caribengkel.model.Workshop
import id.assel.caribengkel.model.WorkshopListLiveData
import java.util.*
import kotlin.collections.HashMap

class MainViewModel(application: Application) : AndroidViewModel(application) {

    var workshopLocation = WorkshopListLiveData(getApplication())
    lateinit var user: FirebaseUser

    fun postOrder(location: Location, callback: OrderCallback) {
        //todo find workshop

        val firestore = FirebaseFirestore.getInstance()

        //find available workshop
        firestore.collection("workshop").whereEqualTo("active", true).whereEqualTo("currentOrderUuid", null)
                .get()
                .addOnCompleteListener {
                    val e = it.exception
                    if (e != null) {
                        callback.onFailure(e)
                        return@addOnCompleteListener
                    }

                    val workshops = it.result?.toObjects(Workshop::class.java)
                    if (workshops == null || workshops.isEmpty()) {
                        callback.onFailure(Exception("bengkel tidak tersedia"))
                        return@addOnCompleteListener
                    }

                    val iterator = workshops.iterator()

                    fun requestOrderLoop() {
                        if (iterator.hasNext()) {
                            val value = iterator.next()

                            //create order
                            val order = Order(
                                    uuid = UUID.randomUUID().toString(),
                                    userUuid = user.uid,
                                    location = GeoPoint(location.latitude, location.longitude),
                                    workshopId = value.id
                            )
                            //post order
                            firestore.document("order/${order.uuid}")
                                    .set(order)
                                    .addOnCompleteListener {
                                        val ex = it.exception
                                        if (ex != null) {
                                            requestOrderLoop()
                                        } else {

                                            firestore.document("workshop/${value.id}")
                                                .update("currentOrderUuid", order.uuid)
                                                .addOnCompleteListener {val
                                                        exc = it.exception
                                                        if (exc != null) {
                                                            requestOrderLoop()
                                                        } else {
                                                            //wait for workshop accepting
                                                            callback.onOrderPosted()
                                                        }
                                                }
                                        }


                                    }
                        } else {
                            callback.onFailure(Exception("bengkel tidak tersedia"))
                        }

                    }
                    requestOrderLoop()




//                    //create order
//                    val order = Order(
//                            uuid = UUID.randomUUID().toString(),
//                            userUuid = user.uid,
//                            location = GeoPoint(location.latitude, location.longitude)
//                    )
//                    //post order
//                    firestore.document("order/${order.uuid}")
//                            .set(order)
//                            .addOnCompleteListener {
//                                val e = it.exception
//                                if (e != null) {
//                                    callback.onFailure(e)
//                                    return@addOnCompleteListener
//                                }
//                                callback.onOrderPosted()
//                            }



                }


    }



    interface OrderCallback {
        fun onOrderPosted()
        fun onProcessingOrder(processMessage: String)
        fun onFailure(exception: Exception)
    }

}
