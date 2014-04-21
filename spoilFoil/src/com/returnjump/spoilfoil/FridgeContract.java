package com.returnjump.spoilfoil;

import android.provider.BaseColumns;

public final class FridgeContract {
    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "fridge.db";
    private static final String TEXT_TYPE = " TEXT";
    private static final String COMMA_SEP = ",";
    
    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty *private* constructor.
    private FridgeContract() {}

    // Inner class that defines the table contents
    public static abstract class FridgeTable implements BaseColumns {
        public static final String TABLE_NAME = "fridge";
        public static final String COLUMN_NAME_ITEM_ID = "itemid";
        public static final String COLUMN_NAME_FOOD_ITEM = "fooditem";
        public static final String COLUMN_NAME_EXPIRY_DATE = "expirydate";
        
        public static final String SQL_CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + " (" +
            FridgeTable._ID + " INTEGER PRIMARY KEY," +
            FridgeTable.COLUMN_NAME_ITEM_ID + TEXT_TYPE + COMMA_SEP +
            FridgeTable.COLUMN_NAME_FOOD_ITEM + TEXT_TYPE + COMMA_SEP +
            FridgeTable.COLUMN_NAME_EXPIRY_DATE + TEXT_TYPE + COMMA_SEP +
            " )";

        public static final String SQL_DELETE_TABLE =
            "DROP TABLE IF EXISTS " + FridgeTable.TABLE_NAME;
    }
}