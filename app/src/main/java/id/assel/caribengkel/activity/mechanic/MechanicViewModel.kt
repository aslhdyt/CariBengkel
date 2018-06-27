package id.assel.caribengkel.activity.mechanic

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.LiveData
import android.content.pm.PackageManager
import android.os.Looper
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import com.google.android.gms.location.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import id.assel.caribengkel.model.Order
import id.assel.caribengkel.model.Workshop
import id.assel.caribengkel.model.WorkshopListLiveData
import id.assel.caribengkel.model.WorkshopLiveData

class MechanicViewModel(application: Application): AndroidViewModel(application), LifecycleObserver {
    var workshops = WorkshopListLiveData(application)
    val firestore = FirebaseFirestore.getInstance()
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(application)

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
        removeLocationUpdates()
        order.status = Order.ORDER_FINISH
        order.endAt = System.currentTimeMillis()
        firestore.document("order/${order.uuid}").set(order)
    }

    private var locationCallback : LocationCallback? = null
    @SuppressLint("MissingPermission")
    fun updatesMechanicPosition(activity: Activity, orderUuid: String) {
        if (locationCallback == null) {
            locationCallback = object : LocationCallback() {
                var i = 0
                override fun onLocationResult(locationResult: LocationResult?) {
                    if (locationResult != null) {
                        val location = locationResult.lastLocation
                        val point = GeoPoint(location.latitude, location.longitude)
                        println("location updated: $point")
                        firestore.document("order/$orderUuid").update("mechanicPosition", point).addOnCompleteListener {
                            i++
                            println("posting try: $i")
                        }
                    }
                }

                override fun onLocationAvailability(p0: LocationAvailability?) {
                    if (p0?.isLocationAvailable == true) {
                        checkLocationPermission(activity)
                    }
                }
            }
            fusedLocationClient.requestLocationUpdates(LOCATION_REQUEST, locationCallback, Looper.myLooper())
        }
    }

    private fun removeLocationUpdates() {
        if (locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback)
            locationCallback = null
        }
    }


    fun checkLocationPermission(activity: Activity) {
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity,
                            Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                AlertDialog.Builder(activity)
                        .setTitle("Location Permission Needed")
                        .setMessage("This app needs the Location permission, please accept to use location functionality")
                        .setPositiveButton("OK") { dialogInterface, i ->
                            //Prompt the user once explanation has been shown
                            ActivityCompat.requestPermissions(activity,
                                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                                    REQUEST_PERMISSIONS_LOCATION)
                        }
                        .create()
                        .show()


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(activity,
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                        REQUEST_PERMISSIONS_LOCATION)
            }
        }
    }


    companion object {
        val LOCATION_REQUEST = LocationRequest().setFastestInterval(2000L).setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY).setInterval(4000L)
        const val REQUEST_PERMISSIONS_LOCATION = 31
    }

}