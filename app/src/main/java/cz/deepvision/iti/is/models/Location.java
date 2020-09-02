package cz.deepvision.iti.is.models;

import io.realm.RealmObject;

public class Location extends RealmObject {
    private Double lat;
    private Double lng;

    public Location() {
    }

    public Location(Double lat, Double lng) {
        this.lat = lat;
        this.lng = lng;
    }

    public Location(double[] locations) {
        this.lat = locations[0];
        this.lng = locations[1];
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLng() {
        return lng;
    }

    public void setLng(Double lng) {
        this.lng = lng;
    }
}
