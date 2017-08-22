package com.gumbley.jonathon.findmeaplace;

import android.graphics.Bitmap;
import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by jonat on 20/05/2017.
 */

public class PlaceItem {

    private String mTitle, mAddress, mImageUrl;
    private LatLng mLocation;
    private float mDistanceFromUserLocation;

    public PlaceItem(String title, LatLng location, String imageUrl, String address){
        mTitle = title;
        mLocation = location;
        mImageUrl = imageUrl;
        mAddress = address;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) { mTitle = title; }

    public LatLng getLocation() {
        return mLocation;
    }

    public void setLocation(LatLng location) { mLocation = location; }

    public String getAddress() {return mAddress; }

    public void setAddress(String address) { mAddress = address; }

    public String getImageUrl() { return mImageUrl; }

    public void setImageUrl(String imageUrl) { mImageUrl = imageUrl; }

    public float getDistanceFrom() { return mDistanceFromUserLocation; }

    public void setDistanceFromUserLocation(LatLng userLocation){
        float[] results = new float[1];
        Location.distanceBetween(userLocation.latitude, userLocation.longitude, mLocation.latitude, mLocation.longitude, results);
        mDistanceFromUserLocation = results[0];
    }

}
