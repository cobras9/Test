package com.mobilis.android.nfc.model;

/**
 * Created by ahmed on 24/07/14.
 */
public class Customer {


    private String index;
    private String MSISDN;
    private String givenName;
    private String surName;
    private String DOB;
    private String customerId;
    private String customerVerificationId;
    private String emailAddress;
    private String idType;
    private String customerType;
    private CustomerAddress customerAddress;
    private boolean iDVerified;

    public Customer(){
        customerAddress = new CustomerAddress();
    }
    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }

    public String getDOB() {
        return DOB;
    }

    public void setDOB(String DOB) {
        this.DOB = DOB;
    }

    public String getGivenName() {
        return givenName;
    }

    public void setGivenName(String givenName) {
        this.givenName = givenName;
    }

    public String getMSISDN() {
        return MSISDN;
    }

    public void setMSISDN(String MSISDN) {
        this.MSISDN = MSISDN;
    }

    public String getSurName() {
        return surName;
    }

    public void setSurName(String surName) {
        this.surName = surName;
    }

    public boolean isiDVerified() {
        return iDVerified;
    }

    public void setiDVerified(boolean iDVerified) {
        this.iDVerified = iDVerified;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public CustomerAddress getCustomerAddress() {
        return customerAddress;
    }

    public void setCustomerAddress(CustomerAddress customerAddress) {
        this.customerAddress = customerAddress;
    }

    public String getCustomerVerificationId() {
        return customerVerificationId;
    }

    public void setCustomerVerificationId(String customerVerificationId) {
        this.customerVerificationId = customerVerificationId;
    }


    public String getCustomerType() {
        return customerType;
    }

    public void setCustomerType(String customerType) {
        this.customerType = customerType;
    }

    public String getIdType() {
        return idType;
    }

    public void setIdType(String idType) {
        this.idType = idType;
    }
}
