package com.mobilis.android.nfc.model;

import java.util.ArrayList;
import java.util.List;

public class TransferCode {

	private String code;
	private String description;
	private String amount;
	public static List<TransferCode> billPaymentCodes = new ArrayList<TransferCode>();
    public static List<TransferCode> bankCodes = new ArrayList<TransferCode>();
	public static List<TransferCode> topupAirtimeCodes = new ArrayList<TransferCode>();
    public static List<TransferCode> topupcreditCodes = new ArrayList<TransferCode>();
    public static List<TransferCode> utilityCodes = new ArrayList<TransferCode>();

	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getAmount() {
		return amount;
	}
	public void setAmount(String amount) {
		this.amount = amount;
	}
	
}
