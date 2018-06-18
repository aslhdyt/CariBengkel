package id.assel.caribengkel.activity.mechanic

import android.app.Application
import android.arch.lifecycle.*
import com.google.firebase.firestore.FirebaseFirestore
import id.assel.caribengkel.model.WorkshopListLiveData
import id.assel.caribengkel.model.Workshop
import id.assel.caribengkel.model.WorkshopLiveData

class MechanicViewModel(application: Application): AndroidViewModel(application), LifecycleObserver {
    var workshops = WorkshopListLiveData(application)

    fun setActiveWorkshop(checked: Boolean, selectedWorkshop: Workshop?) {
        if (selectedWorkshop != null) {
            selectedWorkshop.active = checked
            selectedWorkshop.currentOrderUuid = null
            FirebaseFirestore.getInstance().document("workshop/${selectedWorkshop.id}")
                    .set(selectedWorkshop)
        }

    }

    fun getWorkshopRepository(workshop: Workshop): LiveData<Workshop>? {
        return WorkshopLiveData(getApplication(), workshop.id)
    }

    fun acceptJob() {
        println("job accepted, TODO notify user")
        //TODO notify user
    }

    fun rejectJob() {
        println("job rejected, user find another mechanic")
    }

}