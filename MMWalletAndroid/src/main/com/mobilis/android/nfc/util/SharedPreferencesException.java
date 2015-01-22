package com.mobilis.android.nfc.util;

public class SharedPreferencesException extends Exception{

	private static final long serialVersionUID = 1L;
	public SharedPreferencesException ()
    {
    }

	public SharedPreferencesException (String message)
    {
		super (message);
    }

	public SharedPreferencesException (Throwable cause)
    {
		super (cause);
    }

	public SharedPreferencesException (String message, Throwable cause)
    {
		super (message, cause);
    }

}
