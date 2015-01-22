package com.mobilis.android.nfc.util;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class MySQLiteHelper extends SQLiteOpenHelper {

  public static final String TABLE_TXL = "TXL";
  public static final String TABLE_CONNECTIONS = "ConnectionDetails";
  public static final String COLUMN_CONNECTIONS_ID = "ConnectionId";
  public static final String COLUMN_CONNECTIONS_KEY = "PairKey";
  public static final String COLUMN_CONNECTIONS_VALUE = "PairValue";
  public static final String COLUMN_TXL_ID = "TXL_ID";
  public static final String COLUMN_TRANSACTIONID = "txlId";
  public static final String COLUMN_DATECREATED = "dateCreated";
  public static final String COLUMN_TYPE = "txlType";

  private static final String DATABASE_NAME = "verifone001.db";
  private static final int DATABASE_VERSION = 1;

  // Database creation sql statement
  private static final String DATABASE_CREATE = "create table "
      + TABLE_TXL + "(" 
      + COLUMN_TXL_ID + " integer primary key autoincrement, " 
      + COLUMN_TRANSACTIONID   + " text, "
      + COLUMN_DATECREATED     + " text, " 
      + COLUMN_TYPE			   + " text "	
      + ");";
  
  private static final String CONNECTION_TABLE_CREATION = "create table "
	      + TABLE_CONNECTIONS + "(" 
	      + COLUMN_CONNECTIONS_ID + " integer primary key autoincrement, " 
	      + COLUMN_CONNECTIONS_KEY   + " text, "
	      + COLUMN_CONNECTIONS_VALUE    + " text" 	
	      + ");";
  

  public MySQLiteHelper(Context context) {
    super(context, DATABASE_NAME, null, DATABASE_VERSION);
  }

  @Override
  public void onCreate(SQLiteDatabase database) {
    database.execSQL(DATABASE_CREATE);
    database.execSQL(CONNECTION_TABLE_CREATION);
  }

  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    Log.w(MySQLiteHelper.class.getName(),
        "Upgrading database from version " + oldVersion + " to "
            + newVersion + ", which will destroy all old data");
    db.execSQL("DROP TABLE IF EXISTS " + TABLE_TXL);
    db.execSQL("DROP TABLE IF EXISTS " + TABLE_CONNECTIONS);
    onCreate(db);
  }

} 