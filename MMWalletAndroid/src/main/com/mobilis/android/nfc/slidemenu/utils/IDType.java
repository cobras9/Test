package com.mobilis.android.nfc.slidemenu.utils;

public class IDType {

    private final IDType.TYPE idType;
    private boolean idTypeAvailable;

    public boolean isIdTypeAvailable() {
        return idType == null;
    }
    public IDType(IDType.TYPE customerType){
        this.idType = customerType;
    }
    public IDType.TYPE getIDType(){
        return idType;
    }
    
	public enum TYPE{
        PASSPORT("Passport"),
        DRIVER_LICENCE("DriverLicence"),
        TAZKIRA("Tazkira"),
        NATIONAL_ID("NationalId"),
        TAXATION("Taxation"),
        PHOTO_ID("PhotoId");

        private final String value;
        private TYPE(String value){
            this.value = value;
        }
        public String toString(){
            return value;
        }
    }
}
