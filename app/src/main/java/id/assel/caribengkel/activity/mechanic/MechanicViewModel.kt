package id.assel.caribengkel.activity.mechanic

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import id.assel.caribengkel.activity.main.data.WorkshopLiveData

class MechanicViewModel(application: Application): AndroidViewModel(application) {
    var workshops = WorkshopLiveData(application)
}