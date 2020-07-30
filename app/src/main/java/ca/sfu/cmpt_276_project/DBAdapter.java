package ca.sfu.cmpt_276_project;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;


// TO USE:
// Change the package (at top) to match your project.
public class DBAdapter {

    /////////////////////////////////////////////////////////////////////
    //	Constants & Data
    /////////////////////////////////////////////////////////////////////
    // For logging:
    private static final String TAG = "DBAdapter";

    // DB Fields
    public static final String KEY_ROWID = "_id";
    public static final int COL_ROWID = 0;
    /*
     * CHANGE 1:
     */
    // TODO: Setup your fields here:
    public static final String KEY_TRACK_NUM = "trackNumber";
    public static final String KEY_RES_NAME = "restaurantName";
    public static final String KEY_ADDRESS = "physicalAddress";
    public static final String KEY_CITY = "physicalCity";
    public static final String KEY_FAC_TYPE = "facType";
    public static final String KEY_LATITUDE = "latitude";
    public static final String KEY_LONGITUDE = "longitude";
    public static final String KEY_ICON = "icon";
    public static final String KEY_INSPECTION = "icon";

    // TODO: Setup your field numbers here (0 = KEY_ROWID, 1=...)
    public static final int COL_TRACK_NUM = 1;
    public static final int COL_RES_NAME  = 2;
    public static final int COL_ADDRESS = 3;
    public static final int COL_CITY = 4;
    public static final int COL_FAC_TYPE = 5;
    public static final int COL_LATITUDE = 6;
    public static final int COL_LONGITUDE = 7;
    public static final int COL_ICON = 8;
    public static final int COL_INSPECTION = 8;

    public static final String[] ALL_KEYS = new String[] {KEY_ROWID, KEY_TRACK_NUM, KEY_RES_NAME,
            KEY_ADDRESS, KEY_CITY, KEY_FAC_TYPE, KEY_LATITUDE, KEY_LONGITUDE, KEY_ICON, KEY_INSPECTION};

    // DB info: it's name, and the table we are using (just one).
    public static final String DATABASE_NAME = "MyDb";
    public static final String DATABASE_TABLE = "mainTable";
    // Track DB version if a new version of your app changes the format.
    public static final int DATABASE_VERSION = 2;

    private static final String DATABASE_CREATE_SQL =
            "create table " + DATABASE_TABLE
                    + " (" + KEY_ROWID + " integer primary key autoincrement, "

                    /*
                     * CHANGE 2:
                     */
                    // TODO: Place your fields here!
                    // + KEY_{...} + " {type} not null"
                    //	- Key is the column name you created above.
                    //	- {type} is one of: text, integer, real, blob
                    //		(http://www.sqlite.org/datatype3.html)
                    //  - "not null" means it is a required field (must be given a value).
                    // NOTE: All must be comma separated (end of line!) Last one must have NO comma!!
                    + KEY_TRACK_NUM + " text not null, "
                    + KEY_RES_NAME + " text not null, "
                    + KEY_ADDRESS + " text not null, "
                    + KEY_CITY + " text not null, "
                    + KEY_FAC_TYPE + " text not null, "
                    + KEY_LATITUDE + " real not null, "
                    + KEY_LONGITUDE + " real not null, "
//                    + KEY_ICON + " integer not null,"
                    + KEY_INSPECTION + " text not null"

                    // Rest  of creation:
                    + ");";

    // Context of application who uses us.
    private final Context context;

    private DatabaseHelper myDBHelper = null;
    private SQLiteDatabase db;

    /////////////////////////////////////////////////////////////////////
    //	Public methods:
    /////////////////////////////////////////////////////////////////////

    public DBAdapter(Context ctx) {
        this.context = ctx;
        myDBHelper = new DatabaseHelper(context);
    }

    // Open the database connection.
    public DBAdapter open() {
        db = myDBHelper.getWritableDatabase();
        return this;
    }

    // Close the database connection.
    public void close() {
        myDBHelper.close();
    }

