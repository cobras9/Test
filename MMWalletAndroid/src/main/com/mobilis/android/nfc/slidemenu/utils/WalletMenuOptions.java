package com.mobilis.android.nfc.slidemenu.utils;

public class WalletMenuOptions {

	private boolean changePin = false;
	private boolean checkLastTxn = false;
	private boolean balance = false;
	
	private boolean receivePayment = false;
	private boolean sendTopup = false;

    private boolean receiveCashIn = false;
	private boolean giveCashOut = false;
	private boolean makePayment = false;
    private boolean redeemVoucher = false;

	private boolean billPayments = false;
	private boolean vouchers = false;

    private boolean cashoutVouchers = false;
    private boolean registration = false;

    private boolean reg_services = false;
    private boolean reg_services_customer = false;
    private boolean reg_services_customer_new = false;
    private boolean reg_services_customer_existing = false;
    private boolean reg_services_tag = false;
    private boolean reg_services_tag_primary = false;
    private boolean reg_services_tag_secondary = false;
    private boolean reg_services_tag_replace = false;
    private boolean reg_services_additional = false;
    private boolean reg_services_customer_details = false;

    private boolean customerLookupAvailable;
    private boolean customerRegistrationAvailable;

    private boolean serverConfigurable;
	private boolean selectCurrencyAvailable;

    private boolean electronicVouchersAvailable;
    private boolean txfEVDAvailable;
    private boolean buyVoucherAvailable;
    private boolean buyBulkVoucherAvailable;

    private boolean receiveCashInCCAvailable;

    private boolean generateTokenAvailable;

	public boolean isChangePinAvailable() {
		return changePin;
	}
	public void setChangePin(boolean changePin) {
		this.changePin = changePin;
	}

	public boolean isCheckLastTxnAvailable() {
		return checkLastTxn;
	}
	public void setCheckLastTxn(boolean checkLastTxn) {
		this.checkLastTxn = checkLastTxn;
	}

	public boolean isGetBalanceAvailable() {
		return balance;
	}
	public void setBalance(boolean balance) {
		this.balance = balance;
	}

    public boolean isRedeemVoucherAvailable() {
        return redeemVoucher;
    }
    public void setRedeemVoucherAvailable(boolean redeemVoucher) { this.redeemVoucher = redeemVoucher;}

	public boolean isReceivePaymentAvailable() {
		return receivePayment;
	}
	public void setReceivePaymentAvailable(boolean purchase) {
		this.receivePayment = purchase;
	}


	public boolean isGiveCashOutAvailable() {
		return giveCashOut;
	}
	public void setGiveCashOutAvailable(boolean withdraw) {
		this.giveCashOut = withdraw;
	}

	public boolean isMakePayment() {
		return makePayment;
	}
	public void setMakePayment(boolean makePayment) {
		this.makePayment = makePayment;
	}

	public boolean isSendTopup() {
		return sendTopup;
	}
	public void setSendTopup(boolean topup) {
		this.sendTopup = topup;
	}

	public boolean isServerConfigurable() {
		return serverConfigurable;
	}
	public void setServerConfigurable(boolean serverConfigurable) {this.serverConfigurable = serverConfigurable;}

	public boolean isSelectCurrencyAvailable() {
		return selectCurrencyAvailable;
	}
	public void setSelectCurrencyAvailable(boolean selectCurrencyAvailable) {	this.selectCurrencyAvailable = selectCurrencyAvailable;}

	public boolean isBillPayments() {
		return billPayments;
	}
	public void setBillPayments(boolean billPayments) {
		this.billPayments = billPayments;
	}

	public boolean isVouchers() {
		return vouchers;
	}
	public void setVouchers(boolean vouchers) {
		this.vouchers = vouchers;
	}

    public boolean isReceiveCashInCCAvailable() {
        return receiveCashInCCAvailable;
    }

    public void setReceiveCashInCCAvailable(boolean receiveCashInCC) {
        this.receiveCashInCCAvailable = receiveCashInCC;
    }

    public boolean isReceiveCashInAvailable() {
        return receiveCashIn;
    }

    public void setReceiveCashInAvailable(boolean receiveCashIn) {
        this.receiveCashIn = receiveCashIn;
    }


    // registration Services

    //<editor-fold desc="registration Services">
    public boolean isRegistrationServices() {
        return reg_services;
    }
    public void setRegistrationServices(boolean reg_services) {
        this.reg_services = reg_services;
    }

