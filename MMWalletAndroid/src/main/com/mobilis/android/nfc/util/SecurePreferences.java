package com.mobilis.android.nfc.util;

/*
 * Copyright (C) 2013, Daniel Abraham
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Base64;
import android.util.Log;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Wrapper class for Android's {@link SharedPreferences} interface, which adds a layer of
 * encryption to the persistent storage and retrieval of sensitive key-value pairs of primitive
 * data types.
 * <p>
 * This class provides important - but nevertheless imperfect - protection against simple attacks
 * by casual snoopers. It is crucial to remember that even encrypted data may still be susceptible
 * to attacks, especially on rooted or stolen devices!
 * <p>
 * This class requires API level 8 (Android 2.2, a.k.a. "Froyo") or greater.
 * 
 * @see <a href="http://www.codeproject.com/Articles/549119/Encryption-Wrapper-for-Android-SharedPreferences">CodeProject article</a>
 */
public class SecurePreferences implements SharedPreferences {
	
	public static final String KEY_SERVER_IP = "SERVER_IP";
	public static final String KEY_SERVER_PORT = "SERVER_PORT";
    public static final String KEY_DEFAULT_CURRENCY = "KEY_DEFAULT_CURRENCY";

    public static final String KEY_SPLASHSCREEN = "SPLASHSCREEN";
    public static final String KEY_CLIENT_ID = "ClIENT_ID";
    public static final String KEY_MERCHANT_ID = "KEY_MERCHANT_ID";
    public static final String KEY_LOGIN_CLIENT_ID = "LOGIN_ClIENT_ID";
    public static final String KEY_WIDGET_QUICKBALANCE = "QuickBalanceEnabled";
    public static final String KEY_IS_CLIENT_ID_HIDDEN = "false";
    public static final String VALUE_CLIENT_ID_HIDDEN_TRUE = String.valueOf(true);
    public static final String VALUE_CLIENT_ID_HIDDEN_FALSE = String.valueOf(false);

    public static final String LAST_REQUEST = "LAST_REQUEST";
    public static final String LAST_RESPONSE = "LAST_RESPONSE";

    public static final String SAVED_VARIABLE_AMOUNT = "SAVED_VARIABLE_AMOUNT";

    private static SharedPreferences sFile;
    private static byte[] sKey;

    /**
     * Constructor.
     * 
     * @param context the caller's context
     */
    public SecurePreferences(Context context) {
        // Proxy design pattern
        if (SecurePreferences.sFile == null) {
            SecurePreferences.sFile = PreferenceManager.getDefaultSharedPreferences(context);
        }
        // Initialize encryption/decryption key
        try {
            final String key = SecurePreferences.generateAesKeyName(context);
            String value = SecurePreferences.sFile.getString(key, null);
            if (value == null) {
                value = SecurePreferences.generateAesKeyValue();
                SecurePreferences.sFile.edit().putString(key, value).commit();
            }
            SecurePreferences.sKey = SecurePreferences.decode(value);
            
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

	public String getResString(int res, Context context){
		return context.getResources().getString(res);
	}
	
    private static String encode(byte[] input)
    {
        return Base64.encodeToString(input, Base64.NO_PADDING | Base64.NO_WRAP);
    }

    private static byte[] decode(String input)
    {
        return Base64.decode(input, Base64.NO_PADDING | Base64.NO_WRAP);
    }

    private static String generateAesKeyName(Context context) throws InvalidKeySpecException,
            NoSuchAlgorithmException {
        final char[] password = context.getPackageName().toCharArray();
        final byte[] salt = Settings.Secure.getString(context.getContentResolver(),
                Settings.Secure.ANDROID_ID).getBytes();

        // Number of PBKDF2 hardening rounds to use, larger values increase
        // computation time, you should select a value that causes
        // computation to take >100ms
        final int iterations = 1000;

        // Generate a 256-bit key
        final int keyLength = 256;

        final KeySpec spec = new PBEKeySpec(password, salt, iterations, keyLength);
        return SecurePreferences.encode(SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
                .generateSecret(spec).getEncoded());
    }

    private static String generateAesKeyValue() throws NoSuchAlgorithmException {
        // Do *not* seed secureRandom! Automatically seeded from system entropy
        final SecureRandom random = new SecureRandom();

        // Use the largest AES key length which is supported by the OS
        final KeyGenerator generator = KeyGenerator.getInstance("AES");
        try {
            generator.init(256, random);
        } catch (Exception e) {
            try {
                generator.init(192, random);
            } catch (Exception e1) {
                generator.init(128, random);
            }
        }
        return SecurePreferences.encode(generator.generateKey().getEncoded());
    }

    private static String encrypt(String cleartext) {
        if (cleartext == null || cleartext.length() == 0) {
            return cleartext;
        }
        try {
            final Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(SecurePreferences.sKey, "AES"));
        	
//        	String ss = SecurePreferences.encode(cipher.doFinal(cleartext.getBytes("UTF-8")));
//        	Log.d("ahmed", "*!!!!!*Encrypting "+cleartext+"...encryptedValue is: "+ss);
            return SecurePreferences.encode(cipher.doFinal(cleartext.getBytes("UTF-8")));
        } catch (Exception e) {
            Log.w(SecurePreferences.class.getName(), "encrypt", e);
            return null;
        }
    }

    private static String decrypt(String ciphertext) {
        if (ciphertext == null || ciphertext.length() == 0) {
            return ciphertext;
        }
        try {
            final Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(SecurePreferences.sKey, "AES"));
//            String ss = new String(cipher.doFinal(SecurePreferences.decode(ciphertext)), "UTF-8");
//            Log.d("ahmed", "*!!!!!*decrypting "+ciphertext+"...clear value is: "+ss);
            return new String(cipher.doFinal(SecurePreferences.decode(ciphertext)), "UTF-8");
        } catch (Exception e) {
            Log.w(SecurePreferences.class.getName(), "decrypt", e);
            return null;
        }
    }