    // Add a new set of values to the database.
    public long insertRow(String trackNumber,
            String restaurantName,
            String physicalAddress,
            String physicalCity,
            String facType,
            double latitude,
            double longitude,
            int icon, String inspectionJSON) {
        /*
         * CHANGE 3:
         */
        // TODO: Update data in the row with new fields.
        // TODO: Also change the function's arguments to be what you need!
        // Create row's data:
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_TRACK_NUM, trackNumber);
        initialValues.put(KEY_RES_NAME, restaurantName);
        initialValues.put(KEY_ADDRESS, physicalAddress);
        initialValues.put(KEY_CITY, physicalCity);
        initialValues.put(KEY_FAC_TYPE, facType);
        initialValues.put(KEY_LATITUDE, latitude);
        initialValues.put(KEY_LONGITUDE, longitude);
//        initialValues.put(KEY_ICON, icon);
        initialValues.put(KEY_INSPECTION, inspectionJSON);

        // Insert it into the database.
        return db.insert(DATABASE_TABLE, null, initialValues);
    }

    // Delete a row from the database, by rowId (primary key)
    public boolean deleteRow(long rowId) {
        String where = KEY_ROWID + "=" + rowId;
        return db.delete(DATABASE_TABLE, where, null) != 0;
    }

    public void deleteAll() {
//        Cursor c = getAllRows();
//        long rowId = c.getColumnIndexOrThrow(KEY_ROWID);
//        int limit = 0;
//
//        while (c.moveToFirst()) {
//            do {
//                deleteRow(c.getLong((int) rowId));
//            } while (c.moveToNext());
//        }
//        c.close();
        myDBHelper = new DBAdapter.DatabaseHelper(context);
        db = myDBHelper.getWritableDatabase();
        db.delete(DATABASE_TABLE,null,null);

        //Resets ID sequence to 0
        db.execSQL("DELETE FROM SQLITE_SEQUENCE WHERE NAME = '" + DATABASE_TABLE + "'");
        db.execSQL("UPDATE SQLITE_SEQUENCE SET SEQ = 0 WHERE NAME = '" + DATABASE_TABLE + "'");
    }

    // Return all data in the database.
    public Cursor getAllRows() {
        String where = null;
        Cursor c = 	db.query(true, DATABASE_TABLE, ALL_KEYS,
                where, null, null, null, null, null);
        if (c != null) {
            c.moveToFirst();
        }
        return c;
    }

    // Get a specific row (by rowId)
    public Cursor getRow(long rowId) {
        String where = KEY_ROWID + "=" + rowId;
        Cursor c = 	db.query(true, DATABASE_TABLE, ALL_KEYS,
                where, null, null, null, null, null);
        if (c != null) {
            c.moveToFirst();
        }
        return c;
    }

    // Change an existing row to be equal to new data.
    public boolean updateRow(long rowId, String trackNumber,
                             String restaurantName,
                             String physicalAddress,
                             String physicalCity,
                             String facType,
                             double latitude,
                             double longitude,
                             int icon, String inspectionJSON) {
        String where = KEY_ROWID + "=" + rowId;

        /*
         * CHANGE 4:
         */
        // TODO: Update data in the row with new fields.
        // TODO: Also change the function's arguments to be what you need!
        // Create row's data:
        ContentValues newValues = new ContentValues();
        newValues.put(KEY_TRACK_NUM, trackNumber);
        newValues.put(KEY_RES_NAME, restaurantName);
        newValues.put(KEY_ADDRESS, physicalAddress);
        newValues.put(KEY_CITY, physicalCity);
        newValues.put(KEY_FAC_TYPE, facType);
        newValues.put(KEY_LATITUDE, latitude);
        newValues.put(KEY_LONGITUDE, longitude);
//        newValues.put(KEY_ICON, icon);
        newValues.put(KEY_INSPECTION, inspectionJSON);

        // Insert it into the database.
        return db.update(DATABASE_TABLE, newValues, where, null) != 0;
    }



    /////////////////////////////////////////////////////////////////////
    //	Private Helper Classes:
    /////////////////////////////////////////////////////////////////////

    /**
     * Private class which handles database creation and upgrading.
     * Used to handle low-level database access.
     */
    private static class DatabaseHelper extends SQLiteOpenHelper
    {
        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase _db) {
            _db.execSQL(DATABASE_CREATE_SQL);
        }

        @Override
        public void onUpgrade(SQLiteDatabase _db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading application's database from version " + oldVersion
                    + " to " + newVersion + ", which will destroy all old data!");

            // Destroy old database:
            _db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE);

            // Recreate new database:
            onCreate(_db);
        }
    }
}