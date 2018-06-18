package id.assel.caribengkel.tools;

import android.app.Activity;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

import java.util.ArrayList;
import java.util.List;

import id.assel.caribengkel.model.Workshop;

public class Debug {

    public static void debugSetLocation(Activity activity) {
        //TODO set bengkel location
        FirebaseFirestore FR = FirebaseFirestore.getInstance();

        List<GeoPoint> points = new ArrayList<>();

        points.add(new GeoPoint(-6.233067,107.001396));
        points.add(new GeoPoint(-6.232753,107.001412));
        points.add(new GeoPoint(-6.231650,107.002124));
        points.add(new GeoPoint(-6.233511,107.001236));
        points.add(new GeoPoint(-6.233480,107.001314));
        points.add(new GeoPoint(-6.230732,107.002803));
        points.add(new GeoPoint(-6.230988,107.002761));
        points.add(new GeoPoint(-6.231378,107.003367));
        points.add(new GeoPoint(-6.233218,107.005171));
        points.add(new GeoPoint(-6.229706,107.003802));
        points.add(new GeoPoint(-6.227570,107.005986));
        points.add(new GeoPoint(-6.222468,107.003570));
        points.add(new GeoPoint(-6.222626,107.003632));
        points.add(new GeoPoint(-6.2234061, 107.0122188));
        points.add(new GeoPoint(-6.2232044, 107.0116244));
        points.add(new GeoPoint(-6.2324468, 106.9995118));
        points.add(new GeoPoint(-6.2306216, 106.9966858));
        points.add(new GeoPoint(-6.2216409, 106.9968088));

        int i = 1;
        for(GeoPoint point : points) {
            final int finalI = i;

            Workshop data = new Workshop();
            data.setLatLng(point);
            data.setId(finalI);
            data.setName("Bengkel "+finalI);
            data.setCurrentOrderUuid(null);
            FR.document("workshop/"+i).set(data).addOnSuccessListener(activity, new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    System.out.println("added workshop "+ finalI);
                }
            });
            i++;
        }
    }
}