    public Map<String, String> getAll() {
        final Map<String, ?> encryptedMap = SecurePreferences.sFile.getAll();
        final Map<String, String> decryptedMap = new HashMap<String, String>(encryptedMap.size());
        for (Entry<String, ?> entry : encryptedMap.entrySet()) {
            try {
                decryptedMap.put(SecurePreferences.decrypt(entry.getKey()),
                        SecurePreferences.decrypt(entry.getValue().toString()));
            } catch (Exception e) {
                // Ignore unencrypted key/value pairs
            }
        }
        return decryptedMap;
    }


    public String getString(String key, String defaultValue) {
        final String encryptedValue = SecurePreferences.sFile.getString(SecurePreferences.encrypt(key), null);
        return (encryptedValue != null) ? SecurePreferences.decrypt(encryptedValue) : defaultValue;
    }


    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public Set<String> getStringSet(String key, Set<String> defaultValues) {
        final Set<String> encryptedSet =
                SecurePreferences.sFile.getStringSet(SecurePreferences.encrypt(key), null);
        if (encryptedSet == null) {
            return defaultValues;
        }
        final Set<String> decryptedSet = new HashSet<String>(encryptedSet.size());
        for (String encryptedValue : encryptedSet) {
            decryptedSet.add(SecurePreferences.decrypt(encryptedValue));
        }
        return decryptedSet;
    }


    public int getInt(String key, int defaultValue) {
        final String encryptedValue =
                SecurePreferences.sFile.getString(SecurePreferences.encrypt(key), null);
        if (encryptedValue == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(SecurePreferences.decrypt(encryptedValue));
        } catch (NumberFormatException e) {
            throw new ClassCastException(e.getMessage());
        }
    }


    public long getLong(String key, long defaultValue) {
        final String encryptedValue =
                SecurePreferences.sFile.getString(SecurePreferences.encrypt(key), null);
        if (encryptedValue == null) {
            return defaultValue;
        }
        try {
            return Long.parseLong(SecurePreferences.decrypt(encryptedValue));
        } catch (NumberFormatException e) {
            throw new ClassCastException(e.getMessage());
        }
    }


    public float getFloat(String key, float defaultValue) {
        final String encryptedValue =
                SecurePreferences.sFile.getString(SecurePreferences.encrypt(key), null);
        if (encryptedValue == null) {
            return defaultValue;
        }
        try {
            return Float.parseFloat(SecurePreferences.decrypt(encryptedValue));
        } catch (NumberFormatException e) {
            throw new ClassCastException(e.getMessage());
        }
    }


    public boolean getBoolean(String key, boolean defaultValue) {
        final String encryptedValue =
                SecurePreferences.sFile.getString(SecurePreferences.encrypt(key), null);
        if (encryptedValue == null) {
            return defaultValue;
        }
        try {
            return Boolean.parseBoolean(SecurePreferences.decrypt(encryptedValue));
        } catch (NumberFormatException e) {
            throw new ClassCastException(e.getMessage());
        }
    }


    public boolean contains(String key) {
        return SecurePreferences.sFile.contains(SecurePreferences.encrypt(key));
    }


    public Editor edit() {
        return new Editor();
    }

    /**
     * Wrapper for Android's {@link android.content.SharedPreferences.Editor}.
     * <p>
     * Used for modifying values in a {@link SecurePreferences} object. All changes you make in an
     * editor are batched, and not copied back to the original {@link SecurePreferences} until you
     * call {@link #commit()} or {@link #apply()}.
     */
    public static class Editor implements SharedPreferences.Editor {
        private SharedPreferences.Editor mEditor;

