package com.googlecode.gtalksms.databases;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseOpenHelper extends SQLiteOpenHelper {
    
    /* general database version gtalksms uses */
    private static final int DATABASE_VERSION = 3;
    
    /* information for the alias database */
    public static final String ALIAS_TABLE_NAME = "alias";
    private static final String ALIAS_TABLE_CREATE = 
        "CREATE TABLE " + ALIAS_TABLE_NAME + " (" +
            "aliasName TEXT NOT NULL, " +
            "number TEXT NOT NULL, " +
            "contactName TEXT, " +
            "PRIMARY KEY(aliasName)" +
         ")";
        
    /* information for the key value string table */
    public static final String KV_TABLE_NAME = "key_value";
    private static final String KV_TABLE_CREATE = 
        "CREATE TABLE " + KV_TABLE_NAME + " (" +
            "key TEXT NOT NULL, " +
            "value TEXT NOT NULL, " +
            "PRIMARY KEY(key)" +
         ")";
    
    /* information for the muc table */
    public static final String MUC_TABLE_NAME = "muc";
    private static final String MUC_TABLE_CREATE = 
        "CREATE TABLE " + MUC_TABLE_NAME + " (" +
            "muc TEXT NOT NULL, " +
            "number TEXT NOT NULL, " +
            "PRIMARY KEY(muc)" +
         ")";

//    public AliasOpenHelper(Context context, String name, CursorFactory factory, int version) {
//        super(context, name, factory, version);
//    }
    
    DatabaseOpenHelper(Context context) {
        super(context, ALIAS_TABLE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(ALIAS_TABLE_CREATE);
        db.execSQL(KV_TABLE_CREATE);
        db.execSQL(MUC_TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    	// add table that came with version 2 of our database
    	if (oldVersion < 2) {
            db.execSQL(KV_TABLE_CREATE);
    	}
    	if (oldVersion < 3) {
    		db.execSQL(MUC_TABLE_CREATE);
    	}
    }

}