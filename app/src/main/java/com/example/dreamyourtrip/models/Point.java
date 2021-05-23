package com.example.dreamyourtrip.models;

import android.os.Parcel;
import android.os.Parcelable;

public class Point implements Parcelable {

    private String docID;
    private String name;
    private double longitude;
    private double latitude;
    private String ownerID;

    public Point() {
    }

    public static final Creator<Point> CREATOR = new Creator<Point>() {
        @Override
        public Point createFromParcel(Parcel in) {
            return new Point(in);
        }

        @Override
        public Point[] newArray(int size) {
            return new Point[size];
        }
    };

    public String getDocID() {
        return docID;
    }

    public void setDocID(String docID) {
        this.docID = docID;
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

    public String getOwnerID() {
        return ownerID;
    }

    public void setOwnerID(String ownerID) {
        this.ownerID = ownerID;
    }

    @Override
    public String toString() {
        return "Point{" +
                "docID='" + docID + '\'' +
                ", name='" + name + '\'' +
                ", longitude=" + longitude +
                ", latitude=" + latitude +
                ", ownerID='" + ownerID + '\'' +
                '}';
    }

    public Point(Parcel source) {
        docID = source.readString();
        name = source.readString();
        longitude = source.readDouble();
        latitude = source.readDouble();
        ownerID = source.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(docID);
        dest.writeString(name);
        dest.writeDouble(longitude);
        dest.writeDouble(latitude);
        dest.writeString(ownerID);
    }
}