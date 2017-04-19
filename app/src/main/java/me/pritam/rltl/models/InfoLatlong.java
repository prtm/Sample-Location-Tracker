package me.pritam.rltl.models;

/**
 * Created by ghost on 19/4/17.
 */

public class InfoLatlong {
    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    private double lat, lon;
}
