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

class MainViewModel(application: Application) : AndroidViewModel(application) {

    var workshopLocation = WorkshopListLiveData(getApplication())
    lateinit var user: FirebaseUser
    var isUserCancelOrder = false

    fun postOrder(location: Location, callback: OrderCallback) {
        //todo find workshop

        val firestore = FirebaseFirestore.getInstance()

        //find available workshop
        callback.onProcessingOrder("mencari bengkel yang tersedia")
        firestore.collection("workshop").whereEqualTo("active", true).whereEqualTo("currentOrderUuid", null).get()
            .addOnCompleteListener {
                val e = it.exception
                if (e != null) {
                    callback.onFailure(e)
                    return@addOnCompleteListener
                }
                if (isUserCancelOrder) {
                    callback.onCanceled()
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
                            .addOnCompleteListener createOrder@{
                                val ex = it.exception
                                if (ex != null) {
                                    if (isUserCancelOrder) {
                                        callback.onCanceled()
                                    } else {
                                        requestOrderLoop()
                                    }
                                    return@createOrder
                                }
                                firestore.document("workshop/${value.id}")
                                    .update("currentOrderUuid", order.uuid)
                                    .addOnCompleteListener notifyMechanic@ {val
                                        exc = it.exception
                                        if (exc != null) {
                                            requestOrderLoop()
                                        } else {
                                            callback.onProcessingOrder("menunggu respon dari mekanik")
                                            //wait for workshop accepting
                                            if (isUserCancelOrder) {
                                                callback.onCanceled()
                                                return@notifyMechanic
                                            } else {
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
            }


    }

    fun cancelOrderRequest() {
        isUserCancelOrder = true
    }



    interface OrderCallback {
        fun onCanceled();
        fun onOrderPosted()
        fun onProcessingOrder(processMessage: String)
        fun onFailure(exception: Exception)
    }

}
