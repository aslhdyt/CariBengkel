package id.assel.caribengkel.activity.mechanic

import android.app.Application
import android.arch.lifecycle.*
import com.google.firebase.firestore.FirebaseFirestore
import id.assel.caribengkel.activity.main.data.WorkshopLiveData
import id.assel.caribengkel.model.Workshop

class MechanicViewModel(application: Application): AndroidViewModel(application), LifecycleObserver {
    var workshops = WorkshopLiveData(application)
    var selectedWorkshop: MutableLiveData<Workshop> = MutableLiveData()

//
//    var workshopObserver = object : Observer<Workshop> {
//        var lastActiveWorkshop: Workshop? = null
//        override fun onChanged(it: Workshop?) {
//            if (lastActiveWorkshop != null) {
//                println("TODO do some deactive workshop")
//            }
//
//            //init new active workshop
//            lastActiveWorkshop = it
//
//
//            if (it != null) {
//                println("active workshop now = ${it.name}")
//            } else {
//                println("inactive workshop")
//            }        }
//
//    }
//
//    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
//    fun initObserver() {
//        selectedWorkshop.observeForever(workshopObserver)
//    }
//
//    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
//    fun destroyViewModel() {
//        selectedWorkshop.removeObserver(workshopObserver)
//    }

    fun setActiveWorkshop(checked: Boolean, selectedWorkshop: Workshop?) {
        if (selectedWorkshop != null) {
            selectedWorkshop.isActive = checked
            FirebaseFirestore.getInstance().document("workshop/${selectedWorkshop.id}")
                    .set(selectedWorkshop)
        }

    }

}