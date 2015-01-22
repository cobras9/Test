package com.mobilis.android.nfc.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.mobilis.android.nfc.R;
import com.mobilis.android.nfc.domain.ConnectionDomain;
import com.mobilis.android.nfc.domain.TxlDomain;
import com.mobilis.android.nfc.util.MySQLiteHelper;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.mobilis.android.nfc.util.Constants.getApplicationContext;

public class TxlDAO {
	  private static final String TAG = "TxlDAO";
	 // Database fields
	  private static SQLiteDatabase database;
	  private static MySQLiteHelper dbHelper;
	  private String[] allColumns_txlTable = { MySQLiteHelper.COLUMN_TXL_ID,  MySQLiteHelper.COLUMN_TRANSACTIONID, MySQLiteHelper.COLUMN_DATECREATED };
	  private String[] allColumns_connTable = { MySQLiteHelper.COLUMN_CONNECTIONS_ID,  MySQLiteHelper.COLUMN_CONNECTIONS_KEY, MySQLiteHelper.COLUMN_CONNECTIONS_VALUE};
	  
	  @SuppressWarnings("static-access")
	  protected TxlDAO(MySQLiteHelper dbHelper) {
		  this.dbHelper = dbHelper;
	  }

	  protected static void open() throws SQLException {
		  database = dbHelper.getWritableDatabase();
	  }

	  protected void close() {
//	    dbHelper.close();
	  }

	  protected TxlDomain createTxl(String txlId, String dateCreated, String txlType) {
	    ContentValues values = new ContentValues();
	    values.put(MySQLiteHelper.COLUMN_TRANSACTIONID, txlId);
	    values.put(MySQLiteHelper.COLUMN_DATECREATED, dateCreated);
	    values.put(MySQLiteHelper.COLUMN_TYPE, txlType);
	    long insertId = database.insert(MySQLiteHelper.TABLE_TXL, null, values);
	    Cursor cursor = database.query(MySQLiteHelper.TABLE_TXL, allColumns_txlTable, MySQLiteHelper.COLUMN_TXL_ID + " = " + insertId, null,
	        null, null, null);
	    cursor.moveToFirst();
	    TxlDomain newLogin = cursorToTxl(cursor);
	    cursor.close();
	    return newLogin;
	  }
	  
	  protected TxlDomain commitTxl(TxlDomain txl) {
		    ContentValues values = new ContentValues();
		    values.put(MySQLiteHelper.COLUMN_TRANSACTIONID, txl.getTxlId());
		    values.put(MySQLiteHelper.COLUMN_DATECREATED, txl.getDateCreated());
		    values.put(MySQLiteHelper.COLUMN_TYPE, txl.getTxlType());
		    long insertId = database.insert(MySQLiteHelper.TABLE_TXL, null, values);
		    Cursor cursor = database.query(MySQLiteHelper.TABLE_TXL, allColumns_txlTable, MySQLiteHelper.COLUMN_TXL_ID + " = " + insertId, null,
		        null, null, null);
		    cursor.moveToFirst();
		    TxlDomain newLogin = cursorToTxl(cursor);
		    cursor.close();
		    return newLogin;
		  }

	  protected void deleteTxl(TxlDomain txl) {
	    long id = txl.getId();
	    database.delete(MySQLiteHelper.TABLE_TXL, MySQLiteHelper.COLUMN_TXL_ID
	        + " = " + id, null);
	  }
	  
	  protected void deleteAllTxls() {
		  if(getAllTxls().size() > 0)
		    database.delete(MySQLiteHelper.TABLE_TXL, null, null);
	  }

	  protected List<TxlDomain> getAllTxls() {
	    List<TxlDomain> txls = new ArrayList<TxlDomain>();
	    
	    Cursor cursor = database.query(MySQLiteHelper.TABLE_TXL,
	    		allColumns_txlTable, null, null, null, null, null);

	    cursor.moveToFirst();
	    while (!cursor.isAfterLast()) {
	      TxlDomain login = cursorToTxl(cursor);
	      txls.add(login);
	      cursor.moveToNext();
	    }
	    // Make sure to close the cursor
	    cursor.close();
	    return txls;

	  }
	  protected TxlDomain cursorToTxl(Cursor cursor) {
		    TxlDomain txl = new TxlDomain();
		    if(cursor.getCount() > 0){
		    	txl.setId(cursor.getLong(0));
		    	txl.setTxlId(cursor.getString(1));
		    	txl.setDateCreated(cursor.getString(2));
		    }
		    return txl;
	   }
	  
	  
	  
