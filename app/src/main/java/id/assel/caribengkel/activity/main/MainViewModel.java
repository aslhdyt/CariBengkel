package id.assel.caribengkel.activity.main;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;

import id.assel.caribengkel.activity.main.data.WorkshopLiveData;

public class MainViewModel extends AndroidViewModel {
    public MainViewModel(Application application) {
        super(application);
    }

    WorkshopLiveData workshopLocation = new WorkshopLiveData(getApplication());
}
