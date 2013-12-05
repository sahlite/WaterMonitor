package com.yulinghb.watermonitor;

import java.util.HashMap;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

import com.yulinghb.watermonitor.RecorderContract.DataEntry;
import com.yulinghb.watermonitor.RecorderContract.LocationEntry;

/*
 * 数据库操作工具类
 */
public class RecorderProvider extends ContentProvider {
//	private static final String TAG = "RecorderProvider";

    private static HashMap<String, String> sLocationListProjectionMap;
    private static HashMap<String, String> sDataListProjectionMap;
    private static HashMap<String, String> sLocationItemProjectionMap;
    private static HashMap<String, String> sDataItemProjectionMap;
    private static HashMap<String, String> sLocationImageNameProjectionMap;
    
    private static final int LOCATION = 1;
    private static final int DATA = 2;
    private static final int LOCATION_ID = 3;
    private static final int DATA_ID = 4;
    private static final int LOCATION_IMAGE_NAME = 5;

    private static final UriMatcher sUriMatcher;

	public class DataOpenHelper extends SQLiteOpenHelper{
		public static final int DATABASE_VERSION = 1;
		public static final String DATABASE_NAME = "DataReader.db";
		
		public DataOpenHelper(Context context){
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			// TODO Auto-generated method stub
			db.execSQL(SQL_CREATE_ENTRIES);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int arg1, int arg2) {
			// TODO Auto-generated method stub
			db.execSQL(SQL_DELETE_ENTRIES);
			onCreate(db);
		}
		
		private static final String TEXT_TYPE = " TEXT";
		private static final String INTEGER_TYPE = " INTEGER";
		private static final String REAL_TYPE = " REAL";
		private static final String COMMA_SEP = ", ";
		private static final String SQL_CREATE_ENTRIES = 
						"CREATE TABLE " + DataEntry.TABLE_NAME + " (" +
						DataEntry._ID + " INTEGER PRIMARY KEY, " +
						DataEntry.COLUMN_NAME_LOCATION_ID + INTEGER_TYPE + COMMA_SEP +
						DataEntry.COLUMN_NAME_TIME + TEXT_TYPE + COMMA_SEP +
						DataEntry.COLUMN_NAME_TEMP + REAL_TYPE + COMMA_SEP +
						DataEntry.COLUMN_NAME_DEPTH + REAL_TYPE + COMMA_SEP +
						DataEntry.COLUMN_NAME_RDO + REAL_TYPE + COMMA_SEP +
						DataEntry.COLUMN_NAME_SAT + REAL_TYPE + COMMA_SEP +
						DataEntry.COLUMN_NAME_ORP + REAL_TYPE + COMMA_SEP +
						DataEntry.COLUMN_NAME_PH + REAL_TYPE + COMMA_SEP +
						DataEntry.COLUMN_NAME_ACT + REAL_TYPE + COMMA_SEP +
						DataEntry.COLUMN_NAME_BARO + REAL_TYPE + 
						"); ";
		private static final String SQL_DELETE_ENTRIES =
						"DROP TABLE IF EXISTS " + DataEntry.TABLE_NAME;
	}
	
	public class LocationOpenHelper extends SQLiteOpenHelper{
		public static final int DATABASE_VERSION = 2;
		public static final String DATABASE_NAME = "LocationReader.db";
		
		public LocationOpenHelper(Context context){
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			// TODO Auto-generated method stub
			db.execSQL(SQL_CREATE_ENTRIES);
			db.execSQL(SQL_INSERT_DEFAULE_ENTRIES);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int arg1, int arg2) {
			// TODO Auto-generated method stub
			db.execSQL(SQL_DELETE_ENTRIES);
			onCreate(db);
		}
		
		private static final String TEXT_TYPE = " TEXT";
		//private static final String INTEGER_TYPE = " INTEGER";
		private static final String REAL_TYPE = " REAL";
		private static final String COMMA_SEP = ", ";
		private static final String SQL_CREATE_ENTRIES = 
								"CREATE TABLE " + LocationEntry.TABLE_NAME + " (" +
								LocationEntry._ID + " INTEGER PRIMARY KEY, " +
								LocationEntry.COLUMN_NAME_LOCATION + TEXT_TYPE + COMMA_SEP +
								LocationEntry.COLUMN_NAME_DESCRIPTION + TEXT_TYPE + COMMA_SEP +
								LocationEntry.COLUMN_NAME_IMAGE + TEXT_TYPE + COMMA_SEP +
								LocationEntry.COLUMN_NAME_LATITUDE + REAL_TYPE + COMMA_SEP +
								LocationEntry.COLUMN_NAME_LONGTITUDE + REAL_TYPE +
								");";
		private static final String SQL_INSERT_DEFAULE_ENTRIES = 
				"INSERT INTO " + LocationEntry.TABLE_NAME + " (" +
				LocationEntry.COLUMN_NAME_LOCATION + COMMA_SEP +
				LocationEntry.COLUMN_NAME_DESCRIPTION + COMMA_SEP +
				LocationEntry.COLUMN_NAME_IMAGE + COMMA_SEP +
				LocationEntry.COLUMN_NAME_LATITUDE + COMMA_SEP +
				LocationEntry.COLUMN_NAME_LONGTITUDE + ") " +
				"VALUES ('UNKOWN', 'UNKOWN', '', 0, 0)";
		private static final String SQL_DELETE_ENTRIES =
						"DROP TABLE IF EXISTS " + LocationEntry.TABLE_NAME;
	}
	
