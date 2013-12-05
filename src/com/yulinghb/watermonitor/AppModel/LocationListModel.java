package com.yulinghb.watermonitor.AppModel;

import android.app.Activity;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;

import com.yulinghb.watermonitor.RecorderContract.LocationEntry;
import com.yulinghb.watermonitor.WaterMonitorClientContract;



public class LocationListModel {

	public static final String GET_CONTENT_ID = "location id";
	public static final String GET_CONTENT_NAME = "location name";
	public static final String GET_CONTENT_IMAGE = "location image";
	
//	private static final String TAG = "LocationListActivity";
	
	public interface LocationSelectedOnListener{
		void selectLocation(Intent data);
		void refresh(Cursor cursor);
	}

	private Cursor mCursor;
	private int location_id = 0;
	private FragmentActivity mActivity;
	LocationSelectedOnListener mListener;
	LocationListLoader loader;

	
	public LocationListModel(Activity activity){
		mActivity = (FragmentActivity)activity;
		loader = new LocationListLoader();
	}
	
	public void onAttach(LocationSelectedOnListener listener){
		mListener = (LocationSelectedOnListener) listener;
	}

	public Cursor getCursor(){
//		cursor = mActivity.managedQuery(LocationEntry.IMAGE_AND_NAME_URI, PROJECTION, null, null,
//                RecorderContract.DEFAULT_SORT_ORDER);
		mActivity.getSupportLoaderManager().restartLoader(WaterMonitorClientContract.LOCATION_LIST_LOADER, null, loader);
		return null;
	}
	
	public void update(){
		mActivity.getSupportLoaderManager().restartLoader(WaterMonitorClientContract.LOCATION_LIST_LOADER, null, loader);
	}

	
	public void createOnClick(){
		Intent intent = new Intent(Intent.ACTION_INSERT, LocationEntry.CONTENT_URI);
		intent.putExtra(LocationEditModel.ACTION, Intent.ACTION_INSERT);
		intent.putExtra(LocationEditModel.URI, LocationEntry.CONTENT_URI.toString());
//		if (null == mListener){
//			mActivity.startActivity(intent);
//		}else{
			mListener.selectLocation(intent);
//		}
		
	}
	
	public void deleteOnClick(long deleteid){
		Uri noteUri = ContentUris.withAppendedId(LocationEntry.CONTENT_URI, deleteid);
		mActivity.getContentResolver().delete(noteUri, null, null);
		mActivity.getSupportLoaderManager().restartLoader(WaterMonitorClientContract.LOCATION_LIST_LOADER, null, loader);
	}
	
	public void editOnClick(){
//		Uri noteUri = ContentUris.withAppendedId(LocationEntry.CONTENT_URI, location_id);
//		Intent intent = new Intent(Intent.ACTION_EDIT, noteUri);
//		if (null == mListener){
//			mActivity.startActivity(intent);
//		}else{
//			mListener.selectLocation(intent);
//		}
	}
	
	public void selectedLocationOnClick(int position){
		if (null == mCursor){
			return;
		}
		mCursor.moveToPosition(position);
		location_id = mCursor.getInt(0);
		String location_name = mCursor.getString(1);
		String location_image_path = mCursor.getString(2);
		Intent intent = new Intent();
		intent.putExtra(GET_CONTENT_ID, location_id);
		intent.putExtra(GET_CONTENT_NAME, location_name);
		intent.putExtra(GET_CONTENT_IMAGE, location_image_path);
		mActivity.setResult(Activity.RESULT_OK, intent);
		
		if (null != mListener){
			Uri noteUri = ContentUris.withAppendedId(LocationEntry.CONTENT_URI, location_id);
			intent = new Intent(Intent.ACTION_EDIT, noteUri);
			intent.putExtra(LocationEditModel.ACTION, Intent.ACTION_EDIT);
			intent.putExtra(LocationEditModel.URI, noteUri.toString());
			mListener.selectLocation(intent);
		}
	}
	
	public void onActivityResult(int requestCode, int resultCode, Intent data){
//		updateSelectedLocation(data);
	}
	
	class LocationListLoader implements LoaderManager.LoaderCallbacks<Cursor>{

		/**
	     * The columns we are interested in from the database
	     */
	    private final String[] PROJECTION = new String[] {
	    	LocationEntry._ID, // 0
	    	LocationEntry.COLUMN_NAME_LOCATION, // 1
	    	LocationEntry.COLUMN_NAME_IMAGE,    // 2
	    };
	    
		@Override
		public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
			// TODO Auto-generated method stub
			return new CursorLoader(mActivity, LocationEntry.IMAGE_AND_NAME_URI,
					PROJECTION, null, null, null);
		}

		@Override
		public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
			// TODO Auto-generated method stub
			mCursor = cursor;
			mListener.refresh(cursor);
		}

		@Override
		public void onLoaderReset(Loader<Cursor> loader) {
			// TODO Auto-generated method stub
			mListener.refresh(null);
		}
		
	}
}
