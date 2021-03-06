package com.returnjump.frij;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.preference.PreferenceManager;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

public class FridgeDbHelper extends SQLiteOpenHelper {

    private Context context;

    public FridgeDbHelper(Context context) {
        super(context, DatabaseContract.DATABASE_NAME, null, DatabaseContract.DATABASE_VERSION);

        this.context = context;
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
    
    public static String calendarToString(Calendar cal, String format) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(format);
        
        return dateFormat.format(cal.getTime());
    }

    public long put(String foodItem, Calendar expiryDate, String rawFoodItem, int isFromImage, String image, String imageBinarized) {
        SQLiteDatabase db = this.getWritableDatabase();
        
        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        String calNow = calendarToString(GregorianCalendar.getInstance(), DatabaseContract.FORMAT_DATETIME);
        String hash = FridgeItem.getMD5Hash(foodItem + calNow);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String email = sharedPreferences.getString(SettingsActivity.PREF_EMAIL_ADDRESS, SettingsActivity.PREF_EMAIL_ADDRESS_DEFAULT);

        values.put(DatabaseContract.FridgeTable.COLUMN_NAME_HASH, hash);
        values.put(DatabaseContract.FridgeTable.COLUMN_NAME_FOOD_ITEM, foodItem);
        values.put(DatabaseContract.FridgeTable.COLUMN_NAME_RAW_FOOD_ITEM, rawFoodItem);
        values.put(DatabaseContract.FridgeTable.COLUMN_NAME_EXPIRY_DATE, calendarToString(expiryDate, DatabaseContract.FORMAT_DATE));
        values.put(DatabaseContract.FridgeTable.COLUMN_NAME_CREATED_DATE, calNow);
        values.put(DatabaseContract.FridgeTable.COLUMN_NAME_UPDATED_DATE, calNow);
        values.put(DatabaseContract.FridgeTable.COLUMN_NAME_EMAIL, email); // If someone adds items with no email, then later adds an email, the previous items won't belong to them
        values.put(DatabaseContract.FridgeTable.COLUMN_NAME_UPDATED_BY, "DEVICE");
        values.put(DatabaseContract.FridgeTable.COLUMN_NAME_FROM_IMAGE, isFromImage);
        values.put(DatabaseContract.FridgeTable.COLUMN_NAME_IMAGE, image);
        values.put(DatabaseContract.FridgeTable.COLUMN_NAME_IMAGE_BINARIZED, imageBinarized);
        values.put(DatabaseContract.FridgeTable.COLUMN_NAME_DISMISSED, DatabaseContract.BOOL_FALSE);
        values.put(DatabaseContract.FridgeTable.COLUMN_NAME_EXPIRED, DatabaseContract.BOOL_FALSE);
        values.put(DatabaseContract.FridgeTable.COLUMN_NAME_EDITED_CART, DatabaseContract.BOOL_FALSE);
        values.put(DatabaseContract.FridgeTable.COLUMN_NAME_EDITED_FRIDGE, DatabaseContract.BOOL_FALSE);
        values.put(DatabaseContract.FridgeTable.COLUMN_NAME_DELETED_CART, DatabaseContract.BOOL_FALSE);
        values.put(DatabaseContract.FridgeTable.COLUMN_NAME_DELETED_FRIDGE, DatabaseContract.BOOL_FALSE);
        values.put(DatabaseContract.FridgeTable.COLUMN_NAME_NOTIFIED_PUSH, DatabaseContract.BOOL_FALSE);
        values.put(DatabaseContract.FridgeTable.COLUMN_NAME_NOTIFIED_EMAIL, DatabaseContract.BOOL_FALSE);

        // Insert the new row, returning the primary key value of the new row
        long newRowId = db.insert(
                DatabaseContract.FridgeTable.TABLE_NAME,
                null,
                values);

        // Save to cloud
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this.context);
        boolean auto = sharedPref.getBoolean(SettingsActivity.PREF_CHECKBOX_AUTO, true);
        if (auto) {
            // Temporary fix for Issue #13
            FridgeItem fridgeItem = getRowById(newRowId, false);
            if (fridgeItem != null) {
                MyParse.saveFridgeItemEventually(fridgeItem, true, context, false);
            }
        }
        