	DataOpenHelper mDataOpenHelper;
	LocationOpenHelper mLocationOpenHelper;
	
	@Override
	public int delete(Uri uri, String where, String[] whereArgs) {
		SQLiteDatabase db;
		String id;	
        int count;
        
        switch (sUriMatcher.match(uri)) {
        case LOCATION_ID:
        	db = mLocationOpenHelper.getWritableDatabase();
        	id = uri.getPathSegments().get(1);
            count = db.delete(LocationEntry.TABLE_NAME, LocationEntry._ID + "=" + id
                    + (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : ""), whereArgs);
            break;

        case DATA_ID:
        	db = mDataOpenHelper.getWritableDatabase();
        	id = uri.getPathSegments().get(1);
            count = db.delete(DataEntry.TABLE_NAME, DataEntry._ID + "=" + id
                    + (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : ""), whereArgs);
            break;

        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
	}

	@Override
	public String getType(Uri uri) {
		switch (sUriMatcher.match(uri)) {
        case LOCATION:
        	return LocationEntry.CONTENT_TYPE;
        	
        case LOCATION_ID:
            return LocationEntry.CONTENT_ITEM_TYPE;

        case DATA:
            return DataEntry.CONTENT_TYPE;
            
        case DATA_ID:
            return DataEntry.CONTENT_ITEM_TYPE;

        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }
	}

	@Override
	public Uri insert(Uri uri, ContentValues initialValues) {
		// Validate the requested uri
        if ((sUriMatcher.match(uri) != LOCATION)&&(sUriMatcher.match(uri) != DATA)) {
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        ContentValues values;
        if (initialValues != null) {
            values = new ContentValues(initialValues);
        } else {
            values = new ContentValues();
        }
        
        if (sUriMatcher.match(uri) == LOCATION){
        	SQLiteDatabase db = mLocationOpenHelper.getWritableDatabase();
            long rowId = db.insert(LocationEntry.TABLE_NAME, null, values);
            if (rowId > 0) {
                Uri noteUri = ContentUris.withAppendedId(LocationEntry.CONTENT_URI, rowId);
                getContext().getContentResolver().notifyChange(noteUri, null);
                return noteUri;
            }
        }else if (sUriMatcher.match(uri) == DATA){
        	SQLiteDatabase db = mDataOpenHelper.getWritableDatabase();
            long rowId = db.insert(DataEntry.TABLE_NAME, null, values);
            if (rowId > 0) {
                Uri noteUri = ContentUris.withAppendedId(DataEntry.CONTENT_URI, rowId);
                getContext().getContentResolver().notifyChange(noteUri, null);
                return noteUri;
            }
        }
        
        throw new SQLException("Failed to insert row into " + uri);
	}

	@Override
	public boolean onCreate() {
		mDataOpenHelper = new DataOpenHelper(getContext());
		mLocationOpenHelper = new LocationOpenHelper(getContext());
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
			String sortOrder) {
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        

        switch (sUriMatcher.match(uri)) {
        case LOCATION:
        	qb.setTables(LocationEntry.TABLE_NAME);
            qb.setProjectionMap(sLocationListProjectionMap);
            break;

        case LOCATION_ID:
        	qb.setTables(LocationEntry.TABLE_NAME);
            qb.setProjectionMap(sLocationItemProjectionMap);
            qb.appendWhere(LocationEntry._ID + "=" + uri.getPathSegments().get(1));
            break;

        case DATA:
        	qb.setTables(DataEntry.TABLE_NAME);
            qb.setProjectionMap(sDataListProjectionMap);
            break;

        case DATA_ID:
        	qb.setTables(DataEntry.TABLE_NAME);
            qb.setProjectionMap(sDataItemProjectionMap);
            qb.appendWhere(DataEntry._ID + "=" + uri.getPathSegments().get(1));
            break;
            
        case LOCATION_IMAGE_NAME:
        	qb.setTables(LocationEntry.TABLE_NAME);
            qb.setProjectionMap(sLocationImageNameProjectionMap);
        	break;

        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        // If no sort order is specified use the default
//        String orderBy;
//        if (TextUtils.isEmpty(sortOrder)) {
//            orderBy = RecorderContract.DEFAULT_SORT_ORDER;
//        } else {
//            orderBy = sortOrder;
//        }

        // Get the database and run the query
        SQLiteDatabase db = null;
        switch (sUriMatcher.match(uri)) {
        case LOCATION:
        case LOCATION_ID:
        case LOCATION_IMAGE_NAME:
        	db = mLocationOpenHelper.getReadableDatabase();
            break;

        case DATA:
        case DATA_ID:
        	db = mDataOpenHelper.getReadableDatabase();
            break;
            
        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }
        
        Cursor c = qb.query(db, projection, selection, selectionArgs, null, null, null);

        // Tell the cursor what uri to watch, so it knows when its source data changes
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
	}

	@Override
	public int update(Uri uri, ContentValues values, String where, String[] whereArgs) {
		SQLiteDatabase db;
		String id;
        int count;
        
        switch (sUriMatcher.match(uri)) {
        case LOCATION_ID:
        	db = mLocationOpenHelper.getWritableDatabase();
        	id = uri.getPathSegments().get(1);
            count = db.update(LocationEntry.TABLE_NAME, values, LocationEntry._ID + "=" + id
        			+ (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : ""), whereArgs);
            break;

        case DATA_ID:
        	db = mDataOpenHelper.getWritableDatabase();
        	id = uri.getPathSegments().get(1);
            count = db.update(DataEntry.TABLE_NAME, values, DataEntry._ID + "=" + id
        			+ (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : ""), whereArgs);
            break;

        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
	}

	static {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(RecorderContract.AUTHORITY, "location", LOCATION);
        sUriMatcher.addURI(RecorderContract.AUTHORITY, "data", DATA);
        sUriMatcher.addURI(RecorderContract.AUTHORITY, "location/#", LOCATION_ID);
        sUriMatcher.addURI(RecorderContract.AUTHORITY, "data/#", DATA_ID);
        sUriMatcher.addURI(RecorderContract.AUTHORITY, "location/image_and_name", LOCATION_IMAGE_NAME);
        
        sLocationImageNameProjectionMap = new HashMap<String, String>();
        sLocationImageNameProjectionMap.put(LocationEntry._ID, LocationEntry._ID);
        sLocationImageNameProjectionMap.put(LocationEntry.COLUMN_NAME_LOCATION, LocationEntry.COLUMN_NAME_LOCATION);
        sLocationImageNameProjectionMap.put(LocationEntry.COLUMN_NAME_IMAGE, LocationEntry.COLUMN_NAME_IMAGE);

        sLocationListProjectionMap = new HashMap<String, String>();
        sLocationListProjectionMap.put(LocationEntry._ID, LocationEntry._ID);
        sLocationListProjectionMap.put(LocationEntry.COLUMN_NAME_LOCATION, LocationEntry.COLUMN_NAME_LOCATION);
        
        sDataListProjectionMap = new HashMap<String, String>();
        sDataListProjectionMap.put(DataEntry._ID, DataEntry._ID);
        sDataListProjectionMap.put(DataEntry.COLUMN_NAME_TIME, DataEntry.COLUMN_NAME_TIME);
        
        sLocationItemProjectionMap = new HashMap<String, String>();
        sLocationItemProjectionMap.put(LocationEntry._ID, LocationEntry._ID);
        sLocationItemProjectionMap.put(LocationEntry.COLUMN_NAME_LOCATION, LocationEntry.COLUMN_NAME_LOCATION);
        sLocationItemProjectionMap.put(LocationEntry.COLUMN_NAME_DESCRIPTION, LocationEntry.COLUMN_NAME_DESCRIPTION);
        sLocationItemProjectionMap.put(LocationEntry.COLUMN_NAME_IMAGE, LocationEntry.COLUMN_NAME_IMAGE);
        sLocationItemProjectionMap.put(LocationEntry.COLUMN_NAME_LATITUDE, LocationEntry.COLUMN_NAME_LATITUDE);
        sLocationItemProjectionMap.put(LocationEntry.COLUMN_NAME_LONGTITUDE, LocationEntry.COLUMN_NAME_LONGTITUDE);
        
        sDataItemProjectionMap = new HashMap<String, String>();
        sDataItemProjectionMap.put(DataEntry._ID, DataEntry._ID);
        sDataItemProjectionMap.put(DataEntry.COLUMN_NAME_LOCATION_ID, DataEntry.COLUMN_NAME_LOCATION_ID);
        sDataItemProjectionMap.put(DataEntry.COLUMN_NAME_TIME, DataEntry.COLUMN_NAME_TIME);
        sDataItemProjectionMap.put(DataEntry.COLUMN_NAME_TEMP, DataEntry.COLUMN_NAME_TEMP);
        sDataItemProjectionMap.put(DataEntry.COLUMN_NAME_DEPTH, DataEntry.COLUMN_NAME_DEPTH);
        sDataItemProjectionMap.put(DataEntry.COLUMN_NAME_RDO, DataEntry.COLUMN_NAME_RDO);
        sDataItemProjectionMap.put(DataEntry.COLUMN_NAME_SAT, DataEntry.COLUMN_NAME_SAT);
        sDataItemProjectionMap.put(DataEntry.COLUMN_NAME_ORP, DataEntry.COLUMN_NAME_ORP);
        sDataItemProjectionMap.put(DataEntry.COLUMN_NAME_PH, DataEntry.COLUMN_NAME_PH);
        sDataItemProjectionMap.put(DataEntry.COLUMN_NAME_ACT, DataEntry.COLUMN_NAME_ACT);
        sDataItemProjectionMap.put(DataEntry.COLUMN_NAME_BARO, DataEntry.COLUMN_NAME_BARO);

    }
}
