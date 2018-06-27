package id.assel.caribengkel.activity.main

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Transformations
import android.location.Location
import android.os.Handler
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.ListenerRegistration
import id.assel.caribengkel.model.Order
import id.assel.caribengkel.model.OrderByUserLiveData
import id.assel.caribengkel.model.Workshop
import id.assel.caribengkel.model.WorkshopListLiveData
import java.util.*

class MainViewModel(application: Application) : AndroidViewModel(application) {

    var workshopLocation = WorkshopListLiveData(getApplication())

    lateinit var listOrder: OrderByUserLiveData
    fun initUser(user : FirebaseUser) {
        listOrder = OrderByUserLiveData(getApplication(), user.uid)
    }


    fun onGoingOrder(srcLiveData: OrderByUserLiveData): LiveData<Order> {
        return Transformations.map(srcLiveData) {
            it.firstOrNull { it.status == Order.ORDER_ONGOING }
        }
    }



    var isUserCancelOrder = false
    fun postOrder(location: Location, callback: OrderCallback) {
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

                        val user: FirebaseUser = FirebaseAuth.getInstance().currentUser!!
                        val displayName = user.displayName
                        val username: String = if (!displayName.isNullOrEmpty()) displayName!! else user.email ?: ""
                        //create order
                        val order = Order(
                                uuid = UUID.randomUUID().toString(),
                                userUuid = user.uid,
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
                                                                    callback.onOrderAccepted(updatedOrder)
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
                                                            firestore.document("order/${order.uuid}").update("status", Order.ORDER_USER_CANCEL)
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

    fun filteredListOrder(): List<Order> {
        val orderList = listOrder.value
        return orderList?.filter { it.status == Order.ORDER_FINISH } ?: listOf()
    }

    fun cancelOrderRequest() {
        isUserCancelOrder = true
    }

    interface OrderCallback {
        fun onCanceled()
        fun onOrderAccepted(updatedOrder: Order)
        fun onProcessingOrder(processMessage: String)
        fun onFailure(exception: Exception)
    }

}
