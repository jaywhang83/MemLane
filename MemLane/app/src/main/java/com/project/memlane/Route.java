package com.project.memlane;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.HashMap;

// Class that holds the file path to firstPicture, all of the saved picture file paths, locations
// of pictures, and route locations
public class Route implements Parcelable {
    private String firstPicture;
    private HashMap<String, LatLng> pictureLocations;
    private ArrayList<LatLng> routes;

    public Route(String firstPicture, HashMap<String, LatLng> pictureLocations, ArrayList<LatLng> routes) {
        this.firstPicture = firstPicture;
        this.pictureLocations = pictureLocations;
        this.routes = routes;
    }

    // Parcelable constructor
    public Route(Parcel source) {
        this.firstPicture = source.readString();
        this.pictureLocations = source.readHashMap(LatLng.class.getClassLoader());
        this.routes = source.readArrayList(LatLng.class.getClassLoader());
    }

    // Returns the firstPicture
    public String getFirstPicture() {
        return this.firstPicture;
    }

    // Returns pictureLocations
    public HashMap<String, LatLng> getPictureLocations() {
        return this.pictureLocations;
    }

    // Returns the routes
    public ArrayList<LatLng> getRoutes() {
        return this.routes;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    // Writes all the route data as parcelable. Need this for when we save Route as Parcelable to
    // send it over to another activity to use.
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(firstPicture);
        dest.writeMap(pictureLocations);
        dest.writeList(routes);
    }

    // Creates Parcelable
    public static final Parcelable.Creator<Route> CREATOR = new Parcelable.Creator<Route>() {

        public Route createFromParcel(Parcel source) {
            return new Route(source);
        }

        @Override
        public Route[] newArray(int size) {
            return new Route[size];
        }
    };
}
