package id.assel.caribengkel.activity.main

import android.app.Application
import android.arch.lifecycle.AndroidViewModel

import id.assel.caribengkel.activity.main.data.WorkshopLiveData

class MainViewModel(application: Application) : AndroidViewModel(application) {

    var workshopLocation = WorkshopLiveData(getApplication())


}
