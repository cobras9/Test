package com.mobilis.android.nfc.model;

import java.util.ArrayList;

public class TransactionHistoryRoot {
	
	private String requestTime;
	private ArrayList<TransactionHistoryGroup> txlsGroup = new ArrayList<TransactionHistoryGroup>();
	
	public TransactionHistoryRoot(){
		txlsGroup = new ArrayList<TransactionHistoryGroup>();
	}
	public ArrayList<TransactionHistoryGroup> getTxlGroup() {
		return txlsGroup;
	}
	public void setTxlsGroup(ArrayList<TransactionHistoryGroup> transactionHistories) {
		this.txlsGroup = transactionHistories;
	}

	public String getRequestTime() {
		return requestTime;
	}
	public void setRequestTime(String requestTime) {
		this.requestTime = requestTime;
	}

}
