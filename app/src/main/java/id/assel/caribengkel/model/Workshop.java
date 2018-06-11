package id.assel.caribengkel.model;

import com.google.firebase.firestore.GeoPoint;

public class Workshop {
    public GeoPoint latLng;
    public String name;

    public GeoPoint getLatLng() {
        return latLng;
    }

    public void setLatLng(GeoPoint latLng) {
        this.latLng = latLng;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