	  protected TxlDomain getLastLoginOfType(String txlType) {

		  String mySQL = "SELECT "+ "*" + " FROM " + MySQLiteHelper.TABLE_TXL + " WHERE " + MySQLiteHelper.COLUMN_TYPE + " =?";
		  Cursor cursor = database.rawQuery(mySQL, new String[]{txlType});
		  cursor.moveToLast(); 
		  return cursorToTxl(cursor); 
	  }
	  
	  protected TxlDomain getLastLogin() {
		  
		  Cursor cursor = database.query(MySQLiteHelper.TABLE_TXL, null, null, null, null, null, null);
		  Log.v(TAG, "curser after fetching all records, size: "+String.valueOf(cursor.getCount()));
		  cursor.moveToLast(); 
		  return cursorToTxl(cursor); 
	  }
	  
	  protected HashMap<String, String> getConnectionDetails() {
		  Cursor cursor = database.query(MySQLiteHelper.TABLE_CONNECTIONS, null, null, null, null, null, null);
		  HashMap<String, String> connectionDetails = new HashMap<String, String>();
		  cursor.moveToLast();
		  if(cursor.getCount() > 0){
			  cursor.moveToFirst();
			  for(int i =0 ; i < cursor.getCount(); i++){
				  if(cursor.getString(1).equalsIgnoreCase(getResString(R.string.DB_SERVER_IP)))
					  connectionDetails.put(getResString(R.string.DB_SERVER_IP), cursor.getString(2));
				  else
					  connectionDetails.put(getResString(R.string.DB_SERVER_PORT), cursor.getString(2));
				  cursor.moveToNext();
			  }
		  }
		  
		  return connectionDetails;
	  }
	  
	  protected void deleteConnectionDetails() {
		    database.delete(MySQLiteHelper.TABLE_CONNECTIONS, null, null);
	  }
	  
	  protected void insertConnectionDetails(String serverIP, String serverPort) {
		  ContentValues values = new ContentValues();
		  
		  Log.d("mobilisDemo", "updating serverIp to: "+serverIP);
		  deleteConnectionDetails();
		  ConnectionDomain ConnectionIP = new ConnectionDomain();
		  ConnectionIP.setKey(getResString(R.string.DB_SERVER_IP));
		  ConnectionIP.setValue(serverIP);
		  
		  values.put(MySQLiteHelper.COLUMN_CONNECTIONS_KEY, ConnectionIP.getKey());
		  values.put(MySQLiteHelper.COLUMN_CONNECTIONS_VALUE, ConnectionIP.getValue());
		  long insertId = database.insert(MySQLiteHelper.TABLE_CONNECTIONS, null, values);
		  Cursor cursor = database.query(MySQLiteHelper.TABLE_CONNECTIONS, allColumns_connTable, MySQLiteHelper.COLUMN_CONNECTIONS_ID + " = " + insertId, null, null, null, null);
		  cursor.close();
			  
		  
		  ConnectionDomain connectionPort = new ConnectionDomain();
		  connectionPort.setKey(getResString(R.string.DB_SERVER_PORT));
		  connectionPort.setValue(serverPort);
		  Log.d("mobilisDemo", "updating port to: "+serverPort);
		  values.clear();
		  values.put(MySQLiteHelper.COLUMN_CONNECTIONS_KEY, connectionPort.getKey());
		  values.put(MySQLiteHelper.COLUMN_CONNECTIONS_VALUE, connectionPort.getValue());
		  long insertId2 = database.insert(MySQLiteHelper.TABLE_CONNECTIONS, null, values);
		  Cursor cursor2 = database.query(MySQLiteHelper.TABLE_CONNECTIONS, allColumns_connTable, MySQLiteHelper.COLUMN_CONNECTIONS_ID + " = " + insertId2, null, null, null, null);
		  cursor2.close();
		   
	  }
	  
	  protected TxlDomain getLastTxlPayment(){
		  String mySQL = "SELECT "+ "*" + " FROM " + MySQLiteHelper.TABLE_TXL + " WHERE " + MySQLiteHelper.COLUMN_TYPE + " =?";
		  Cursor cursor = database.rawQuery(mySQL, new String[]{"MerchantPayment"});
		  cursor.moveToLast(); 
		  return cursorToTxl(cursor); 
	  }
	  
	  private String getResString(int id){
		  Log.d("mobilisDemo", "get String: "+id);
		  return getApplicationContext().getResources().getString(id);
	  }
}
