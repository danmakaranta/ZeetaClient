package com.example.zeeta;

import android.os.Parcel;
import android.os.Parcelable;

import com.example.zeeta.data.Card;
import com.google.firebase.Timestamp;

public class TransactionData implements Parcelable {

    public static final Creator<TransactionData> CREATOR = new Creator<TransactionData>() {
        @Override
        public TransactionData createFromParcel(Parcel in) {
            return new TransactionData(in);
        }

        @Override
        public TransactionData[] newArray(int size) {
            return new TransactionData[size];
        }
    };
    private String detail;
    private String customerID;
    private boolean paidArtisan;
    private long amountPaid;
    private Timestamp date;
    private Card card;
    private String type;// cash or card or wallet

    protected TransactionData(Parcel in) {
        detail = in.readString();
        customerID = in.readString();
        paidArtisan = in.readByte() != 0;
        amountPaid = in.readLong();
        date = in.readParcelable(Timestamp.class.getClassLoader());
        type = in.readString();
    }

    public TransactionData(String detail, String customerID, boolean paidArtisan, long amountPaid, Timestamp date, Card card, String type) {
        this.detail = detail;
        this.customerID = customerID;
        this.paidArtisan = paidArtisan;
        this.amountPaid = amountPaid;
        this.date = date;
        this.card = card;
        this.type = type;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public String getCustomerID() {
        return customerID;
    }

    public void setCustomerID(String customerID) {
        this.customerID = customerID;
    }

    public boolean isPaidArtisan() {
        return paidArtisan;
    }

    public void setPaidArtisan(boolean paidArtisan) {
        this.paidArtisan = paidArtisan;
    }

    public long getAmountPaid() {
        return amountPaid;
    }

    public void setAmountPaid(long amountPaid) {
        this.amountPaid = amountPaid;
    }

    public Timestamp getDate() {
        return date;
    }

    public void setDate(Timestamp date) {
        this.date = date;
    }

    public Card getCard() {
        return card;
    }

    public void setCard(Card card) {
        this.card = card;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(detail);
        dest.writeString(customerID);
        dest.writeByte((byte) (paidArtisan ? 1 : 0));
        dest.writeLong(amountPaid);
        dest.writeParcelable(date, flags);
        dest.writeString(type);
    }
}

