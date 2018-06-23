package id.assel.caribengkel.model

import android.arch.lifecycle.LiveData
import android.content.Context
import android.widget.Toast
import com.google.firebase.firestore.*

class OrderByUserLiveData(val context: Context, userUuid: String): LiveData<List<Order>>() {
    private val reference = FirebaseFirestore.getInstance().collection("order").whereEqualTo("userUuid", userUuid)
    private var listenerRegistration: ListenerRegistration? = null

    override fun onActive() {
        super.onActive()


        listenerRegistration = reference.addSnapshotListener(EventListener<QuerySnapshot> { snapshot, e ->
            if (e != null) {
                e.printStackTrace()
                Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
                return@EventListener
            }

            if (snapshot != null) {
                val value = snapshot.toObjects(Order::class.java)
                setValue(value)
            } else
                value = null
        })

    }

    override fun onInactive() {
        listenerRegistration?.remove()
    }
}