package com.example.zeeta.models;

import com.google.firebase.Timestamp;

public class CompletedJobs {

    private String name;
    private Double amountPaid;
    private Timestamp dateRendered;
    private String phoneNumber;
    private String job;
    private String status;
    private String employeeID;
    private String employeeHourlyRate;
    private long hoursSpent;
    private Timestamp datecompleted;


    public CompletedJobs(String name, Double amountPaid, Timestamp dateRendered, String phoneNumber) {
        this.name = name;
        this.amountPaid = amountPaid;
        this.dateRendered = dateRendered;
        this.phoneNumber = phoneNumber;
    }

    public CompletedJobs(String name, Timestamp dateRendered, String job, String status) {
        this.name = name;
        this.dateRendered = dateRendered;
        this.job = job;
        this.status = status;
    }

    public CompletedJobs(String name, Timestamp dateRendered, String job, String status, String employeeID) {
        this.name = name;
        this.dateRendered = dateRendered;
        this.job = job;
        this.status = status;
        this.employeeID = employeeID;
    }

    public CompletedJobs(String name, Double amountPaid, Timestamp dateRendered, String phoneNumber, String job, String status, String employeeID, String employeeHourlyRate, long hoursSpent, Timestamp datecompleted) {
        this.name = name;
        this.amountPaid = amountPaid;
        this.dateRendered = dateRendered;
        this.phoneNumber = phoneNumber;
        this.job = job;
        this.status = status;
        this.employeeID = employeeID;
        this.employeeHourlyRate = employeeHourlyRate;
        this.hoursSpent = hoursSpent;
        this.datecompleted = datecompleted;
    }

    public String getJob() {
        return job;
    }

    public void setJob(String job) {
        this.job = job;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getEmployeeID() {
        return employeeID;
    }

    public void setEmployeeID(String employeeID) {
        this.employeeID = employeeID;
    }

    public String getEmployeeHourlyRate() {
        return employeeHourlyRate;
    }

    public void setEmployeeHourlyRate(String employeeHourlyRate) {
        this.employeeHourlyRate = employeeHourlyRate;
    }

    public long getHoursSpent() {
        return hoursSpent;
    }

    public void setHoursSpent(long hoursSpent) {
        this.hoursSpent = hoursSpent;
    }

    public Timestamp getDatecompleted() {
        return datecompleted;
    }

    public void setDatecompleted(Timestamp datecompleted) {
        this.datecompleted = datecompleted;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getAmountPaid() {
        return amountPaid;
    }

    public void setAmountPaid(Double amountPaid) {
        this.amountPaid = amountPaid;
    }

    public Timestamp getDateRendered() {
        return dateRendered;
    }

    public void setDateRendered(Timestamp dateRendered) {
        this.dateRendered = dateRendered;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

}
