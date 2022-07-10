package com.cr7.budgetapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class SQLiteDatabase extends SQLiteOpenHelper {
    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "budget.db";

    // Table Names
    private static final String TABLE_ITEMS = "items";
    private static final String TABLE_ITEMS_TO_UPLOAD = "itemstoupload";
    private static final String TABLE_ITEMS_TO_DELETE = "itemstodelete";
    private static final String TABLE_LAUNDRY = "laundry";
    private static final String TABLE_LAUNDRY_TO_UPLOAD = "laundrytoupload";
    private static final String TABLE_LAUNDRY_TO_DELETE = "laundrytodelete";

    // Table Columns
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_DATE = "date";
    private static final String COLUMN_OBJECT_ID = "objectId";
    private static final String COLUMN_ITEM = "item";
    private static final String COLUMN_PRICE = "price";
    private static final String COLUMN_NUMBER_OF_LAUNDRY = "numberoflaundry";

    // SQL command to create the tables
    /*
     * **************************************************************************************************************************************************** */
    /*
     * **************************************************************************************************************************************************** */
    /* ***************************************NOTE: Here "items" and "laundry" will be referred to as the main table*************************************** */
    /*
     * **************************************************************************************************************************************************** */
    /*
     * **************************************************************************************************************************************************** */
    private static final String CREATE_NOTES_TABLE =
            "CREATE TABLE " + TABLE_ITEMS + "(" + COLUMN_ID +
                    " INTEGER PRIMARY KEY, " + COLUMN_DATE + " INTEGER, " +
                    COLUMN_OBJECT_ID + " TEXT, " + COLUMN_ITEM + " TEXT, " + COLUMN_PRICE + " " +
                    "INTEGER " + ")";

    private static final String CREATE_NOTES_TABLE_TO_UPLOAD =
            "CREATE TABLE " + TABLE_ITEMS_TO_UPLOAD + "(" + COLUMN_ID +
                    " INTEGER PRIMARY KEY, " + COLUMN_DATE + " INT, " +
                    COLUMN_OBJECT_ID + " TEXT, " + COLUMN_ITEM + " TEXT, " + COLUMN_PRICE + " " +
                    "INTEGER " + ")";

    private static final String CREATE_NOTES_TABLE_TO_DELETE =
            "CREATE TABLE " + TABLE_ITEMS_TO_DELETE + "(" + COLUMN_ID +
                    " INTEGER PRIMARY KEY, " + COLUMN_DATE + " INTEGER, " +
                    COLUMN_OBJECT_ID + " TEXT, " + COLUMN_ITEM + " TEXT, " + COLUMN_PRICE + " " +
                    "INTEGER " + ")";

    private static final String CREATE_LAUNDRY_TABLE =
            "CREATE TABLE " + TABLE_LAUNDRY + "(" + COLUMN_ID +
                    " INTEGER PRIMARY KEY, " + COLUMN_DATE + " INTEGER NOT NULL, " +
                    COLUMN_OBJECT_ID + " TEXT, " + COLUMN_NUMBER_OF_LAUNDRY + " INTEGER " + ")";

    private static final String CREATE_LAUNDRY_TABLE_TO_UPLOAD =
            "CREATE TABLE " + TABLE_LAUNDRY_TO_UPLOAD + "(" + COLUMN_ID +
                    " INTEGER PRIMARY KEY, " + COLUMN_DATE + " INTEGER NOT NULL, " +
                    COLUMN_OBJECT_ID + " TEXT, " + COLUMN_NUMBER_OF_LAUNDRY + " INTEGER " + ")";

    private static final String CREATE_LAUNDRY_TABLE_TO_DELETE =
            "CREATE TABLE " + TABLE_LAUNDRY_TO_DELETE + "(" + COLUMN_ID +
                    " INTEGER PRIMARY KEY, " + COLUMN_DATE + " INTEGER NOT NULL, " +
                    COLUMN_OBJECT_ID + " TEXT, " + COLUMN_NUMBER_OF_LAUNDRY + " INTEGER " + ")";

    private android.database.sqlite.SQLiteDatabase database = this.getWritableDatabase();

    public SQLiteDatabase(Context context) {
        super(context, /*name if the database*/DATABASE_NAME, /*cursor factory*/null,/*version of
         the database*/ DATABASE_VERSION);
    }

    @Override
    public void onCreate(android.database.sqlite.SQLiteDatabase sqLiteDatabase) {
        // On creating the database it will create the tables
        sqLiteDatabase.execSQL(CREATE_NOTES_TABLE);
        sqLiteDatabase.execSQL(CREATE_NOTES_TABLE_TO_DELETE);
        sqLiteDatabase.execSQL(CREATE_NOTES_TABLE_TO_UPLOAD);
        sqLiteDatabase.execSQL(CREATE_LAUNDRY_TABLE);
        sqLiteDatabase.execSQL(CREATE_LAUNDRY_TABLE_TO_UPLOAD);
        sqLiteDatabase.execSQL(CREATE_LAUNDRY_TABLE_TO_DELETE);
    }

    @Override
    public void onUpgrade(android.database.sqlite.SQLiteDatabase sqLiteDatabase, int oldVersion,
                          int newVersion) {
        // on Upgrading the database drop the previous table if it exists
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_ITEMS);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_ITEMS_TO_UPLOAD);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_ITEMS_TO_DELETE);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_LAUNDRY);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_LAUNDRY_TO_UPLOAD);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_LAUNDRY_TO_DELETE);
    }

    // Method to add an Item to the database
    public void addItem(Item item) {
        // Creating ContentValues object which will store the values of the columns
        ContentValues values = new ContentValues();
        // We're not inserting date in the data type date as SQLite doesn't support date hence 
        // we're adding it as an integer which holds the number of days from 1st January 1970
        // Putting the objectId column as null as in the LoggedInActivity all notes with null 
        // objectIds will be uploaded to the server
        values.putNull(COLUMN_OBJECT_ID);
        values.put(COLUMN_DATE, item.getDate());
        values.put(COLUMN_ITEM, item.getItem());
        values.put(COLUMN_PRICE, item.getPrice());
        // Inserting the ContentValue object i.e. the values to the database
        database.insert(/*Table Name*/TABLE_ITEMS_TO_UPLOAD, null, /*ContentValues*/values);
    }

    // This method will get all records of the particular date passed to it as a parameter
    public List<Item> getAllItems(int date) {
        // Creating a list of items
        List<Item> itemList = new ArrayList<>();
        // Creating a raw SQL query if you don't understand this learn SQL and come and if you 
        // still don't understand leave programming
        String selectFromItemsQuery = "SELECT * FROM " + TABLE_ITEMS + " WHERE " + COLUMN_DATE +
                " = " + date + ";";
        // Creating a Cursor object that is going to hold the queries of the raw query mentioned 
        // above
        Cursor cursor = database.rawQuery(selectFromItemsQuery, null);
        // Checking if cursor is not null as cursor.moveToFirst checks whether cursor is null and 
        // returns a boolean value based on that(if null false else true). It also moves to the 
        // first query.
        if (cursor.moveToFirst()) {
            do {
                Item item = new Item(Integer.parseInt(cursor.getString(0)),
                        Integer.parseInt(cursor.getString(1)), cursor.getString(2),
                        cursor.getString(3), Integer.parseInt(cursor.getString(4)));
                itemList.add(item);
            } while (cursor.moveToNext())/*i.e while the next query exist*/;
        }
        String selectFromItemsToUploadQuery = "SELECT * FROM " + TABLE_ITEMS_TO_UPLOAD + " WHERE "
                + COLUMN_DATE + " = " + date + ";";
        // Creating a Cursor object that is going to hold the queries of the raw query mentioned 
        // above
        cursor = database.rawQuery(selectFromItemsToUploadQuery, null);
        // Checking if cursor is not null as cursor.moveToFirst checks whether cursor is null and 
        // returns a boolean value based on that(if null false else true). It also moves to the 
        // first query.
        if (cursor.moveToFirst()) {
            do {
                Item item = new Item(Integer.parseInt(cursor.getString(0)),
                        Integer.parseInt(cursor.getString(1)), cursor.getString(2),
                        cursor.getString(3), Integer.parseInt(cursor.getString(4)));
                itemList.add(item);
            } while (cursor.moveToNext())/*i.e while the next query exist*/;
        }
        cursor.close();
        // Returning the list of notes
        return itemList;
    }

    public void updateItem(Item item) {
        if (item.getObjectId() != null) {
            // Creating a raw SQL query if you don't understand this learn SQL and come and if you
            // still don't understand leave programming
            String selectObjectIdFromItemsQuery =
                    "SELECT * FROM " + TABLE_ITEMS + " WHERE " + COLUMN_OBJECT_ID + " = \"" + item.getObjectId() + "\";";
            // Creating a Cursor object that is going to hold the queries of the raw query 
            // mentioned above
            Cursor cursor = database.rawQuery(selectObjectIdFromItemsQuery, null);
            // Checking if the Item is present in the main table
            if (cursor.moveToFirst()) {
                // Deleting the item from the main table and inserting it into the "itemstoupload"
                // table (by the way the delete function also returns a integer value showing the
                // number of rows it has affected)
                database.delete(TABLE_ITEMS, COLUMN_OBJECT_ID + " = ?",
                        new String[]{item.getObjectId()});
                // Creating content values to store the updated values of the column
                ContentValues values = new ContentValues();
                values.put(COLUMN_DATE, item.getDate());
                values.put(COLUMN_OBJECT_ID, item.getObjectId());
                values.put(COLUMN_ITEM, item.getItem());
                values.put(COLUMN_PRICE, item.getPrice());
                database.insert(/*Table Name*/TABLE_ITEMS_TO_UPLOAD, null, /*ContentValues*/values);
            } /* If it is not present in the main table it has to be present in the 
            "itemstoupload" table*/ else {
                // Creating content values to store the updated values of the column
                ContentValues values = new ContentValues();
                values.put(COLUMN_ITEM, item.getItem());
                values.put(COLUMN_PRICE, item.getPrice());
                database.update(TABLE_ITEMS_TO_UPLOAD, values, COLUMN_OBJECT_ID + " = ? ",
                        new String[]{item.getObjectId()});
            }
            cursor.close();
        } /* If it doesn't have an object id there is no chance that it is present in the main 
        table as main table cannot have objectId as null as all objects have an object id */ else {
            // Creating content values to store the updated values of the column
            ContentValues values = new ContentValues();
            values.put(COLUMN_ITEM, item.getItem());
            values.put(COLUMN_PRICE, item.getPrice());
            // Updating the database
            database.update(TABLE_ITEMS_TO_UPLOAD, values, COLUMN_ID + " = ? ",
                    new String[]{String.valueOf(item.getId())});
        }
    }

    // Method to delete item from the database
    public void deleteItem(Item item) {
        if (item.getObjectId() != null) {
            // Creating a raw SQL query if you don't understand this learn SQL and come and if you
            // still don't understand leave programming
            String selectObjectIdFromItemsQuery =
                    "SELECT * FROM " + TABLE_ITEMS + " WHERE " + COLUMN_OBJECT_ID + " = \"" + item.getObjectId() + "\";";
            // Creating a Cursor object that is going to hold the queries of the raw query 
            // mentioned above
            Cursor cursor = database.rawQuery(selectObjectIdFromItemsQuery, null);
            // Checking if the Item is present in the main table
            if (cursor.moveToFirst()) {
                // Deleting the item from the main table and inserting it into the "itemstodelete"
                // table (by the way the delete function also returns a integer value showing the
                // number of rows it has affected)
                database.delete(TABLE_ITEMS, COLUMN_OBJECT_ID + " = ?",
                        new String[]{item.getObjectId()});
                // Creating content values to store the updated values of the column
                ContentValues values = new ContentValues();
                values.put(COLUMN_DATE, item.getDate());
                values.put(COLUMN_OBJECT_ID, item.getObjectId());
                values.put(COLUMN_ITEM, item.getItem());
                values.put(COLUMN_PRICE, item.getPrice());
                database.insert(/*Table Name*/TABLE_ITEMS_TO_DELETE, null, /*ContentValues*/values);
            } /* If it is not present in the main table it has to be present in the 
            "itemstoupload" table */ else {
                // Deleting the note (by the way the delete function also returns a integer value 
                // showing the number of rows it has affected)
                database.delete(TABLE_ITEMS_TO_UPLOAD, COLUMN_OBJECT_ID + " = ?",
                        new String[]{item.getObjectId()});
                // Creating content values to store the updated values of the column
                ContentValues values = new ContentValues();
                values.put(COLUMN_DATE, item.getDate());
                values.put(COLUMN_OBJECT_ID, item.getObjectId());
                values.put(COLUMN_ITEM, item.getItem());
                values.put(COLUMN_PRICE, item.getPrice());
                database.insert(/*Table Name*/TABLE_ITEMS_TO_DELETE, null, /*ContentValues*/values);
            }
            cursor.close();
        }/* If it doesn't have an object id there is no chance that it is present in the main 
        table as main table cannot have objectId as null as all objects have an object id*/ else {
            database.delete(TABLE_ITEMS_TO_UPLOAD, COLUMN_ID + " = ?",
                    new String[]{String.valueOf(item.getId())});
        }

    }

    // Method that will move an item from itemstoupload table to the main table after it is 
    // uploaded to the server
    public void moveToMain(Item item) {
        // Deleting the item from the "itemstoupload" table and inserting it into the
        // main table (by the way the delete function also returns a integer value showing the
        // number of rows it has affected)
        database.delete(TABLE_ITEMS_TO_UPLOAD, COLUMN_ID + " = ?",
                new String[]{String.valueOf(item.getId())});
        // Creating content values to store the values of the column from the Item object
        ContentValues values = new ContentValues();
        values.put(COLUMN_DATE, item.getDate());
        values.put(COLUMN_OBJECT_ID, item.getObjectId());
        values.put(COLUMN_ITEM, item.getItem());
        values.put(COLUMN_PRICE, item.getPrice());
        database.insert(/*Table Name*/TABLE_ITEMS, null, /*ContentValues*/values);
    }

    // Gets all items from the itemstoupload table and sends them to be uploaded
    public List<Item> getToUploadItems() {
        List<Item> items = new ArrayList<>();
        String selectFromItemsQuery = "SELECT * FROM " + TABLE_ITEMS_TO_UPLOAD + ";";
        // Creating a Cursor object that is going to hold the queries of the raw query mentioned 
        // above
        Cursor cursor = database.rawQuery(selectFromItemsQuery, null);
        // Checking if cursor is not null as cursor.moveToFirst checks whether cursor is null and 
        // returns a boolean value based on that(if null false else true). It also moves to the 
        // first query.
        if (cursor.moveToFirst()) {
            do {
                Item item = new Item(Integer.parseInt(cursor.getString(0)),
                        Integer.parseInt(cursor.getString(1)), cursor.getString(2),
                        cursor.getString(3), Integer.parseInt(cursor.getString(4)));
                items.add(item);
            } while (cursor.moveToNext())/*i.e while the next query exist*/;
        }
        cursor.close();
        return items;
    }

    // Checks if an Item is present in either the itemstodelete table or the itemstoupload table 
    // as we do not want to overwrite them in the main table while syncing with the server
    public List<String> getObjectIdFromToUploadTableOrDeleteTable() {
        List<String> objectIds = new ArrayList<>();
        String selectFromItemsQueryToUpload =
                "SELECT " + COLUMN_OBJECT_ID + " FROM " + TABLE_ITEMS_TO_UPLOAD + ";";
        String selectFromItemsQueryToDelete =
                "SELECT " + COLUMN_OBJECT_ID + " FROM " + TABLE_ITEMS_TO_DELETE + ";";
        // Creating Cursor objects that are going to hold the rows of the raw queries mentioned
        // above
        Cursor cursor = database.rawQuery(selectFromItemsQueryToUpload, null);
        Cursor cursor1 = database.rawQuery(selectFromItemsQueryToDelete, null);
        // Checking if cursor is not null as cursor.moveToFirst checks whether cursor is null and 
        // returns a boolean value based on that(if null false else true). It also moves to the 
        // first query.
        if (cursor.moveToFirst()) {
            do {
                objectIds.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }
        if (cursor1.moveToFirst()) {
            do {
                objectIds.add(cursor1.getString(0));
            } while (cursor1.moveToNext());
        }
        cursor.close();
        cursor1.close();
        return objectIds;
    }

    // Method for adding the note directly from the server
    public void addToMainTable(List<Item> items) {
        for (Item item : items) {
            // Creating ContentValues object which will store the values of the columns
            ContentValues values = new ContentValues();
            values.put(COLUMN_OBJECT_ID, item.getObjectId());
            // We're not inserting date in the data type date as SQLite doesn't support date hence
            // we're adding it as an integer which holds the number of days from 1st January 1970
            values.put(COLUMN_DATE, item.getDate());
            values.put(COLUMN_ITEM, item.getItem());
            values.put(COLUMN_PRICE, item.getPrice());
            // Inserting the ContentValue object i.e. the values to the database
            database.insert(/*Table Name*/TABLE_ITEMS, null, /*ContentValues*/values);
        }
    }

    // Method to get all items from the itemstodelete table so that it can be deleted from the
    // server
    public List<Item> getItemsToDelete() {
        // Creating a list of items which we will return
        List<Item> items = new ArrayList<>();
        String selectFromItemsQuery = "SELECT * FROM " + TABLE_ITEMS_TO_DELETE + ";";
        // Creating a Cursor object that is going to hold the rows of the raw query mentioned
        // above
        Cursor cursor = database.rawQuery(selectFromItemsQuery, null);
        // Checking if cursor is not null as cursor.moveToFirst checks whether cursor is null and 
        // returns a boolean value based on that(if null false else true). It also moves to the 
        // first query.
        if (cursor.moveToFirst()) {
            do {
                Item item = new Item(Integer.parseInt(cursor.getString(0)),
                        Integer.parseInt(cursor.getString(1)), cursor.getString(2),
                        cursor.getString(3), Integer.parseInt(cursor.getString(4)));
                items.add(item);
            } while (cursor.moveToNext())/*i.e while the next query exist*/;
        }
        cursor.close();
        return items;
    }

    // Method to delete items from the itemstodelete table once the item has been deleted from the
    // server
    public void deleteItemFromToDeleteTable(Item item) {
        database.delete(TABLE_ITEMS_TO_DELETE, COLUMN_ID + " = ?",
                new String[]{String.valueOf(item.getId())});
    }

    // Method to get all objectIds of all the items in the itemstoupload table
    public List<String> getAllObjectIdsFromUploadTable() {
        List<String> objectIds = new ArrayList<>();
        // Creating a raw SQL query if you don't understand this learn SQL and come and if you 
        // still don't understand leave programming
        String selectFromItemsQuery =
                "SELECT " + COLUMN_OBJECT_ID + " FROM " + TABLE_ITEMS_TO_UPLOAD + " WHERE " + COLUMN_OBJECT_ID + " NOT NULL" + ";";
        // Creating a Cursor object that is going to hold the queries of the raw query mentioned 
        // above
        Cursor cursor = database.rawQuery(selectFromItemsQuery, null);
        // Checking if cursor is not null as cursor.moveToFirst checks whether cursor is null and 
        // returns a boolean value based on that(if null false else true). It also moves to the 
        // first query.
        if (cursor.moveToFirst()) {
            do {
                objectIds.add(cursor.getString(0));
            } while (cursor.moveToNext())/*i.e while the next query exist*/;
        }
        cursor.close();
        return objectIds;
    }

    // Method to get all objectIds of all the items in the itemstodelete table
    public List<String> getAllObjectIdsFromDeleteTable() {
        List<String> objectIds = new ArrayList<>();
        // Creating a raw SQL query if you don't understand this learn SQL and come and if you 
        // still don't understand leave programming
        String selectFromItemsQuery =
                "SELECT " + COLUMN_OBJECT_ID + " FROM " + TABLE_ITEMS_TO_DELETE + " WHERE " + COLUMN_OBJECT_ID + " NOT NULL" + ";";
        // Creating a Cursor object that is going to hold the queries of the raw query mentioned 
        // above
        Cursor cursor = database.rawQuery(selectFromItemsQuery, null);
        // Checking if cursor is not null as cursor.moveToFirst checks whether cursor is null and 
        // returns a boolean value based on that(if null false else true). It also moves to the 
        // first query.
        if (cursor.moveToFirst()) {
            do {
                objectIds.add(cursor.getString(0));
            } while (cursor.moveToNext())/*i.e while the next query exist*/;
        }
        cursor.close();
        return objectIds;
    }

    // Method to delete item from the itemstodelete table based on the object id
    public void deleteItemFromToDeleteTableByObjectId(List<String> objectIds) {
        for (String s : objectIds) {
            database.delete(TABLE_ITEMS_TO_DELETE, COLUMN_OBJECT_ID + " = ?",
                    new String[]{s});
        }
    }

    // Method to delete item's objectId from the itemstoupload table based on the object id
    public void deleteObjectIdFromItemInUploadTable(List<String> objectIds) {
        for (String s : objectIds) {
            ContentValues values = new ContentValues();
            values.putNull(COLUMN_OBJECT_ID);
            database.update(TABLE_ITEMS_TO_UPLOAD, values, COLUMN_OBJECT_ID + " = ? ",
                    new String[]{s});
        }
    }

    // Method to delete all entries whose dates match the parameter from the main table
    public void deleteAllFromMainTable(List<Integer> dates) {
        for (int s : dates) {
            database.delete(TABLE_ITEMS, COLUMN_DATE + " = ? ", new String[]{String.valueOf(s)});
        }
    }

    // This method will add a LaundryItem to the database
    public void addLaundry(LaundryItem item) {
        // Creating ContentValues object which will store the values of the columns
        ContentValues values = new ContentValues();
        // We're not inserting date in the data type date as SQLite doesn't support date hence 
        // we're adding it as an integer which holds the number of days from 1st January 1970
        // Putting the objectId column as null as in the LoggedInActivity all notes with null 
        // objectIds will be uploaded to the server
        values.putNull(COLUMN_OBJECT_ID);
        values.put(COLUMN_DATE, item.getDate());
        values.put(COLUMN_NUMBER_OF_LAUNDRY, item.getLaundry());
        // Inserting the ContentValue object i.e. the values to the database
        database.insert(/*Table Name*/TABLE_LAUNDRY_TO_UPLOAD, null, /*ContentValues*/values);
    }

    // Method for adding the note directly from the server
    public void addToMainLaundryTable(List<LaundryItem> items) {
        for (LaundryItem item : items) {
            // Creating ContentValues object which will store the values of the columns
            ContentValues values = new ContentValues();
            values.put(COLUMN_OBJECT_ID, item.getObjectId());
            // We're not inserting date in the data type date as SQLite doesn't support date hence
            // we're adding it as an integer which holds the number of days from 1st January 1970
            values.put(COLUMN_DATE, item.getDate());
            values.put(COLUMN_NUMBER_OF_LAUNDRY, item.getLaundry());
            // Inserting the ContentValue object i.e. the values to the database
            database.insert(/*Table Name*/TABLE_LAUNDRY, null, /*ContentValues*/values);
        }
    }

    // Updating the LaundryItem that has the same date as the LaundryItem in the parameter as
    // only one item can exist for a date
    public void updateLaundry(LaundryItem item) {
        // Creating a raw SQL query if you don't understand this learn SQL and come and if you 
        // still don't understand leave programming
        String selectObjectIdFromLaundryItemsQuery =
                "SELECT * FROM " + TABLE_LAUNDRY + " WHERE " + COLUMN_DATE + " = " + item.getDate() + ";";
        // Creating a Cursor object that is going to hold the queries of the raw query mentioned 
        // above
        Cursor cursor = database.rawQuery(selectObjectIdFromLaundryItemsQuery, null);
        // Checking if the LaundryItem is present in the main table
        if (cursor.moveToFirst()) {
            // Deleting the item from the main table and inserting it into the "laundrytoupload"
            // table (by the way the delete function also returns a integer value showing the 
            // number of rows it has affected)
            database.delete(TABLE_LAUNDRY, COLUMN_DATE + " = ?",
                    new String[]{String.valueOf(item.getDate())});
            // Creating content values to store the updated values of the column
            ContentValues values = new ContentValues();
            values.put(COLUMN_DATE, item.getDate());
            values.put(COLUMN_OBJECT_ID, cursor.getString(2));
            values.put(COLUMN_NUMBER_OF_LAUNDRY, item.getLaundry());
            database.insert(/*Table Name*/TABLE_LAUNDRY_TO_UPLOAD, null, /*ContentValues*/values);
        } /* If it is not present in the main table it has to be present in the "laundrytoupload"
        table*/ else {
            // Creating content values to store the updated values of the column
            ContentValues values = new ContentValues();
            values.put(COLUMN_NUMBER_OF_LAUNDRY, item.getLaundry());
            database.update(TABLE_LAUNDRY_TO_UPLOAD, values, COLUMN_DATE + " = ? ",
                    new String[]{String.valueOf(item.getDate())});
        }
        cursor.close();
    }

    // Method to get all laundry items
    public List<LaundryItem> getLaundry(int startDate, int endDate) {
        // Creating a list of LaundryItems
        List<LaundryItem> laundryItemList = new ArrayList<>();
        // Creating a raw SQL query if you don't understand this learn SQL and come and if you 
        // still don't understand leave programming
        String selectFromItemsQuery =
                "SELECT * FROM " + TABLE_LAUNDRY + " WHERE " + COLUMN_DATE + " >= " + startDate + " AND " + COLUMN_DATE +" <= " + endDate + ";";
        // Creating a Cursor object that is going to hold the queries of the raw query mentioned 
        // above
        Cursor cursor = database.rawQuery(selectFromItemsQuery, null);
        // Checking if cursor is not null as cursor.moveToFirst checks whether cursor is null and 
        // returns a boolean value based on that(if null false else true). It also moves to the 
        // first query.
        if (cursor.moveToFirst()) {
            do {
                LaundryItem item = new LaundryItem(Integer.parseInt(cursor.getString(0)),
                        Integer.parseInt(cursor.getString(1)), cursor.getString(2),
                        Integer.parseInt(cursor.getString(3)));
                laundryItemList.add(item);
            } while (cursor.moveToNext())/*i.e while the next query exist*/;
        }
        cursor.close();
        // Creating a raw SQL query if you don't understand this learn SQL and come and if you
        // still don't understand leave programming
        String selectFromItemsToUploadQuery =
                "SELECT * FROM " + TABLE_LAUNDRY_TO_UPLOAD + " WHERE " + COLUMN_DATE + " >= " + startDate + " AND " + COLUMN_DATE +" <= " + endDate + ";";
        // Creating a Cursor object that is going to hold the queries of the raw query mentioned 
        // above
        cursor = database.rawQuery(selectFromItemsToUploadQuery, null);
        // Checking if cursor is not null as cursor.moveToFirst checks whether cursor is null and 
        // returns a boolean value based on that(if null false else true). It also moves to the 
        // first query.
        if (cursor.moveToFirst()) {
            do {
                LaundryItem item = new LaundryItem(Integer.parseInt(cursor.getString(0)),
                        Integer.parseInt(cursor.getString(1)), cursor.getString(2),
                        Integer.parseInt(cursor.getString(3)));
                laundryItemList.add(item);
            } while (cursor.moveToNext())/*i.e while the next query exist*/;
        }
        cursor.close();
        return laundryItemList;
    }


    // Method that will move an item from laundrytoupload table to the main table after it is
    // uploaded to the server
    public void moveLaundryToMain(LaundryItem item) {
        database.delete(TABLE_LAUNDRY_TO_UPLOAD, COLUMN_ID + " = ?",
                new String[]{String.valueOf(item.getId())});
        ContentValues values = new ContentValues();
        values.put(COLUMN_DATE, item.getDate());
        values.put(COLUMN_OBJECT_ID, item.getObjectId());
        values.put(COLUMN_NUMBER_OF_LAUNDRY, item.getLaundry());
        database.insert(/*Table Name*/TABLE_LAUNDRY, null, /*ContentValues*/values);
    }

    // Gets all items from the laundrytoupload table and sends them to be uploaded
    public List<LaundryItem> getToUploadLaundry() {
        List<LaundryItem> items = new ArrayList<>();
        String selectFromItemsQuery = "SELECT * FROM " + TABLE_LAUNDRY_TO_UPLOAD + ";";
        // Getting readable database as we do not have to write anything in the database
        // Creating a Cursor object that is going to hold the queries of the raw query mentioned 
        // above
        Cursor cursor = database.rawQuery(selectFromItemsQuery, null);
        // Checking if cursor is not null as cursor.moveToFirst checks whether cursor is null and 
        // returns a boolean value based on that(if null false else true). It also moves to the 
        // first query.
        if (cursor.moveToFirst()) {
            do {
                LaundryItem item = new LaundryItem(Integer.parseInt(cursor.getString(0)),
                        Integer.parseInt(cursor.getString(1)), cursor.getString(2),
                        Integer.parseInt(cursor.getString(3)));
                items.add(item);
            } while (cursor.moveToNext())/*i.e while the next query exist*/;
        }
        cursor.close();
        return items;
    }

    // Method to get all objectIds of all the LaundryItems in the "laundrytoupload" table
    public List<String> getAllObjectIdsFromLaundryUploadTable() {
        List<String> objectIds = new ArrayList<>();
        // Creating a raw SQL query if you don't understand this learn SQL and come and if you 
        // still don't understand leave programming
        String selectFromItemsQuery =
                "SELECT " + COLUMN_OBJECT_ID + " FROM " + TABLE_LAUNDRY_TO_UPLOAD + " WHERE " + COLUMN_OBJECT_ID + " NOT NULL" + ";";
        // Getting readable database as we do not have to write anything in the database
        // Creating a Cursor object that is going to hold the queries of the raw query mentioned 
        // above
        Cursor cursor = database.rawQuery(selectFromItemsQuery, null);
        // Checking if cursor is not null as cursor.moveToFirst checks whether cursor is null and 
        // returns a boolean value based on that(if null false else true). It also moves to the 
        // first query.
        if (cursor.moveToFirst()) {
            do {
                objectIds.add(cursor.getString(0));
            } while (cursor.moveToNext())/*i.e while the next query exist*/;
        }
        cursor.close();
        return objectIds;
    }

    // Method to get all objectIds of all the LaundryItems in the laundrytodelete table
    public List<String> getAllObjectIdsFromLaundryDeleteTable() {
        List<String> objectIds = new ArrayList<>();
        // Creating a raw SQL query if you don't understand this learn SQL and come and if you 
        // still don't understand leave programming
        String selectFromItemsQuery =
                "SELECT " + COLUMN_OBJECT_ID + " FROM " + TABLE_LAUNDRY_TO_DELETE + " WHERE " + COLUMN_OBJECT_ID + " NOT NULL" + ";";
        // Creating a Cursor object that is going to hold the queries of the raw query mentioned 
        // above
        Cursor cursor = database.rawQuery(selectFromItemsQuery, null);
        // Checking if cursor is not null as cursor.moveToFirst checks whether cursor is null and 
        // returns a boolean value based on that(if null false else true). It also moves to the 
        // first query.
        if (cursor.moveToFirst()) {
            do {
                objectIds.add(cursor.getString(0));
            } while (cursor.moveToNext())/*i.e while the next query exist*/;
        }
        cursor.close();
        return objectIds;
    }

    // Method to delete all entries of the dates in the parameter from the main table
    public void deleteAllFromMainLaundryTable(int from, int to) {
        database.delete(TABLE_LAUNDRY, COLUMN_DATE + " >= ? AND " + COLUMN_DATE + " <= ?",
                new String[]{String.valueOf(from), String.valueOf(to)});
    }

    // Method to delete all entries of the dates in the parameter from the main table
    public void deleteAllFromMainLaundryTable(List<String> objectIds) {
        for (String objectId : objectIds)
            database.delete(TABLE_LAUNDRY, COLUMN_OBJECT_ID + " = ?",
                new String[]{objectId});
    }


    // Method to get the dates from LaundryItems from "laundrytoupload" and "laundrytodelete" tables
    public List<Integer> getDatesOfToUploadTableOrDeleteLaundryTable() {
        List<Integer> dates = new ArrayList<>();
        String selectFromItemsQueryToUpload =
                "SELECT " + COLUMN_DATE + " FROM " + TABLE_LAUNDRY_TO_UPLOAD + ";";
        String selectFromItemsQueryToDelete =
                "SELECT " + COLUMN_DATE + " FROM " + TABLE_LAUNDRY_TO_DELETE + ";";
        // Creating a Cursor object that is going to hold the queries of the raw query mentioned 
        // above
        Cursor cursor = database.rawQuery(selectFromItemsQueryToUpload, null);
        Cursor cursor1 = database.rawQuery(selectFromItemsQueryToDelete, null);
        if (cursor.moveToFirst()) {
            do {
                dates.add(Integer.parseInt(cursor.getString(0)));
            } while (cursor.moveToNext());
        }
        if (cursor1.moveToFirst()) {
            do {
                dates.add(Integer.parseInt(cursor1.getString(0)));
            } while (cursor1.moveToNext());
        }
        cursor.close();
        cursor1.close();
        // Checking if cursor is not null as cursor.moveToFirst checks whether cursor is null and 
        // returns a boolean value based on that(if null false else true). It also moves to the 
        // first query.
        return dates;
    }

    // Method to delete LaundryItem from the laundrytodelete table based on the object id
    public void deleteItemFromLaundryToDeleteTableByObjectId(List<String> objectIds) {
        for (String s : objectIds) {
            database.delete(TABLE_LAUNDRY_TO_DELETE, COLUMN_OBJECT_ID + " = ?",
                    new String[]{s});
        }
    }

    // Method to delete LaundryItem from the laundrytoupload table based on the object id
    public void deleteObjectIdFromLaundryInUploadTable(List<String> objectIds) {
        for (String s : objectIds) {
            ContentValues values = new ContentValues();
            values.putNull(COLUMN_OBJECT_ID);
            database.update(TABLE_LAUNDRY_TO_UPLOAD, values, COLUMN_OBJECT_ID + " = ? ",
                    new String[]{s});
        }
    }

    // Method to get all LaundryItems from the laundrytodelete table so that it can be deleted from
    // the server
    public List<LaundryItem> getLaundryToDelete() {
        // Creating a list of items which we will return
        List<LaundryItem> items = new ArrayList<>();
        String selectFromItemsQuery = "SELECT * FROM " + TABLE_LAUNDRY_TO_DELETE + ";";
        // Creating a Cursor object that is going to hold the queries of the raw query mentioned 
        // above
        Cursor cursor = database.rawQuery(selectFromItemsQuery, null);
        // Checking if cursor is not null as cursor.moveToFirst checks whether cursor is null and 
        // returns a boolean value based on that(if null false else true). It also moves to the 
        // first query.
        if (cursor.moveToFirst()) {
            do {
                LaundryItem item = new LaundryItem(Integer.parseInt(cursor.getString(0)),
                        Integer.parseInt(cursor.getString(1)), cursor.getString(2),
                        Integer.parseInt(cursor.getString(3)));
                items.add(item);
            } while (cursor.moveToNext())/*i.e while the next query exist*/;
        }
        cursor.close();
        return items;
    }

    // Method to delete items from the laundrytodelete table once the item has been deleted from the
    // server
    public void deleteLaundryFromToDeleteTable(LaundryItem item) {
        database.delete(TABLE_LAUNDRY_TO_DELETE, COLUMN_ID + " = ?",
                new String[]{String.valueOf(item.getId())});
    }

    // Method to delete a LaundryItem
    public void deleteLaundry(LaundryItem item) {
        if (item.getObjectId() != null) {
            // Creating a raw SQL query if you don't understand this learn SQL and come and if you
            // still don't understand leave programming
            String selectObjectIdFromItemsQuery =
                    "SELECT * FROM " + TABLE_LAUNDRY + " WHERE " + COLUMN_OBJECT_ID + " = '" + item.getObjectId() + "';";
            // Creating a Cursor object that is going to hold the queries of the raw query 
            // mentioned above
            Cursor cursor = database.rawQuery(selectObjectIdFromItemsQuery, null);
            // Checking if the Item is present in the main table
            if (cursor.moveToFirst()) {
                // Deleting the item from the main table and inserting it into the "itemstodelete"
                // table (by the way the delete function also returns a integer value showing the
                // number of rows it has affected)
                database.delete(TABLE_LAUNDRY, COLUMN_OBJECT_ID + " = ?",
                        new String[]{String.valueOf(item.getObjectId())});
                // Creating content values to store the updated values of the column
                ContentValues values = new ContentValues();
                values.put(COLUMN_DATE, item.getDate());
                values.put(COLUMN_OBJECT_ID, item.getObjectId());
                values.put(COLUMN_NUMBER_OF_LAUNDRY, item.getLaundry());
                database.insert(/*Table Name*/TABLE_LAUNDRY_TO_DELETE, null, /*ContentValues
                 */values);
            } /* If it is not present in the main table it has to be present in the 
            "itemstoupload" table */ else {
                // Deleting the note (by the way the delete function also returns a integer value 
                // showing the number of rows it has affected)
                database.delete(TABLE_LAUNDRY_TO_UPLOAD, COLUMN_DATE + " = ?",
                        new String[]{String.valueOf(item.getDate())});
                // Creating content values to store the updated values of the column
                ContentValues values = new ContentValues();
                values.put(COLUMN_DATE, item.getDate());
                values.put(COLUMN_OBJECT_ID, item.getObjectId());
                values.put(COLUMN_NUMBER_OF_LAUNDRY, item.getLaundry());
                database.insert(/*Table Name*/TABLE_LAUNDRY_TO_DELETE, null, /*ContentValues
                 */values);
            }
            cursor.close();
        }/* If it doesn't have an object id there is no chance that it is present in the main 
        table as main table cannot have objectId as null as all objects have an object id*/ else {
            database.delete(TABLE_LAUNDRY_TO_UPLOAD, COLUMN_DATE + " = ?",
                    new String[]{String.valueOf(item.getDate())});
        }
    }
}