package com.mobilis.android.nfc.model;

public class TransactionHistory {

	private String transactionDate;
	private String transactionDetail;
    private String transactionType;
    private String transactionOtherParty;
    private String transactionAmount;

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }
    public String getTransactionOtherParty() {
        return transactionOtherParty;
    }

    public void setTransactionOtherParty(String transactionOtherParty) {
        this.transactionOtherParty = transactionOtherParty;
    }
    public String getTransactionAmount() {
        return transactionAmount;
    }

    public void setTransactionAmount(String transactionAmount) {
        this.transactionAmount = transactionAmount;
    }

	public String getTransactionDate() {
		return transactionDate;
	}
	public void setTransactionDate(String transactionDate) {
		this.transactionDate = transactionDate;
	}
	public String getTransactionDetail() {
		return transactionDetail;
	}
	public void setTransactionDetails(String transactionTime) {
		this.transactionDetail = transactionTime;
	}


}
