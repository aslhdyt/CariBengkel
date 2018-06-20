package id.assel.caribengkel.activity.main

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.location.Location
import android.os.Handler
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.*
import com.google.firebase.firestore.EventListener
import id.assel.caribengkel.model.Order
import id.assel.caribengkel.model.Workshop
import id.assel.caribengkel.model.WorkshopListLiveData
import java.util.*

class MainViewModel(application: Application) : AndroidViewModel(application) {

    var workshopLocation = WorkshopListLiveData(getApplication())
    lateinit var user: FirebaseUser
    var isUserCancelOrder = false

    fun postOrder(user: FirebaseUser, location: Location, callback: OrderCallback) {
        isUserCancelOrder = false

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

                        val displayName = user.displayName
                        val username: String = if (!displayName.isNullOrEmpty()) displayName!! else user.email ?: ""
                        //create order
                        val order = Order(
                                uuid = UUID.randomUUID().toString(),
                                userUuid = this.user.uid,
                                location = GeoPoint(location.latitude, location.longitude),
                                workshopId = value.id,
                                username = username
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
                                                var registrationListener: ListenerRegistration? = null
                                                val stopListen = Runnable {
                                                    if (registrationListener != null) {
                                                        println("listener removed")
                                                        registrationListener?.remove()
                                                    } else {
                                                        println("listener failed to remove")
                                                    }
                                                }

                                                registrationListener = firestore.document("order/${order.uuid}")
                                                    .addSnapshotListener (EventListener<DocumentSnapshot> { documentSnapshot, firebaseFirestoreException ->
                                                        println("snapshotListener fired")
                                                        if (isUserCancelOrder) {
                                                            order.status = Order.ORDER_USER_CANCEL
                                                            order.endAt = System.currentTimeMillis()
                                                            FirebaseFirestore.getInstance().document("order/${order.uuid}").set(order)
                                                            callback.onCanceled()
                                                            return@EventListener
                                                        }
                                                        if (firebaseFirestoreException != null) {
                                                            //ignore the exception
                                                            firebaseFirestoreException.printStackTrace()
                                                            println("TODO handle error")
                                                        }
                                                        val updatedOrder = documentSnapshot?.toObject(Order::class.java)
                                                        if (updatedOrder !=null) {
                                                            println("order exist, status: ${updatedOrder.status}")
                                                            when (updatedOrder.status) {
                                                                Order.ORDER_ONGOING -> {
                                                                    println("order accepted")
                                                                    callback.onOrderAccepted()
                                                                    stopListen.run()
                                                                }
                                                                Order.ORDER_MECHANIC_CANCEL -> {
                                                                    println("mechanic rejecting the job")
                                                                    requestOrderLoop()
                                                                    stopListen.run()
                                                                }
                                                            }
                                                        } else {
                                                            requestOrderLoop()
                                                        }
                                                    })

                                                //listen cancellation
                                                val mHandler= Handler()
                                                mHandler.post(object: Runnable {
                                                    override fun run() {
                                                        if (isUserCancelOrder) {
                                                            stopListen.run()
                                                            callback.onCanceled()
                                                        } else {
                                                            mHandler.postDelayed(this, 1000)
                                                        }
                                                    }
                                                })

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
        fun onCanceled()
        fun onOrderAccepted()
        fun onProcessingOrder(processMessage: String)
        fun onFailure(exception: Exception)
    }

}
