package com.spilab.percom21.database;

import java.util.Date;

import io.realm.RealmObject;

public class LocationBeanRealm extends RealmObject {

    public LocationBeanRealm() {
    }

    public LocationBeanRealm(Double lat, Double lng, Date timestamp) {
        this.lat = lat;
        this.lng = lng;
        this.timestamp = timestamp;
    }

    private Double lat;

    private Double lng;

    private Date timestamp;

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

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date date) {
        this.timestamp = date;
    }

    @Override
    public String toString() {
        return "LocationBeanRealm{" +
                "lat=" + lat +
                ", lng=" + lng +
                ", timestamp='" + timestamp + '\'' +
                '}';
    }
}
