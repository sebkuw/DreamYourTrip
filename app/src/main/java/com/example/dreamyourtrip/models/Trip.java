package com.example.dreamyourtrip.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

public class Trip implements Parcelable {
    private String docID;
    private String name;
    private String ownerID;
    private String transportType;
    private ArrayList<TripStop> tripStops = new ArrayList<>();
    private String description;
    private double actualMoney;
    private double costSum = 0;

    public Trip() {
    }

    protected Trip(Parcel in) {
        docID = in.readString();
        name = in.readString();
        ownerID = in.readString();
        tripStops = in.createTypedArrayList(TripStop.CREATOR);
        description = in.readString();
        actualMoney = in.readDouble();
        costSum = in.readDouble();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(docID);
        dest.writeString(name);
        dest.writeString(ownerID);
        dest.writeTypedList(tripStops);
        dest.writeString(description);
        dest.writeDouble(actualMoney);
        dest.writeDouble(costSum);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Trip> CREATOR = new Creator<Trip>() {
        @Override
        public Trip createFromParcel(Parcel in) {
            return new Trip(in);
        }

        @Override
        public Trip[] newArray(int size) {
            return new Trip[size];
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

    public String getOwnerID() {
        return ownerID;
    }

    public void setOwnerID(String ownerID) {
        this.ownerID = ownerID;
    }

    public String getTransportType() {
        return transportType;
    }

    public void setTransportType(String transportType) {
        this.transportType = transportType;
    }

    public ArrayList<TripStop> getTripStops() {
        return tripStops;
    }

    public void setTripStops(ArrayList<TripStop> tripStops) {
        double sum = 0;
        for (TripStop tripStop: tripStops) {
            sum += tripStop.getCost();
        }
        this.costSum = sum;
        this.tripStops = tripStops;
    }

    public void addTripStop(TripStop tripStop) {
        this.tripStops.add(tripStop);
        tripStop.setOrder(this.tripStops.size());
        this.costSum += tripStop.getCost();
    }

    public void deleteTripStop(TripStop tripStop) {
        for (int i = 0; i < tripStops.size(); i++) {
            if(tripStops.get(i).getOrder() > tripStop.getOrder()) {
                tripStops.get(i).setOrder(i-1);
            }
        }
        this.tripStops.remove(tripStop);
        this.costSum -= tripStop.getCost();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getActualMoney() {
        return actualMoney;
    }

    public void setActualMoney(double actualMoney) {
        this.actualMoney = actualMoney;
    }

    public double getCostSum() {
        return costSum;
    }

    public void setCostSum(double costSum) {
        this.costSum = costSum;
    }

    @Override
    public String toString() {
        return name;
    }
}