package id.assel.caribengkel.activity.main

import android.app.Application
import android.arch.lifecycle.AndroidViewModel

import id.assel.caribengkel.model.WorkshopListLiveData

class MainViewModel(application: Application) : AndroidViewModel(application) {

    var workshopLocation = WorkshopListLiveData(getApplication())


}