    public boolean isRegServicesCustomer() {
        return reg_services;
    }
    public void setRegServicesCustomer(boolean reg_services_customer) {
        this.reg_services_customer = reg_services_customer;
    }

    public boolean isRegServicesCustomerNew() {
        return reg_services_customer_new;
    }
    public void setRegServicesCustomerNew(boolean reg_services_customer_new) {
        this.reg_services_customer_new = reg_services_customer_new;
    }

    public boolean isRegServicesCustomerExisting() {
        return reg_services_customer_existing;
    }
    public void setRegServicesCustomerExisting(boolean reg_services_customer_existing) {
        this.reg_services_customer_existing = reg_services_customer_existing;
    }

    public boolean isRegServicesTag() {
        return reg_services_tag;
    }
    public void setRegServicesTag(boolean reg_services_tag) {
        this.reg_services_tag = reg_services_tag;
    }

    public boolean isRegServicesTagPrimary() {
        return reg_services_tag_primary;
    }
    public void setRegServicesTagPrimary(boolean reg_services_tag_primary) {
        this.reg_services_tag_primary = reg_services_tag_primary;
    }

    public boolean isRegServicesTagSecondary() {
        return reg_services_tag_secondary;
    }
    public void setRegServicesTagSecondary(boolean reg_services_tag_secondary) {
        this.reg_services_tag_secondary = reg_services_tag_secondary;
    }

    public boolean isRegServicesTagReplace() {
        return reg_services_tag_replace;
    }
    public void setRegServicesTagReplace(boolean reg_services_tag_replace) {
        this.reg_services_tag_replace = reg_services_tag_replace;
    }

    public boolean isRegServicesAdditional() {
        return reg_services_additional;
    }
    public void setRegServicesAdditional(boolean reg_services_additional) {
        this.reg_services_additional = reg_services_additional;
    }

    public boolean isRegServicesCustomerDetails() {
        return reg_services_customer_details;
    }
    public void setRegServicesCustomerDetails(boolean reg_services_customer_details) {
        this.reg_services_customer_details = reg_services_customer_details;
    }
    //</editor-fold>

    public boolean isCashoutVouchers() {
        return cashoutVouchers;
    }

    public void setCashoutVouchers(boolean cashoutVouchers) {
        this.cashoutVouchers = cashoutVouchers;
    }

    public boolean isRegistrationAvailable() {
        return registration;
    }

    public void setRegistrationAvailable(boolean registration) {
        this.registration = registration;
    }

    public boolean isCustomerLookupAvailable() {
        return customerLookupAvailable;
    }

    public void setCustomerLookupAvailable(boolean customerLookupAvailable) {
        this.customerLookupAvailable = customerLookupAvailable;
    }

    public boolean isCustomerRegistrationAvailable() {
        return customerRegistrationAvailable;
    }

    public void setCustomerRegistrationAvailable(boolean customerRegistrationAvailable) {
        this.customerRegistrationAvailable = customerRegistrationAvailable;
    }

    public boolean isElectronicVouchersAvailable() {
        return electronicVouchersAvailable;
    }

    public void setElectronicVouchersAvailable(boolean electronicVouchersAvailable) {
        this.electronicVouchersAvailable = electronicVouchersAvailable;
    }


    public boolean isBuyVoucherAvailable() {
        return buyVoucherAvailable;
    }

    public void setBuyVoucherAvailable(boolean buyVoucherAvailable) {
        this.buyVoucherAvailable = buyVoucherAvailable;
    }


    public boolean isTxfEVDAvailable() {
        return txfEVDAvailable;
    }

    public void setTxfEVDAvailable(boolean txfEVDAvailable) {
        this.txfEVDAvailable = txfEVDAvailable;
    }

    public boolean isBuyBulkVoucherAvailable() {
        return buyBulkVoucherAvailable;
    }

    public void setBuyBulkVoucherAvailable(boolean buyBulkVoucherAvailable) {
        this.buyBulkVoucherAvailable = buyBulkVoucherAvailable;
    }

    public boolean isGenerateTokenAvailable() {
        return generateTokenAvailable;
    }

    public void setGenerateTokenAvailable(boolean generateTokenAvailable) {
        this.generateTokenAvailable = generateTokenAvailable;
    }
}
