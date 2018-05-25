package id.assel.caribengkel

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData

class MainViewModel(application: Application): AndroidViewModel(application) {
    val test: LiveData<String> = MutableLiveData<String>()
}