package com.example.zeeta.data;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

public class Card implements Parcelable {
    public static final Creator<Card> CREATOR = new Creator<Card>() {
        @Override
        public Card createFromParcel(Parcel in) {
            return new Card(in);
        }

        @Override
        public Card[] newArray(int size) {
            return new Card[size];
        }
    };
    private String cardNumber;
    private String cardName;
    private Date expiratinDate;
    private String cvv;
    private String type; // mastercard, visa, paypal, etc

    protected Card(Parcel in) {
        cardNumber = in.readString();
        cardName = in.readString();
        cvv = in.readString();
        type = in.readString();
    }

    public Card(String cardNumber, String cardName, Date expiratinDate, String cvv, String type) {
        this.cardNumber = cardNumber;
        this.cardName = cardName;
        this.expiratinDate = expiratinDate;
        this.cvv = cvv;
        this.type = type;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public String getCardName() {
        return cardName;
    }

    public void setCardName(String cardName) {
        this.cardName = cardName;
    }

    public Date getExpiratinDate() {
        return expiratinDate;
    }

    public void setExpiratinDate(Date expiratinDate) {
        this.expiratinDate = expiratinDate;
    }

    public String getCvv() {
        return cvv;
    }

    public void setCvv(String cvv) {
        this.cvv = cvv;
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
        dest.writeString(cardNumber);
        dest.writeString(cardName);
        dest.writeString(cvv);
        dest.writeString(type);
    }
}

