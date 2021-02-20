package ua.int20h.sodd_warriors.ar_task.utils;

/**
 * Created by Amal Krishnan on 08-05-2017.
 */

public class LatLng {

    private final double lat;
    private final double lng;

    public LatLng(double lat, double lng) {
        this.lat = lat;
        this.lng = lng;
    }

    public double getLat() {
        return lat;
    }

    public double getLng() {
        return lng;
    }
}
