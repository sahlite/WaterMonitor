package com.yulinghb.watermonitor.AppModel;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import android.app.Activity;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;

import com.yulinghb.watermonitor.R;
import com.yulinghb.watermonitor.RecorderContract.DataEntry;
import com.yulinghb.watermonitor.RecorderContract.LocationEntry;
import com.yulinghb.watermonitor.RecorderFile;
import com.yulinghb.watermonitor.WaterMonitorClientContract;

public class RecorderEditModel {
	public static final int INTENT_GET_LOCATION = 12;
	public static final String TAG = "RecorderEditModel";
	public static final String URI = "recorder edit uri";
	public static final String ACTION = "recorder edit action";
	
	public interface RecorderUpdateOnClick{
		void updateTimeOnClick(String time);
		void updateDOOnClick(String doValue);
		void updateTempOnClick(String temperature);
		void updateSATOnClick(String sat);
		void updateBaroOnClick(String baro);
		void updateLocationOnClick(String location);
		void updateImageOnClick(Bitmap data);
		void updateDefaultImage();
		int getImageWidth();
		int getImageHeight();
	}
    
    private Uri mUri;
    private String sData;
    private String sImage;
    private int selected_location = 0;
    private FragmentActivity mActivity;
    RecorderUpdateOnClick mUpdateCallback;
    RecorderLoader mRecorderLoader;
    LocationLoader mLocationLoader;

	public RecorderEditModel(Activity activity, RecorderUpdateOnClick callback){
		mActivity = (FragmentActivity)activity;
		mUpdateCallback = callback;
		mRecorderLoader = new RecorderLoader();
		mLocationLoader = new LocationLoader();
	}
	
	public void update(){
		Intent intent = mActivity.getIntent();
        // Do some setup based on the action being performed.
        final String action = intent.getStringExtra(ACTION);
        if (Intent.ACTION_EDIT.equals(action)) {
            // Requested to edit: set that state, and the data being edited.
            mUri = Uri.parse(intent.getStringExtra(URI));
            
            // Get the note!
            mActivity.getSupportLoaderManager().restartLoader(WaterMonitorClientContract.RECORDER_EDITOR_LOADER, null, mRecorderLoader);
        } else {
            // Whoops, unknown action!  Bail.
            Log.e(TAG, "Unknown action, exiting");
        }
	}
	
