package id.assel.caribengkel.model

import android.arch.lifecycle.LiveData
import android.content.Context
import android.widget.Toast
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class OrderLiveData(val context: Context, uuid: String): LiveData<Order>() {
    private val reference = FirebaseFirestore.getInstance().document("order/$uuid")
    private var listenerRegistration: ListenerRegistration? = null

    override fun onActive() {
        super.onActive()

        listenerRegistration = reference.addSnapshotListener(EventListener<DocumentSnapshot> { snapshot, e ->
            if (e != null) {
                e.printStackTrace()
                Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
                return@EventListener
            }

            if (snapshot != null) {
                val value = snapshot.toObject(Order::class.java)
                setValue(value)
            } else
                value = null
        })

    }

    override fun onInactive() {
        listenerRegistration?.remove()
    }
}