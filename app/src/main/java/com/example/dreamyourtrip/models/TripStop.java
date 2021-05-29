package com.example.dreamyourtrip.models;

import android.os.Parcel;
import android.os.Parcelable;

public class TripStop implements
        Parcelable,
        Comparable<TripStop> {
    private String docID;
    private String ownerID;
    private String name;
    private double longitude;
    private double latitude;
    private String description;
    private double cost = 0;
    private int order;

    public TripStop() {
    }

    protected TripStop(Parcel in) {
        docID = in.readString();
        ownerID = in.readString();
        name = in.readString();
        longitude = in.readDouble();
        latitude = in.readDouble();
        description = in.readString();
        cost = in.readDouble();
        order = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(docID);
        dest.writeString(ownerID);
        dest.writeString(name);
        dest.writeDouble(longitude);
        dest.writeDouble(latitude);
        dest.writeString(description);
        dest.writeDouble(cost);
        dest.writeInt(order);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<TripStop> CREATOR = new Creator<TripStop>() {
        @Override
        public TripStop createFromParcel(Parcel in) {
            return new TripStop(in);
        }

        @Override
        public TripStop[] newArray(int size) {
            return new TripStop[size];
        }
    };

    public String getDocID() {
        return docID;
    }

    public void setDocID(String docID) {
        this.docID = docID;
    }

    public String getOwnerID() {
        return ownerID;
    }

    public void setOwnerID(String ownerID) {
        this.ownerID = ownerID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getCost() {
        return cost;
    }

    public void setCost(double cost) {
        this.cost = cost;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    @Override
    public String toString() {
        return "TripStop{" +
                "docID='" + docID + '\'' +
                ", ownerID='" + ownerID + '\'' +
                ", name='" + name + '\'' +
                ", longitude=" + longitude +
                ", latitude=" + latitude +
                ", description='" + description + '\'' +
                ", cost=" + cost +
                ", order=" + order +
                '}';
    }

    @Override
    public int compareTo(TripStop o) {
        if (order > o.order) {
            return 1;
        }
        else if (order <  o.order) {
            return -1;
        }
        else {
            return 0;
        }
    }
}