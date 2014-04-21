package com.returnjump.spoilfoil;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class FridgeDbHelper extends SQLiteOpenHelper {
    
    public FridgeDbHelper(Context context) {
        super(context, DatabaseContract.DATABASE_NAME, null, DatabaseContract.DATABASE_VERSION);
    }
    
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DatabaseContract.FridgeTable.SQL_CREATE_TABLE);
    }
    
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(DatabaseContract.FridgeTable.SQL_DELETE_TABLE);
        onCreate(db);
    }
    
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
    
    public long size() {
        SQLiteDatabase db = this.getReadableDatabase();
        
        return DatabaseUtils.queryNumEntries(db, DatabaseContract.FridgeTable.TABLE_NAME);
    }
    
    public static String calendarToString(Calendar cal) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        
        return dateFormat.format(cal.getTime());
    }
    
    public long put(String foodItem, Calendar expiryDate) {
        SQLiteDatabase db = this.getWritableDatabase();
        
        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(DatabaseContract.FridgeTable.COLUMN_NAME_FOOD_ITEM, foodItem);
        values.put(DatabaseContract.FridgeTable.COLUMN_NAME_EXPIRY_DATE, calendarToString(expiryDate));
        values.put(DatabaseContract.FridgeTable.COLUMN_NAME_VISIBLE, DatabaseContract.BOOL_TRUE);

        // Insert the new row, returning the primary key value of the new row
        long newRowId;
        newRowId = db.insert(
                DatabaseContract.FridgeTable.TABLE_NAME,
                null,
                values);
        
        return newRowId;
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
    
    public Cursor read() {
        SQLiteDatabase db = this.getReadableDatabase();
        
        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                DatabaseContract.FridgeTable._ID,
                DatabaseContract.FridgeTable.COLUMN_NAME_FOOD_ITEM,
                DatabaseContract.FridgeTable.COLUMN_NAME_EXPIRY_DATE,
                DatabaseContract.FridgeTable.COLUMN_NAME_VISIBLE,
                };

        // How you want the results sorted in the resulting Cursor
        String sortOrder =
                DatabaseContract.FridgeTable.COLUMN_NAME_EXPIRY_DATE + " ASC";

        Cursor c = db.query(
                DatabaseContract.FridgeTable.TABLE_NAME,    // The table to query
                projection,                               // The columns to return
                null,                                     // The columns for the WHERE clause
                null,                                     // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                sortOrder                                 // The sort order
                );
     
        return c;
    }
    
    // Instead of deleting, this implementation should just set the visible column to false
    public void delete(long rowId) {
        SQLiteDatabase db = this.getWritableDatabase();
        
        // Define 'where' part of query.
        String selection = DatabaseContract.FridgeTable._ID + " LIKE ?";
        
        // Specify arguments in placeholder order.
        String[] selectionArgs = { String.valueOf(rowId) };
        
        // Issue SQL statement.
        db.delete(DatabaseContract.FridgeTable.TABLE_NAME, selection, selectionArgs);
    }
    
    public void update(long rowId, String foodItem, Calendar expiryDate, int visible) {
        SQLiteDatabase db = this.getReadableDatabase();
        
        // New value for one column
        ContentValues values = new ContentValues();
        values.put(DatabaseContract.FridgeTable.COLUMN_NAME_FOOD_ITEM, foodItem);
        values.put(DatabaseContract.FridgeTable.COLUMN_NAME_EXPIRY_DATE, calendarToString(expiryDate));
        values.put(DatabaseContract.FridgeTable.COLUMN_NAME_VISIBLE, visible);

        // Which row to update, based on the ID
        String selection = DatabaseContract.FridgeTable._ID + " LIKE ?";
        String[] selectionArgs = { String.valueOf(rowId) };

        int count = db.update(
            DatabaseContract.FridgeTable.TABLE_NAME,
            values,
            selection,
            selectionArgs);
    }
}