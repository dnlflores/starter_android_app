package com.example.starter.model;

import android.os.Parcel;
import android.os.Parcelable;

public class AddressResult implements Parcelable {
    private String title;
    private String subtitle;
    private double latitude;
    private double longitude;
    private String fullAddress;

    public AddressResult(String title, String subtitle, double latitude, double longitude, String fullAddress) {
        this.title = title;
        this.subtitle = subtitle;
        this.latitude = latitude;
        this.longitude = longitude;
        this.fullAddress = fullAddress;
    }

    protected AddressResult(Parcel in) {
        title = in.readString();
        subtitle = in.readString();
        latitude = in.readDouble();
        longitude = in.readDouble();
        fullAddress = in.readString();
    }

    public static final Creator<AddressResult> CREATOR = new Creator<AddressResult>() {
        @Override
        public AddressResult createFromParcel(Parcel in) {
            return new AddressResult(in);
        }

        @Override
        public AddressResult[] newArray(int size) {
            return new AddressResult[size];
        }
    };

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getFullAddress() {
        return fullAddress;
    }

    public void setFullAddress(String fullAddress) {
        this.fullAddress = fullAddress;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(subtitle);
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
        dest.writeString(fullAddress);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public String toString() {
        return "AddressResult{" +
                "title='" + title + '\'' +
                ", subtitle='" + subtitle + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", fullAddress='" + fullAddress + '\'' +
                '}';
    }
} 