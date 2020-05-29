package com.example.zeeta.data;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.ServerTimestamp;

public class GeneralJobData implements Parcelable {


    private GeoPoint serviceLocation;
    private GeoPoint destination;
    private GeoPoint serviceProviderLocation;
    private String serviceID;
    private String serviceRendered;
    private String phoneNumber;
    private String Name;
    private Long distanceCovered;
    private Long amountPaid;
    public static final Creator<GeneralJobData> CREATOR = new Creator<GeneralJobData>() {
        @Override
        public GeneralJobData createFromParcel(Parcel in) {
            return new GeneralJobData(in);
        }

        @Override
        public GeneralJobData[] newArray(int size) {
            return new GeneralJobData[size];
        }
    };
    private Boolean started;
    private Boolean ended;
    private @ServerTimestamp
    Timestamp timeStamp;
    private String status;
    private Long hoursWorked;
    private String accepted;
    private boolean arrived;
    private boolean cancelRide;

    protected GeneralJobData(Parcel in) {
        serviceID = in.readString();
        serviceRendered = in.readString();
        phoneNumber = in.readString();
        Name = in.readString();
        if (in.readByte() == 0) {
            distanceCovered = null;
        } else {
            distanceCovered = in.readLong();
        }
        if (in.readByte() == 0) {
            amountPaid = null;
        } else {
            amountPaid = in.readLong();
        }
        accepted = in.readString();
        byte tmpStarted = in.readByte();
        started = tmpStarted == 0 ? null : tmpStarted == 1;
        byte tmpEnded = in.readByte();
        ended = tmpEnded == 0 ? null : tmpEnded == 1;
        timeStamp = in.readParcelable(Timestamp.class.getClassLoader());
        status = in.readString();
        if (in.readByte() == 0) {
            hoursWorked = null;
        } else {
            hoursWorked = in.readLong();
        }
        arrived = in.readByte() != 0;
        cancelRide = in.readByte() != 0;
    }

    public GeneralJobData(GeoPoint serviceLocation, GeoPoint destination, GeoPoint serviceProviderLocation, String serviceID,
                          String phoneNumber, String Name, Long distanceCovered, Long amountPaid, String accepted, Boolean started,
                          Boolean ended, String serviceRendered, Timestamp timeStamp, String status, Long hoursWorked, boolean arrived, boolean cancelRide) {
        this.serviceLocation = serviceLocation;
        this.destination = destination;
        this.serviceProviderLocation = serviceProviderLocation;
        this.serviceID = serviceID;
        this.phoneNumber = phoneNumber;
        this.Name = Name;
        this.distanceCovered = distanceCovered;
        this.amountPaid = amountPaid;
        this.accepted = accepted;
        this.started = started;
        this.ended = ended;
        this.serviceRendered = serviceRendered;
        this.timeStamp = timeStamp;
        this.status = status;
        this.hoursWorked = hoursWorked;
        this.arrived = arrived;
        this.cancelRide = cancelRide;
    }

    public GeoPoint getServiceLocation() {
        return serviceLocation;
    }

    public void setServiceLocation(GeoPoint serviceLocation) {
        this.serviceLocation = serviceLocation;
    }

    public GeoPoint getDestination() {
        return destination;
    }

    public void setDestination(GeoPoint destination) {
        this.destination = destination;
    }

    public GeoPoint getServiceProviderLocation() {
        return serviceProviderLocation;
    }

    public void setServiceProviderLocation(GeoPoint serviceProviderLocation) {
        this.serviceProviderLocation = serviceProviderLocation;
    }

    public String getServiceID() {
        return serviceID;
    }

    public void setServiceID(String serviceID) {
        this.serviceID = serviceID;
    }

    public String getServiceRendered() {
        return serviceRendered;
    }

    public void setServiceRendered(String serviceRendered) {
        this.serviceRendered = serviceRendered;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public Long getDistanceCovered() {
        return distanceCovered;
    }

    public void setDistanceCovered(Long distanceCovered) {
        this.distanceCovered = distanceCovered;
    }

    public Long getAmountPaid() {
        return amountPaid;
    }

    public void setAmountPaid(Long amountPaid) {
        this.amountPaid = amountPaid;
    }

    public String getAccepted() {
        return accepted;
    }

    public void setAccepted(String accepted) {
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

    public Timestamp getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Timestamp timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getHoursWorked() {
        return hoursWorked;
    }

    public void setHoursWorked(Long hoursWorked) {
        this.hoursWorked = hoursWorked;
    }

    public boolean isCancelRide() {
        return cancelRide;
    }

    public void setCancelRide(boolean cancelRide) {
        this.cancelRide = cancelRide;
    }

    public boolean isArrived() {
        return arrived;
    }

    public void setArrived(boolean arrived) {
        this.arrived = arrived;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(serviceID);
        dest.writeString(serviceRendered);
        dest.writeString(phoneNumber);
        dest.writeString(Name);
        if (distanceCovered == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(distanceCovered);
        }
        if (amountPaid == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(amountPaid);
        }
        dest.writeString(accepted);
        dest.writeByte((byte) (started == null ? 0 : started ? 1 : 2));
        dest.writeByte((byte) (ended == null ? 0 : ended ? 1 : 2));
        dest.writeParcelable(timeStamp, flags);
        dest.writeString(status);
        if (hoursWorked == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(hoursWorked);
        }
        dest.writeByte((byte) (arrived ? 1 : 0));
        dest.writeByte((byte) (cancelRide ? 1 : 0));
    }
}