        /**
         * Constructor.
         */
        private Editor() {
            mEditor = SecurePreferences.sFile.edit();
        } 

    
        public SharedPreferences.Editor putString(String key, String value) {
            mEditor.putString(SecurePreferences.encrypt(key), SecurePreferences.encrypt(value));
            mEditor.commit();
            return this;
        }

    
        @TargetApi(Build.VERSION_CODES.HONEYCOMB)
        public SharedPreferences.Editor putStringSet(String key, Set<String> values) {
            final Set<String> encryptedValues = new HashSet<String>(values.size());
            for (String value : values) {
                encryptedValues.add(SecurePreferences.encrypt(value));
            }
            mEditor.putStringSet(SecurePreferences.encrypt(key), encryptedValues);
            return this;
        }

    
        public SharedPreferences.Editor putInt(String key, int value) {
            mEditor.putString(SecurePreferences.encrypt(key),
                    SecurePreferences.encrypt(Integer.toString(value)));
            return this;
        }

    
        public SharedPreferences.Editor putLong(String key, long value) {
            mEditor.putString(SecurePreferences.encrypt(key),
                    SecurePreferences.encrypt(Long.toString(value)));
            return this;
        }

    
        public SharedPreferences.Editor putFloat(String key, float value) {
            mEditor.putString(SecurePreferences.encrypt(key),
                    SecurePreferences.encrypt(Float.toString(value)));
            return this;
        }

    
        public SharedPreferences.Editor putBoolean(String key, boolean value) {
            mEditor.putString(SecurePreferences.encrypt(key),
                    SecurePreferences.encrypt(Boolean.toString(value)));
            return this;
        }

    
        public SharedPreferences.Editor remove(String key) {
            mEditor.remove(SecurePreferences.encrypt(key));
            return this;
        }

    
        public SharedPreferences.Editor clear() {
            mEditor.clear();
            return this;
        }

    
        public boolean commit() {
            return mEditor.commit();
        }

    
        @TargetApi(Build.VERSION_CODES.GINGERBREAD)
        public void apply() {
            mEditor.apply();
        }
    }


    public void registerOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {
        SecurePreferences.sFile.registerOnSharedPreferenceChangeListener(listener);
    }


    public void unregisterOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {
        SecurePreferences.sFile.unregisterOnSharedPreferenceChangeListener(listener);
    }
    
    
}

/**
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.mobilis.android.nfc.R;

public class SecurePreferences extends Activity{

	public static final String KEY_SERVER_IP = "SERVER_IP";
	public static final String KEY_SERVER_PORT = "SERVER_PORT";
	public static final String KEY_SPLASHSCREEN = "SPLASHSCREEN";
    public static final String KEY_CLIENT_ID = "ClIENT_ID";
    public static final String KEY_LOGIN_CLIENT_ID = "LOGIN_ClIENT_ID";
    public static final String KEY_IS_CLIENT_ID_HIDDEN = "false";
    public static final String VALUE_CLIENT_ID_HIDDEN_TRUE = String.valueOf(true);
    public static final String VALUE_CLIENT_ID_HIDDEN_FALSE = String.valueOf(false);
    
	private static SharedPreferences preferences;
	private Context context;
	
	public SecurePreferences(Context context){
		preferences = PreferenceManager.getDefaultSharedPreferences(context);
		this.context = context;
	}
	
	public void saveOrUpdate(String key, String value){
		Log.d("mobilis", String.format("saveOrUpdate(%s,%s) begin", key, value));
		SharedPreferences.Editor editor = preferences.edit();
		editor.putString(key,value);
		editor.commit();
		Log.d("mobilis", String.format("Committed saveOrUpdate(%s,%s)", key, value));
	}
	
	public void saveConnectionDetails(){
		SharedPreferences.Editor editor = preferences.edit();
		editor.putString(KEY_SERVER_IP, getResString(R.string.SERVER_IP));
		editor.commit();
		Log.d("mobilis", "---- Created "+KEY_SERVER_IP+" with value: "+getResString(R.string.SERVER_IP)+" in sharedPreference");
		editor.putString(KEY_SERVER_PORT, getResString(R.string.SERVER_PORT));
		editor.commit();
		Log.d("mobilis", "---- Created "+KEY_SERVER_PORT+" with value: "+getResString(R.string.SERVER_PORT)+" in sharedPreference");
		
	}
	
	public String retrieveData(String key){
		
		String value = preferences.getString(key,"");
		Log.d("mobilis", "---- Retrieving key: "+key+" which the value turned out to be: "+value+" sharedPreference");
		return value;
	}
	
	public boolean exists(String key){
		if(retrieveData(key)!= null && !retrieveData(key).equalsIgnoreCase(""))
		{
			return true;
		}
		return false;
	}
	
	public String getResString(int res){
		return context.getResources().getString(res);
	}
}**/
