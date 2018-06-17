package id.assel.caribengkel.model;

import android.arch.lifecycle.LiveData;
import android.content.Context;
import android.widget.Toast;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;

import id.assel.caribengkel.model.Workshop;

public class WorkshopListLiveData extends LiveData<List<Workshop>> {
    private CollectionReference reference = FirebaseFirestore.getInstance().collection("workshop");
    private ListenerRegistration listenerRegistration;
    private Context context;
    public WorkshopListLiveData(Context context) {
        this.context = context;
    }
    @Override
    protected void onActive() {
        super.onActive();

        listenerRegistration = reference.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @javax.annotation.Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    e.printStackTrace();
                    Toast.makeText(context, "Failed to get mechanic data", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (queryDocumentSnapshots != null) {
                    List<Workshop> value = queryDocumentSnapshots.toObjects(Workshop.class);
                    setValue(value);
                } else setValue(null);
            }
        });

    }

    @Override
    protected void onInactive() {
        listenerRegistration.remove();
    }
}