        return newRowId;
    }
    
    public static Calendar stringToCalendar(String date, String format) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(format);
        Calendar cal = GregorianCalendar.getInstance();
        try {
            cal.setTime(dateFormat.parse(date));
        } catch (ParseException e) {
            cal = null;
        }
        
        return cal;
    }

    public static FridgeItem cursorToFridgeItem(Cursor c, Boolean isMinimal) {

        long rowId;
        String hash, foodItem, rawFoodItem, expiryDate, createdDate, updatedDate, updatedBy, image, imageBinarized;
        int fromImage, dismissed, expired, editedCart, editedFridge, deletedCart, deletedFridge,
                notifiedPush, notifiedEmail;

        rowId = c.getLong(
                c.getColumnIndexOrThrow(DatabaseContract.FridgeTable._ID)
        );
        foodItem = c.getString(
                c.getColumnIndexOrThrow(DatabaseContract.FridgeTable.COLUMN_NAME_FOOD_ITEM)
        );
        expiryDate = c.getString(
                c.getColumnIndexOrThrow(DatabaseContract.FridgeTable.COLUMN_NAME_EXPIRY_DATE)
        );

        if (!isMinimal) {
            hash = c.getString(
                    c.getColumnIndexOrThrow(DatabaseContract.FridgeTable.COLUMN_NAME_HASH)
            );
            rawFoodItem = c.getString(
                    c.getColumnIndexOrThrow(DatabaseContract.FridgeTable.COLUMN_NAME_RAW_FOOD_ITEM)
            );
            createdDate = c.getString(
                    c.getColumnIndexOrThrow(DatabaseContract.FridgeTable.COLUMN_NAME_CREATED_DATE)
            );
            updatedDate = c.getString(
                    c.getColumnIndexOrThrow(DatabaseContract.FridgeTable.COLUMN_NAME_UPDATED_DATE)
            );
            updatedBy = c.getString(
                    c.getColumnIndexOrThrow(DatabaseContract.FridgeTable.COLUMN_NAME_UPDATED_BY)
            );
            fromImage = c.getInt(
                    c.getColumnIndexOrThrow(DatabaseContract.FridgeTable.COLUMN_NAME_FROM_IMAGE)
            );
            image = c.getString(
                    c.getColumnIndexOrThrow(DatabaseContract.FridgeTable.COLUMN_NAME_IMAGE)
            );
            imageBinarized = c.getString(
                    c.getColumnIndexOrThrow(DatabaseContract.FridgeTable.COLUMN_NAME_IMAGE_BINARIZED)
            );
            dismissed = c.getInt(
                    c.getColumnIndexOrThrow(DatabaseContract.FridgeTable.COLUMN_NAME_DISMISSED)
            );
            expired = c.getInt(
                    c.getColumnIndexOrThrow(DatabaseContract.FridgeTable.COLUMN_NAME_EXPIRED)
            );
            editedCart = c.getInt(
                    c.getColumnIndexOrThrow(DatabaseContract.FridgeTable.COLUMN_NAME_EDITED_CART)
            );
            editedFridge = c.getInt(
                    c.getColumnIndexOrThrow(DatabaseContract.FridgeTable.COLUMN_NAME_EDITED_FRIDGE)
            );
            deletedCart = c.getInt(
                    c.getColumnIndexOrThrow(DatabaseContract.FridgeTable.COLUMN_NAME_DELETED_CART)
            );
            deletedFridge = c.getInt(
                    c.getColumnIndexOrThrow(DatabaseContract.FridgeTable.COLUMN_NAME_DELETED_FRIDGE)
            );
            notifiedPush = c.getInt(
                    c.getColumnIndexOrThrow(DatabaseContract.FridgeTable.COLUMN_NAME_NOTIFIED_PUSH)
            );
            notifiedEmail = c.getInt(
                    c.getColumnIndexOrThrow(DatabaseContract.FridgeTable.COLUMN_NAME_NOTIFIED_EMAIL)
            );

            return new FridgeItem(rowId, hash, foodItem, rawFoodItem, expiryDate, createdDate, updatedDate, updatedBy,
                    fromImage, image, imageBinarized, dismissed, expired, editedCart, editedFridge, deletedCart,
                    deletedFridge, notifiedPush, notifiedEmail);
        }

        return new FridgeItem(rowId, foodItem, expiryDate);

    }

    /*
        Gets the first item in the data base that matches the query

        Example
            To get a row by its id:
                getFirst(DatabaseContract.FridgeTable._ID, "=", "5", true);

     */
    public FridgeItem getFirst(String column, String operator, String value, boolean isMinimal) {
        SQLiteDatabase db = this.getReadableDatabase();

        String[] projection = getColumns(isMinimal);

        String whereColumn = column + operator + DatabaseContract.QUESTION_MARK;
        String[] whereValue = { value };

        Cursor c = db.query(
                DatabaseContract.FridgeTable.TABLE_NAME,  // The table to query
                projection,                               // The columns to return
                whereColumn,                              // The columns for the WHERE clause
                whereValue,                               // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                null                                      // The sort order
        );
        if (c.moveToFirst()) {
            return cursorToFridgeItem(c, isMinimal);
        } else {
            return null;
        }

    }

    /*
        Returns an array of items in the data base that matches the query

        Note
            conjunction should always have N-1 elements, where N is the number of elements in column/operator

        Example
            To get all items with expiry date before Jan. 1, 2014 and haven't been dismissed:
                dbHelper.getAll(new String[]{DatabaseContract.FridgeTable.COLUMN_NAME_EXPIRY_DATE, DatabaseContract.FridgeTable.COLUMN_NAME_DISMISSED}, new String[]{"<=", "="}, new String[]{"2014-01-01", DatabaseContract.BOOL_FALSE_STR}, new String[]{DatabaseContract.AND}, true);

     */
    public List<FridgeItem> getAll(String[] column, String[] operator, String[] whereValue, String[] conjunction, boolean isMinimal) {
        SQLiteDatabase db = this.getReadableDatabase();

        String[] projection = getColumns(isMinimal);

        String whereColumn = "";
        int n = column.length;

        for (int i = 0; i < n; ++i) {
            String conj = (i == n-1) ? "" : " " + conjunction[i] + " ";

            whereColumn += column[i] + operator[i] + DatabaseContract.QUESTION_MARK + conj;
        }

        Cursor c = db.query(
                DatabaseContract.FridgeTable.TABLE_NAME,  // The table to query
                projection,                               // The columns to return
                whereColumn,                              // The columns for the WHERE clause
                whereValue,                               // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                null                                      // The sort order
        );
        c.moveToFirst();

        List<FridgeItem> fridgeItems = new ArrayList<FridgeItem>();

        while (!c.isAfterLast()) {
            // When syncing by email, only add if the item's email matches the email

            fridgeItems.add(cursorToFridgeItem(c, isMinimal));
            c.moveToNext();
        }

        return fridgeItems;
    }

    public FridgeItem getRowById(long rowId, boolean isMinimal) {
        return getFirst(DatabaseContract.FridgeTable._ID, "=", Long.toString(rowId), isMinimal);
    }

    private String[] getColumns(boolean isMinimal) {
        String[] columnsMinimal = {
                DatabaseContract.FridgeTable._ID,
                DatabaseContract.FridgeTable.COLUMN_NAME_FOOD_ITEM,
                DatabaseContract.FridgeTable.COLUMN_NAME_EXPIRY_DATE,
        };

        String[] columns = {
                DatabaseContract.FridgeTable._ID,
                DatabaseContract.FridgeTable.COLUMN_NAME_HASH,
                DatabaseContract.FridgeTable.COLUMN_NAME_FOOD_ITEM,
                DatabaseContract.FridgeTable.COLUMN_NAME_RAW_FOOD_ITEM,
                DatabaseContract.FridgeTable.COLUMN_NAME_EXPIRY_DATE,
                DatabaseContract.FridgeTable.COLUMN_NAME_CREATED_DATE,
                DatabaseContract.FridgeTable.COLUMN_NAME_UPDATED_DATE,
                DatabaseContract.FridgeTable.COLUMN_NAME_EMAIL,
                DatabaseContract.FridgeTable.COLUMN_NAME_UPDATED_BY,
                DatabaseContract.FridgeTable.COLUMN_NAME_FROM_IMAGE,
                DatabaseContract.FridgeTable.COLUMN_NAME_IMAGE,
                DatabaseContract.FridgeTable.COLUMN_NAME_IMAGE_BINARIZED,
                DatabaseContract.FridgeTable.COLUMN_NAME_DISMISSED,
                DatabaseContract.FridgeTable.COLUMN_NAME_EXPIRED,
                DatabaseContract.FridgeTable.COLUMN_NAME_EDITED_CART,
                DatabaseContract.FridgeTable.COLUMN_NAME_EDITED_FRIDGE,
                DatabaseContract.FridgeTable.COLUMN_NAME_DELETED_CART,
                DatabaseContract.FridgeTable.COLUMN_NAME_DELETED_FRIDGE,
                DatabaseContract.FridgeTable.COLUMN_NAME_NOTIFIED_PUSH,
                DatabaseContract.FridgeTable.COLUMN_NAME_NOTIFIED_EMAIL,
        };

        if (isMinimal) {
            return columnsMinimal;
        } else {
            return columns;
        }

    }

    public Cursor read(String sortBy) {
        SQLiteDatabase db = this.getReadableDatabase();
        
        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = getColumns(false);

        // How you want the results sorted in the resulting Cursor
        String sortOrder;

        if (sortBy != null) {
            sortOrder = sortBy;
        } else {
            sortOrder =
                    DatabaseContract.FridgeTable.COLUMN_NAME_EXPIRY_DATE + DatabaseContract.COMMA_SEP +
                    DatabaseContract.FridgeTable.COLUMN_NAME_FOOD_ITEM + " ASC";
        }

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

    public void update(long rowId, String foodItem, Calendar expiryDate, Integer dismissed, Integer expired, Integer editedCart, Integer editedFridge,
                       Integer deletedCart, Integer deletedFridge, Integer notifiedPush, Integer notifiedEmail) {
        SQLiteDatabase db = this.getReadableDatabase();
        
        ContentValues values = new ContentValues();

        // Update value for each column (null means don't change the value)
        if (foodItem != null) {
            values.put(DatabaseContract.FridgeTable.COLUMN_NAME_FOOD_ITEM, foodItem);
        }
        if (expiryDate != null) {
            values.put(DatabaseContract.FridgeTable.COLUMN_NAME_EXPIRY_DATE, calendarToString(expiryDate, DatabaseContract.FORMAT_DATE));
        }
        if (dismissed != null) {
            values.put(DatabaseContract.FridgeTable.COLUMN_NAME_DISMISSED, dismissed);
        }
        if (expired != null) {
            values.put(DatabaseContract.FridgeTable.COLUMN_NAME_EXPIRED, expired);
        }
        if (editedCart != null) {
            values.put(DatabaseContract.FridgeTable.COLUMN_NAME_EDITED_CART, editedCart);
        }
        if (editedFridge != null) {
            values.put(DatabaseContract.FridgeTable.COLUMN_NAME_EDITED_FRIDGE, editedFridge);
        }
        if (deletedCart != null) {
            values.put(DatabaseContract.FridgeTable.COLUMN_NAME_DELETED_CART, deletedCart);
        }
        if (deletedFridge != null) {
            values.put(DatabaseContract.FridgeTable.COLUMN_NAME_DELETED_FRIDGE, deletedFridge);
        }
        if (notifiedPush != null) {
            values.put(DatabaseContract.FridgeTable.COLUMN_NAME_NOTIFIED_PUSH, notifiedPush);
        }
        if (notifiedEmail != null) {
            values.put(DatabaseContract.FridgeTable.COLUMN_NAME_NOTIFIED_EMAIL, notifiedEmail);
        }
        values.put(DatabaseContract.FridgeTable.COLUMN_NAME_UPDATED_DATE, calendarToString(GregorianCalendar.getInstance(), DatabaseContract.FORMAT_DATETIME));
        values.put(DatabaseContract.FridgeTable.COLUMN_NAME_UPDATED_BY, "DEVICE");

        // Which row to update, based on the ID
        String selection = DatabaseContract.FridgeTable._ID + " LIKE ?";
        String[] selectionArgs = { String.valueOf(rowId) };

        int count = db.update(
            DatabaseContract.FridgeTable.TABLE_NAME,
            values,
            selection,
            selectionArgs);

        // Save to cloud
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this.context);
        boolean auto = sharedPref.getBoolean(SettingsActivity.PREF_CHECKBOX_AUTO, true);
        if (auto) {
            MyParse.saveFridgeItemEventually(getRowById(rowId, false), false, context, false);
        }

    }
}