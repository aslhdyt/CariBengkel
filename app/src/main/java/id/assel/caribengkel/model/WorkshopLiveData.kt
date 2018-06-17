package id.assel.caribengkel.model

import android.arch.lifecycle.LiveData
import android.content.Context
import android.widget.Toast
import com.google.firebase.firestore.*

class WorkshopLiveData(val context: Context, id: Int) : LiveData<Workshop>() {
    private val reference = FirebaseFirestore.getInstance().document("workshop/$id")
    private var listenerRegistration: ListenerRegistration? = null

    override fun onActive() {
        super.onActive()

        listenerRegistration = reference.addSnapshotListener(EventListener<DocumentSnapshot> { snapshot, e ->
            if (e != null) {
                e.printStackTrace()
                Toast.makeText(context, "Failed to get workshop data", Toast.LENGTH_SHORT).show()
                return@EventListener
            }

            if (snapshot != null) {
                val value = snapshot.toObject(Workshop::class.java)
                setValue(value)
            } else
                value = null
        })

    }

    override fun onInactive() {
        listenerRegistration?.remove()
    }
}
