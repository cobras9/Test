package com.mobilis.android.nfc.dao;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.mobilis.android.nfc.domain.TxlDomain;
import com.mobilis.android.nfc.util.MySQLiteHelper;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

public class DBService {
	private static DBService service;
	private static TxlDAO agentDAO;
	private static final String TAG = "DBService";
	
	public DBService(){/*** Exists only to defeat instantiation.*/}
	
	public static DBService getService(Context context){
		if(service == null){
			Log.d("mobilisDemo", "creating an instance of sqldatabase");
			service = new DBService();
			agentDAO = new TxlDAO(new MySQLiteHelper(context));
		}
		else
			Log.d("mobilisDemo", "instance of sqldatabase already exist!!");
		return service;
	}
	
	public TxlDomain getLastLogin(){
		return agentDAO.getLastLogin();
	}
	
	public TxlDomain getLastLoginOfType(String txlType){
		return agentDAO.getLastLoginOfType(txlType);
	}
	
	
	public void open(){
		try {
			TxlDAO.open();
			Log.v(TAG, "Openning database...");
		} catch (SQLException e) {

            e.printStackTrace();
		}
	}
	
	public void close(){
		agentDAO.close();
		Log.v(TAG, "Closing database...");
	}
	
	public TxlDomain createTxl(String txlId, String dateCreated, String txlType){
		return agentDAO.createTxl(txlId, dateCreated, txlType);
	}
	
	public TxlDomain commitTxl(TxlDomain txl){
		Log.v(TAG, "Committing txl with id: "+txl.getTxlId());
		return agentDAO.commitTxl(txl);
	}
	
	public void deleteTxl(TxlDomain txl){
		agentDAO.deleteTxl(txl);
	}
	
	public List<TxlDomain> getAllTxls() {
		return agentDAO.getAllTxls();
	}
	
	public TxlDomain cursorToLogin(Cursor cursor){
		return agentDAO.cursorToTxl(cursor);
	}
	
	public void deleteAllTxls(){
		agentDAO.deleteAllTxls();
	}
	
	public HashMap<String, String> getConnectionDetails() {
		return agentDAO.getConnectionDetails();
	}	 
	
	public void deleteConnectionDetails() {
		agentDAO.deleteConnectionDetails();
	}
	  
	public void insertConnectionDetails(String serverIP, String serverPort) {
		agentDAO.insertConnectionDetails(serverIP, serverPort);
	}

	public TxlDomain getLastTxlPayment(){
		return agentDAO.getLastTxlPayment();
	}
}
