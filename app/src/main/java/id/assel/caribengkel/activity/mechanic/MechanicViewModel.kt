package id.assel.caribengkel.activity.mechanic

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.LiveData
import com.google.firebase.firestore.FirebaseFirestore
import id.assel.caribengkel.model.Order
import id.assel.caribengkel.model.Workshop
import id.assel.caribengkel.model.WorkshopListLiveData
import id.assel.caribengkel.model.WorkshopLiveData

class MechanicViewModel(application: Application): AndroidViewModel(application), LifecycleObserver {
    var workshops = WorkshopListLiveData(application)
    val firestore = FirebaseFirestore.getInstance()

    fun setActiveWorkshop(checked: Boolean, selectedWorkshop: Workshop?) {
        if (selectedWorkshop != null) {
            selectedWorkshop.active = checked
            if (!checked) selectedWorkshop.currentOrderUuid = null
            FirebaseFirestore.getInstance().document("workshop/${selectedWorkshop.id}")
                    .set(selectedWorkshop)
        }
    }

    fun getWorkshopRepository(workshop: Workshop): LiveData<Workshop>? {
        return WorkshopLiveData(getApplication(), workshop.id)
    }



    fun acceptJob(order: Order) {
        println("job accepted, TODO notify user")
        /*checkOrder*/
        order.status = Order.ORDER_ONGOING
        firestore.document("order/${order.uuid}").set(order)
    }

    fun rejectJob(order: Order) {
        println("job rejected, user find another mechanic")
        val map = hashMapOf<String, Any?>()
        map["currentOrderUuid"] = null
        firestore.document("workshop/${order.workshopId}")
                .update("currentOrderUuid", null)
                .addOnCompleteListener {
                    order.status = Order.ORDER_MECHANIC_CANCEL
                    order.endAt = System.currentTimeMillis()
                    firestore.document("order/${order.uuid}").set(order)
                }
    }

    fun finishOrder(order: Order) {
        order.status = Order.ORDER_FINISH
        order.endAt = System.currentTimeMillis()
        firestore.document("order/${order.uuid}").set(order)
    }

}