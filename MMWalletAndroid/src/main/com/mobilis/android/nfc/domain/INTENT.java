package com.mobilis.android.nfc.domain;

/**
 * Created by ahmed on 9/06/14.
 */
public enum INTENT {

    LOGIN_SHOW_KEYPAD("LOGIN_SHOW_KEYPAD"),
    SERVER_COMM_TIME_OUT("SERVER_COMM_TIME_OUT"),
    SEND_MONEY_LOCAL("SEND_MONEY_LOCAL"),
    RECEIVE_MONEY("RECEIVE_MONEY"),
    CASH_OUT("CASH_OUT"),
    CASH_IN("CASH_IN"),
    REDEEM_VOUCHER("REDEEM_VOUCHER"),
    CASH_OUT_VOUCHER("CASH_OUT_VOUCHER"),
    ELECTRONIC_VOUCHER("ELECTRONIC_VOUCHER"),
    TOP_UP("TOP_UP"),
    CABLE_TV("CABLE_TV"),
    BANK("BANK"),
    QUICK_LINK_C2MP_AMOUNT_ONLY("QUICK_LINK_C2MP_AMOUNT_ONLY"),
    OTHER_OPERATOR("OTHER_OPERATOR"),
    HIDE_NFC_DIALOG("HIDE_NFC_DIALOG"),
    NFC_SCANNED("NFC_SCANNED"),
    DIALOG_TAG_REG_NFC_SCANNED("DIALOG_TAG_REG_NFC_SCANNED"),
    REGISTRATION_RESULT("REGISTRATION_RESULT"),
    TIME_OUT_ERROR("TIME_OUT_ERROR"),
    UPDATE_ACCOUNTS_BALANCES("UPDATE_ACCOUNTS_BALANCES"),
    REFRESH_ACCOUNT("REFRESH_ACCOUNT"),

    SHOW_KEYBOARD("SHOW_KEYBOARD"),
    HIDE_KEYBOARD("HIDE_KEYBOARD"),
    QUICK_LINKS_SETUP("QUICK_LINKS_SETUP"),
    QUICK_PAY("QUICK_PAY"),
    CLOSE_SOCKET("CLOSE_SOCKET"),
    DESTINATION_CHANGED("DESTINATION_CHANGED"),
    CHANGE_PIN("CHANGE_PIN"),
    TAG_REGISTRATION("TAG_REGISTRATION"),
    UPDATE_BALANCE("UPDATE_BALANCE"),
    GOT_BALANCE ("GOT_BALANCE"),
    GOT_BALANCE_TRANSACTIONS ("GOT_BALANCE_TRANSACTIONS"),
    INTERNET_WIFI_SIGNAL_WEAK("INTERNET_WIFI_SIGNAL_WEAK"),
    INTERNET_GSM_SIGNAL("INTERNET_GSM_SIGNAL"),
    INTERNET_NO_SIGNAL("INTERNET_NO_SIGNAL"),
    INTERNET_REGAINED("INTERNET_REGAINED"),
    CODE_BILL_PAYMENT("CODE_BILL_PAYMENT"),
    CODE_BANK("CODE_BANK"),
    CODE_UTILITY("CODE_UTILITY"),
    CODE_EVD("CODE_EVD"),
    CODE_AIRTIME_TOP_UP("CODE_AIRTIME_TOP_UP"),
    CODE_CREDIT_TOP_UP("CODE_CREDIT_TOP_UP"),
    CUSTOMER_LOOKUP("CUSTOMER_LOOKUP"),
    CUSTOMER_UPDATE("CUSTOMER_UPDATE"),
    CUSTOMER_REGISTRATION("CUSTOMER_REGISTRATION"),
    NEW_PIN_REQUIRED("NEW_PIN_REQUIRED"),
    NEW_PIN_RESULT("NEW_PIN_RESULT"),
    ENABLE_CUSTOMER_REGISTRATION("ENABLE_CUSTOMER_REGISTRATION"),
    UPDATE_ACTION_BAR_TITLE("UPDATE_ACTION_BAR_TITLE"),
    NFC_DIALOG_ON("NFC_DIALOG_ON"),
    CUSTOMER_CREATION_SELECTION("CUSTOMER_CREATION_SELECTION"),
    ENABLE_AMOUNT_EDIT_EXT("ENABLE_AMOUNT_EDIT_EXT"),
    DISABLE_AMOUNT_EDIT_TEXT("DISABLE_AMOUNT_EDIT_TEXT"),
    NEW_FRAGMENT("NEW_FRAGMENT"),
    NEW_NEW_FRAGMENT("NEW_NEW_FRAGMENT"),
    RESET_FRAGMENTS("RESET_FRAGMENTS"),
    DECREASE_VIEW_PAGER_HEIGHT("DECREASE_VIEW_PAGER_HEIGHT"),
    CUSTOMER_REGISTRATION_FIRST_CIRCLE("CUSTOMER_REGISTRATION_FIRST_CIRCLE"),
    CUSTOMER_REGISTRATION_SECOND_CIRCLE("CUSTOMER_REGISTRATION_SECOND_CIRCLE"),
    CUSTOMER_REGISTRATION_THIRD_CIRCLE("CUSTOMER_REGISTRATION_THIRD_CIRCLE"),
    GOT_QUICK_LINKS_PROCEED_TO_LOGIN("GOT_QUICK_LINKS_PROCEED_TO_LOGIN"),
//    GOT_NEW_PIN_REGISTRATION("GOT_NEW_PIN_REGISTRATION"),
//    GOT_NEW_PIN_LOGIN("GOT_NEW_PIN_LOGIN"),

    EXCHANGE_QUOTATION("EXCHANGE_QUOTATION"),
    EXTRA_INTERNET("EXTRA_INTERNET"),
    EXTRA_ACCOUNT_BALANCE("ACCOUNT_BALANCE"),
    EXTRA_ERROR("ERROR"),
    EXTRA_NFC_ID("NFC_ID"),
    EXTRA_RESPONSE("RESPONSE"),
    EXTRA_MERCHANT_FEES("MERCHANT_FEES"),
    EXTRA_NAME("NAME"),
    EXTRA_TOTAL_AMOUNT("EXTRA_TOTAL_AMOUNT"),
    EXTRA_SERVER_RESPONSE("EXTRA_SERVER_RESPONSE"),
    EXTRA_DESTINATION("EXTRA_DESTINATION"),
    EXTRA_INITIAL_CONTACT_PHONE("EXTRA_INITIAL_CONTACT_PHONE"),
    EXTRA_TITLE("EXTRA_TITLE"),
    EXTRA_FRAG_NAME("EXTRA_FRAG_NAME"),
    EXTRA_NUM("EXTRA_NUM"),
    EXTRA_SELECTION("EXTRA_SELECTION"),
    EXTRA_POS("POS"),
    GENERATE_TOKEN("GENERATE_TOKEN");

    private final String name;

    private INTENT(String s) {
        name = s;
    }

    public boolean equalsName(String otherName){
        return (otherName == null)? false:name.equals(otherName);
    }

    public String toString(){
        return name;
    }
}
