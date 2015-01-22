package com.mobilis.android.nfc.model;

import java.util.ArrayList;

public class TransactionHistoryGroup {

	private String transactionDate;
	private ArrayList<TransactionHistory> transactions;
	
	public TransactionHistoryGroup(){
		transactions = new ArrayList<TransactionHistory>();
	}
	public ArrayList<TransactionHistory> getTransactions(String date)
	{
		
		return null;
	}

	public String getTransactionDate() {
		return transactionDate;
	}

	public void setTransactionDate(String transactionDate) {
		this.transactionDate = transactionDate;
	}

	public ArrayList<TransactionHistory> getTransactions() {
		return transactions;
	}

	public void setTransactions(ArrayList<TransactionHistory> transactions) {
		this.transactions = transactions;
	}
}
