package com.example.zeeta.data;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class JobData implements Parcelable {

    public static final Creator<JobData> CREATOR = new Creator<JobData>() {
        @Override
        public JobData createFromParcel(Parcel in) {
            return new JobData(in);
        }

        @Override
        public JobData[] newArray(int size) {
            return new JobData[size];
        }
    };
    private String serviceProviderID;
    private String serviceProviderName;
    private String serviceProviderPhone;
    private String status;
    private Long amountPaid;
    private GeoPoint gp;
    private Long hoursWorked;
    private @ServerTimestamp
    Date timeStamp;
    private String serviceRendered;
    private long hourlyRate;
    private boolean started;

    public JobData(String serviceProviderID, String serviceProviderName, String serviceProviderPhone, String status, Long amountPaid, Date timeStamp, GeoPoint gp, Long hoursWorked, boolean started, String serviceRendered, long hourlyRate) {
        this.serviceProviderID = serviceProviderID;
        this.serviceProviderName = serviceProviderName;
        this.serviceProviderPhone = serviceProviderPhone;
        this.status = status;
        this.amountPaid = amountPaid;
        this.gp = gp;
        this.hoursWorked = hoursWorked;
        this.timeStamp = timeStamp;
        this.serviceRendered = serviceRendered;
        this.hourlyRate = hourlyRate;
        this.started = started;
    }

    public JobData(String serviceProviderID, String serviceProviderName, String serviceProviderPhone, String status, Long amountPaid, Date timeStamp, GeoPoint gp, Long hoursWorked, boolean started) {
        this.serviceProviderID = serviceProviderID;
        this.serviceProviderName = serviceProviderName;
        this.serviceProviderPhone = serviceProviderPhone;
        this.status = status;
        this.amountPaid = amountPaid;
        this.gp = gp;
        this.hoursWorked = hoursWorked;
        this.timeStamp = timeStamp;
        this.started = started;
    }


    public JobData(String serviceProviderID, String serviceProviderName, String serviceProviderPhone, String status, Long amountPaid, Date timeStamp, GeoPoint gp, Long hoursWorked) {
        this.serviceProviderID = serviceProviderID;
        this.serviceProviderName = serviceProviderName;
        this.serviceProviderPhone = serviceProviderPhone;
        this.status = status;
        this.amountPaid = amountPaid;
        this.gp = gp;
        this.hoursWorked = hoursWorked;
        this.timeStamp = timeStamp;
    }

    protected JobData(Parcel in) {
        serviceProviderID = in.readString();
        serviceProviderName = in.readString();
        serviceProviderPhone = in.readString();
        status = in.readString();
        if (in.readByte() == 0) {
            amountPaid = null;
        } else {
            amountPaid = in.readLong();
        }
        if (in.readByte() == 0) {
            hoursWorked = null;
        } else {
            hoursWorked = in.readLong();
        }
        serviceRendered = in.readString();
        hourlyRate = in.readLong();
        started = in.readByte() != 0;
    }

    public boolean isStarted() {
        return started;
    }

    public void setStarted(boolean started) {
        this.started = started;
    }

    public Long getHoursWorked() {
        return hoursWorked;
    }

    public void setHoursWorked(Long hoursWorked) {
        this.hoursWorked = hoursWorked;
    }

    public GeoPoint getGp() {
        return gp;
    }

    public void setGp(GeoPoint gp) {
        this.gp = gp;
    }

    public String getServiceProviderID() {
        return serviceProviderID;
    }

    public void setServiceProviderID(String serviceProviderID) {
        this.serviceProviderID = serviceProviderID;
    }

    public String getServiceProviderName() {
        return serviceProviderName;
    }

    public void setServiceProviderName(String serviceProviderName) {
        this.serviceProviderName = serviceProviderName;
    }

    public String getServiceProviderPhone() {
        return serviceProviderPhone;
    }

    public void setServiceProviderPhone(String serviceProviderPhone) {
        this.serviceProviderPhone = serviceProviderPhone;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getAmountPaid() {
        return amountPaid;
    }

    public void setAmountPaid(Long amountPaid) {
        this.amountPaid = amountPaid;
    }

    public Date getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Date timeStamp) {
        this.timeStamp = timeStamp;
    }


    public String getServiceRendered() {
        return serviceRendered;
    }

    public void setServiceRendered(String serviceRendered) {
        this.serviceRendered = serviceRendered;
    }

    public long getHourlyRate() {
        return hourlyRate;
    }

    public void setHourlyRate(long hourlyRate) {
        this.hourlyRate = hourlyRate;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(serviceProviderID);
        dest.writeString(serviceProviderName);
        dest.writeString(serviceProviderPhone);
        dest.writeString(status);
        if (amountPaid == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(amountPaid);
        }
        if (hoursWorked == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(hoursWorked);
        }
        dest.writeString(serviceRendered);
        dest.writeLong(hourlyRate);
        dest.writeByte((byte) (started ? 1 : 0));
    }
}

