package com.example.zeeta;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.ServerTimestamp;

public class JourneyInfo implements Parcelable {

    public static final Creator<JourneyInfo> CREATOR = new Creator<JourneyInfo>() {
        @Override
        public JourneyInfo createFromParcel(Parcel in) {
            return new JourneyInfo(in);
        }

        @Override
        public JourneyInfo[] newArray(int size) {
            return new JourneyInfo[size];
        }
    };
    private GeoPoint pickupLocation;
    private GeoPoint destination;
    private String customerID;
    private String customerPhoneNumber;
    private Long distanceCovered;
    private Long amount;
    private Boolean accepted;
    private Boolean started;
    private Boolean ended;
    private @ServerTimestamp
    Timestamp timeStamp;

    protected JourneyInfo(Parcel in) {
        customerID = in.readString();
        customerPhoneNumber = in.readString();
        if (in.readByte() == 0) {
            distanceCovered = null;
        } else {
            distanceCovered = in.readLong();
        }
        if (in.readByte() == 0) {
            amount = null;
        } else {
            amount = in.readLong();
        }
        byte tmpAccepted = in.readByte();
        accepted = tmpAccepted == 0 ? null : tmpAccepted == 1;
        byte tmpStarted = in.readByte();
        started = tmpStarted == 0 ? null : tmpStarted == 1;
        byte tmpEnded = in.readByte();
        ended = tmpEnded == 0 ? null : tmpEnded == 1;
        timeStamp = in.readParcelable(Timestamp.class.getClassLoader());
    }

    public JourneyInfo(GeoPoint pickupLocation, GeoPoint destination, String driverName, String customerPhoneNumber, Long distanceCovered, Timestamp timeStamp, Long amount, Boolean accepted, Boolean started, Boolean ended) {
        this.pickupLocation = pickupLocation;
        this.destination = destination;
        this.customerID = driverName;
        this.customerPhoneNumber = customerPhoneNumber;
        this.distanceCovered = distanceCovered;
        this.timeStamp = timeStamp;
        this.amount = amount;
        this.accepted = accepted;
        this.started = started;
        this.ended = ended;
    }

    public Long getAmount() {
        return amount;
    }

    public void setAmount(Long amount) {
        this.amount = amount;
    }

    public Boolean getAccepted() {
        return accepted;
    }

    public void setAccepted(Boolean accepted) {
        this.accepted = accepted;
    }

    public Boolean getStarted() {
        return started;
    }

    public void setStarted(Boolean started) {
        this.started = started;
    }

    public Boolean getEnded() {
        return ended;
    }

    public void setEnded(Boolean ended) {
        this.ended = ended;
    }

    public GeoPoint getPickupLocation() {
        return pickupLocation;
    }

    public void setPickupLocation(GeoPoint pickupLocation) {
        this.pickupLocation = pickupLocation;
    }

    public GeoPoint getDestination() {
        return destination;
    }

    public void setDestination(GeoPoint destination) {
        this.destination = destination;
    }

    public String getCustomerID() {
        return customerID;
    }

    public void setCustomerID(String customerID) {
        this.customerID = customerID;
    }

    public String getCustomerPhoneNumber() {
        return customerPhoneNumber;
    }

    public void setCustomerPhoneNumber(String customerPhoneNumber) {
        this.customerPhoneNumber = customerPhoneNumber;
    }

    public Long getDistanceCovered() {
        return distanceCovered;
    }

    public void setDistanceCovered(Long distanceCovered) {
        this.distanceCovered = distanceCovered;
    }

    public Timestamp getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Timestamp timeStamp) {
        this.timeStamp = timeStamp;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(customerID);
        dest.writeString(customerPhoneNumber);
        if (distanceCovered == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(distanceCovered);
        }
        if (amount == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(amount);
        }
        dest.writeByte((byte) (accepted == null ? 0 : accepted ? 1 : 2));
        dest.writeByte((byte) (started == null ? 0 : started ? 1 : 2));
        dest.writeByte((byte) (ended == null ? 0 : ended ? 1 : 2));
        dest.writeParcelable(timeStamp, flags);
    }
}
