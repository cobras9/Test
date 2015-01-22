package com.mobilis.android.nfc.domain;



public class TxlDomain {
	private long id;
	private String txlId;
	private String dateCreated;
	private String txlType;
	
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getTxlId() {
		return txlId;
	}

	public void setTxlId(String transactionId) {
		this.txlId = transactionId;
	}

	// Will be used by the ArrayAdapter in the ListView
	@Override
	public String toString() {
		return txlId;
	}

	public String getDateCreated() {
		return dateCreated;
	}

	public void setDateCreated(String dateCreated) {
		this.dateCreated = dateCreated;
	}

	public String getTxlType() {
		return txlType;
	}

	public void setTxlType(String txlType) {
		this.txlType = txlType;
	}

}
