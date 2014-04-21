package com.returnjump.spoilfoil;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class FridgeDbHelper extends SQLiteOpenHelper {
    public FridgeDbHelper(Context context) {
        super(context, FridgeContract.DATABASE_NAME, null, FridgeContract.DATABASE_VERSION);
    }
    
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(FridgeContract.FridgeTable.SQL_CREATE_TABLE);
    }
    
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(FridgeContract.FridgeTable.SQL_DELETE_TABLE);
        onCreate(db);
    }
    
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
    
    public static String calendarToString(Calendar cal) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        
        return dateFormat.format(cal.getTime());
    }
    
    public void put(SQLiteDatabase db, long id, String foodItem, Calendar expiryDate) {
        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(FridgeContract.FridgeTable.COLUMN_NAME_ITEM_ID, id);
        values.put(FridgeContract.FridgeTable.COLUMN_NAME_FOOD_ITEM, foodItem);
        values.put(FridgeContract.FridgeTable.COLUMN_NAME_EXPIRY_DATE, calendarToString(expiryDate));

        // Insert the new row, returning the primary key value of the new row
        long newRowId;
        newRowId = db.insert(
                FridgeContract.FridgeTable.TABLE_NAME,
                null,
                values);
    }
    
    public static Calendar stringToCalendar(String date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Calendar cal = Calendar.getInstance();
        try {
            cal.setTime(dateFormat.parse(date));
        } catch (ParseException e) {
            cal = null;
        }
        
        return cal;
    }
    
    public Cursor read(SQLiteDatabase db) {
        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                FridgeContract.FridgeTable._ID,
                FridgeContract.FridgeTable.COLUMN_NAME_ITEM_ID,
                FridgeContract.FridgeTable.COLUMN_NAME_FOOD_ITEM,
                FridgeContract.FridgeTable.COLUMN_NAME_EXPIRY_DATE,
                };

        // How you want the results sorted in the resulting Cursor
        String sortOrder =
                FridgeContract.FridgeTable.COLUMN_NAME_EXPIRY_DATE + " ASC";

        Cursor c = db.query(
                FridgeContract.FridgeTable.TABLE_NAME,    // The table to query
                projection,                               // The columns to return
                null,                                     // The columns for the WHERE clause
                null,                                     // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                sortOrder                                 // The sort order
                );
     
        return c;
    }
    
    public void delete(SQLiteDatabase db, long rowId) {
        // Define 'where' part of query.
        String selection = FridgeContract.FridgeTable.COLUMN_NAME_ITEM_ID + " LIKE ?";
        
        // Specify arguments in placeholder order.
        String[] selectionArgs = { String.valueOf(rowId) };
        
        // Issue SQL statement.
        db.delete(FridgeContract.FridgeTable.TABLE_NAME, selection, selectionArgs);
    }
    
    public void update(SQLiteDatabase db, long rowId, String foodItem, Calendar expiryDate) {
        // New value for one column
        ContentValues values = new ContentValues();
        values.put(FridgeContract.FridgeTable.COLUMN_NAME_FOOD_ITEM, foodItem);

        // Which row to update, based on the ID
        String selection = FridgeContract.FridgeTable.COLUMN_NAME_ITEM_ID + " LIKE ?";
        String[] selectionArgs = { String.valueOf(rowId) };

        int count = db.update(
            FridgeContract.FridgeTable.TABLE_NAME,
            values,
            selection,
            selectionArgs);
    }
}