	public void emailOnClick(){
		if (null == mUri){
			return;
		}
		
		try{
			RecorderFile.create();
			RecorderFile.write(sData);
			
			Intent sendIntent = new Intent();
			
			if (null == sImage){
				sendIntent.setAction(Intent.ACTION_SEND);
				sendIntent.putExtra(Intent.EXTRA_TEXT, sData);
				File file = RecorderFile.get();
				sendIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
				sendIntent.setType("plain/text");
			}else{
				sendIntent.setAction(Intent.ACTION_SEND_MULTIPLE);
				sendIntent.putExtra(Intent.EXTRA_TEXT, sData);
				ArrayList<Uri> imageUris = new ArrayList<Uri>();
				File file = RecorderFile.get();
				imageUris.add(Uri.fromFile(file));
				imageUris.add(Uri.parse(sImage));
				sendIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, imageUris);
				sendIntent.setType("image/jpeg");
			}
			
			mActivity.startActivity(sendIntent);
		}catch(IOException e){
			Log.e(TAG, "data file create and write exception:", e);
//			isFileOK = false;
		}
	}
	
	public void changeLocationOnClick(){
		if (null == mUri){
			return;
		}
			
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		mActivity.startActivityForResult(intent, INTENT_GET_LOCATION);
	}
	
	public void updateSelectedLocation(Intent data){
		selected_location = data.getIntExtra(LocationListModel.GET_CONTENT_ID, 1);
		String name = data.getStringExtra(LocationListModel.GET_CONTENT_NAME);
		sImage = data.getStringExtra(LocationListModel.GET_CONTENT_IMAGE);
		mUpdateCallback.updateLocationOnClick(name);
		if ((null == sImage)||("" == sImage)){
			mUpdateCallback.updateDefaultImage();
		}else{
			Bitmap bitmap = RecorderFile.getBitmap(mActivity, sImage, mActivity.getString(R.string.txt_unkown), 
					mUpdateCallback.getImageWidth(), 
					mUpdateCallback.getImageHeight());
			mUpdateCallback.updateImageOnClick(bitmap);
		}

	    ContentValues values = new ContentValues();
		values.put(DataEntry.COLUMN_NAME_LOCATION_ID, selected_location);

		mActivity.getContentResolver().update(mUri, values, null, null);

	}
	
	public void onActivityResult(int requestCode, int resultCode, Intent data){
		if ((INTENT_GET_LOCATION == requestCode)&&(Activity.RESULT_OK == resultCode)){
			updateSelectedLocation(data);
//			selected_location = data.getIntExtra(LocationListModel.GET_CONTENT_ID, 0);
		}
	}
	
	class RecorderLoader implements LoaderManager.LoaderCallbacks<Cursor>{

		/**
	     * Standard projection for the interesting columns of a normal note.
	     */
	    private final String[] PROJECTION = new String[] {
	        DataEntry._ID, // 0
	        DataEntry.COLUMN_NAME_LOCATION_ID, //1
	        DataEntry.COLUMN_NAME_TIME, // 2
	        DataEntry.COLUMN_NAME_TEMP, // 3
	        DataEntry.COLUMN_NAME_DEPTH, // 4
	        DataEntry.COLUMN_NAME_RDO, // 5
	        DataEntry.COLUMN_NAME_SAT, // 6
	        DataEntry.COLUMN_NAME_ORP, // 7
	        DataEntry.COLUMN_NAME_PH, // 8
	        DataEntry.COLUMN_NAME_ACT, // 9
	        DataEntry.COLUMN_NAME_BARO, // 10
	    };
	    
	    /** The index of the location column */
	    private static final int COLUMN_INDEX_LOCATION_ID = 1;
	    /** The index of the time column */
	    private static final int COLUMN_INDEX_TIME = 2;
	    /** The index of the temperature column */
	    private static final int COLUMN_INDEX_TEMP = 3;
	    /** The index of the depth column */
//	    private static final int COLUMN_INDEX_DEPTH = 4;
	    /** The index of the rdo column */
	    private static final int COLUMN_INDEX_RDO = 5;
	    /** The index of the rdo.sat column */
	    private static final int COLUMN_INDEX_SAT = 6;
	    /** The index of the orp column */
//	    private static final int COLUMN_INDEX_ORP = 7;
//	    /** The index of the ph column */
//	    private static final int COLUMN_INDEX_PH = 8;
//	    /** The index of the act column */
//	    private static final int COLUMN_INDEX_ACT = 9;
	    /** The index of the baro column */
	    private static final int COLUMN_INDEX_BARO = 10;
	    
		@Override
		public Loader<Cursor> onCreateLoader(int id, Bundle args) {
			// TODO Auto-generated method stub
			return new CursorLoader(mActivity, mUri,
					PROJECTION, null, null, null);
		}

		@Override
		public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
			// TODO Auto-generated method stub
			if (0 < cursor.getCount()){
				cursor.moveToFirst();
				
				String tmp;

	            mUpdateCallback.updateTimeOnClick(cursor.getString(COLUMN_INDEX_TIME));

	            tmp = String.format("%.2f", cursor.getFloat(COLUMN_INDEX_TEMP));//String.valueOf(tmpNumber);
	            mUpdateCallback.updateTempOnClick(tmp);
	            sData = tmp;

	            tmp = String.format("%.2f", cursor.getFloat(COLUMN_INDEX_RDO)*100);
	            mUpdateCallback.updateDOOnClick(tmp);
	            sData += ","+tmp;

	            tmp = String.format("%.2f", cursor.getFloat(COLUMN_INDEX_SAT));
	            mUpdateCallback.updateSATOnClick(tmp);
	            sData += ","+tmp;

	            tmp = String.format("%.2f", cursor.getFloat(COLUMN_INDEX_BARO));
	            mUpdateCallback.updateBaroOnClick(tmp);
	            sData += ","+tmp;
	            
	            selected_location = cursor.getInt(COLUMN_INDEX_LOCATION_ID);
	            
	            mActivity.getSupportLoaderManager().restartLoader(WaterMonitorClientContract.RECORDER_LOCATION_LOADER, null, mLocationLoader);
			}else{
				
			}
			
		}

		@Override
		public void onLoaderReset(Loader<Cursor> loader) {
			// TODO Auto-generated method stub
			
		}
		
	}
	
	class LocationLoader implements LoaderManager.LoaderCallbacks<Cursor>{

		private final String[] PROJECTION = new String[] {
	        LocationEntry._ID, // 0
	        LocationEntry.COLUMN_NAME_LOCATION, //1
	        LocationEntry.COLUMN_NAME_DESCRIPTION, // 2
	        LocationEntry.COLUMN_NAME_IMAGE, // 3
	        LocationEntry.COLUMN_NAME_LATITUDE, // 4
	        LocationEntry.COLUMN_NAME_LONGTITUDE, // 5
	    };
	    
	    /** The index of the time column */
	    private static final int COLUMN_INDEX_LOCATION = 1;
	    /** The index of the temperature column */
//	    private static final int COLUMN_INDEX_DESCRIPTION = 2;
	    /** The index of the depth column */
	    private static final int COLUMN_INDEX_IMAGE = 3;
	    /** The index of the rdo column */
//	    private static final int COLUMN_INDEX_LATITUDE = 4;
	    /** The index of the rdo column */
//	    private static final int COLUMN_INDEX_LONGTITUDE = 5;
	    
		@Override
		public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
			// TODO Auto-generated method stub
			Uri uri = ContentUris.withAppendedId(LocationEntry.CONTENT_URI, selected_location);
			return new CursorLoader(mActivity, uri,
					PROJECTION, null, null, null);
		}

		@Override
		public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
			// TODO Auto-generated method stub
			if (cursor.getCount() == 0)
				return;
			cursor.moveToFirst();
			String name = cursor.getString(COLUMN_INDEX_LOCATION);
			mUpdateCallback.updateLocationOnClick(name);
	        sImage = cursor.getString(COLUMN_INDEX_IMAGE);
	        if ((null == sImage)||("" == sImage)){
	        	mUpdateCallback.updateDefaultImage();
	        }else{
	        	Bitmap bitmap = RecorderFile.getBitmap(mActivity, sImage, mActivity.getString(R.string.txt_unkown), 
						mUpdateCallback.getImageWidth(), 
						mUpdateCallback.getImageHeight());
				//ivLocation.setImageBitmap(bitmap);
				mUpdateCallback.updateImageOnClick(bitmap);
	        }
		}

		@Override
		public void onLoaderReset(Loader<Cursor> loader) {
			// TODO Auto-generated method stub
			
		}
		
	}
}
