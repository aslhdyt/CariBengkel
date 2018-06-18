package id.assel.caribengkel.activity.mechanic

import android.app.Application
import android.arch.lifecycle.*
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore
import id.assel.caribengkel.model.Order
import id.assel.caribengkel.model.WorkshopListLiveData
import id.assel.caribengkel.model.Workshop
import id.assel.caribengkel.model.WorkshopLiveData

class MechanicViewModel(application: Application): AndroidViewModel(application), LifecycleObserver {
    var workshops = WorkshopListLiveData(application)
    val firestore = FirebaseFirestore.getInstance()

    fun setActiveWorkshop(checked: Boolean, selectedWorkshop: Workshop?) {
        if (selectedWorkshop != null) {
            selectedWorkshop.active = checked
            FirebaseFirestore.getInstance().document("workshop/${selectedWorkshop.id}")
                    .set(selectedWorkshop)
        }

    }

    fun getWorkshopRepository(workshop: Workshop): LiveData<Workshop>? {
        return WorkshopLiveData(getApplication(), workshop.id)
    }

    fun acceptJob(order: Order) {
        println("job accepted, TODO notify user")
        //TODO notify user
    }

    fun rejectJob(order: Order) {
        println("job rejected, user find another mechanic")
        val map = hashMapOf<String, Any?>()
        map.put("currentOrderUuid", null)
        firestore.document("workshop/${order.workshopId}")
                .update("currentOrderUuid", null)
                .addOnCompleteListener {
                    order.status = Order.ORDER_MECHANIC_CANCEL
                    order.endAt = System.currentTimeMillis()
                    firestore.document("order/${order.uuid}").set(order)
                }
    }

}