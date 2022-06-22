package com.example.events;

import android.graphics.Bitmap;
import android.location.Address;
import android.location.Location;
import android.media.Image;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.database.DataSnapshot;

import java.io.Serializable;
import java.time.LocalDateTime;

public class Event implements Comparable<Event>{
    String id;

    public String getImageString() {
        return imageString;
    }

    public void setImageString(String imageString) {
        this.imageString = imageString;
    }

    public Event(){

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Person getAddedBy() {
        return addedBy;
    }

    public void setAddedBy(Person addedBy) {
        this.addedBy = addedBy;
    }

    public Bitmap getImage() {
        return image;
    }

    public void setImage(Bitmap image) {
        this.image = image;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public LocalDateTime getAddDateTime() {
        return addDateTime;
    }

    public void setAddDateTime(LocalDateTime addDateTime) {
        this.addDateTime = addDateTime;
    }

    String name;
    Double latitude;
    Double longitude;
    Person addedBy;
    Bitmap image;
    String note;
    LocalDateTime addDateTime;
    String address;
    String imageString;

    public String getImageSerialised() {
        return imageSerialised;
    }

    public void setImageSerialised(String imageSerialised) {
        this.imageSerialised = imageSerialised;
    }

    String imageSerialised;

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }



    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Event(String id, String name, Double latitude, Double longitude, Person addedBy, Bitmap image, String note, LocalDateTime addDateTime, String address, String imageString) {
        this.id = id;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.addedBy = addedBy;
        this.image = image;
        this.note = note;
        this.addDateTime = addDateTime;
        this.address = address;
        this.imageString = imageString;
    }

    @Override
    public String toString() {
        return "Event{" +
                "name='" + name + '\'' +
                '}';
    }
    @Override
    public int compareTo(Event event) {
        if(this.getAddDateTime().isBefore(event.getAddDateTime())){
            System.out.println(event.getName() + " is After");
            return 1;
        }
        else if(this.getAddDateTime().isAfter(event.getAddDateTime())){
            System.out.println(event.getName() + " is Before");
            return -1;
        }
        else return 0;
    }
}
